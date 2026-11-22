package com.wanderingpet.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具：统一 String 序列化，覆盖登录态/缓存/限流/锁/榜单场景
 */
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final StringRedisTemplate redisTemplate;

    /** 写入并设置 TTL */
    public void set(String key, String value, Duration timeout) {
        redisTemplate.opsForValue().set(key, value, timeout);
    }

    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    public Boolean expire(String key, Duration timeout) {
        return redisTemplate.expire(key, timeout);
    }

    /** 自增并设置过期时间（首次自增时生效），返回自增后的值 */
    public Long incr(String key, Duration timeout) {
        Long value = redisTemplate.opsForValue().increment(key);
        if (value != null && value == 1L && timeout != null) {
            redisTemplate.expire(key, timeout);
        }
        return value;
    }

    /** SETNX + TTL：分布式锁/防重锁，true 表示获得锁 */
    public boolean tryLock(String key, String value, Duration timeout) {
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, value, timeout);
        return Boolean.TRUE.equals(ok);
    }

    public void unlock(String key) {
        redisTemplate.delete(key);
    }

    // ---------- Hash（数据字典缓存等） ----------

    public void hashSetAll(String key, Map<String, String> map, Duration timeout) {
        redisTemplate.opsForHash().putAll(key, map);
        redisTemplate.expire(key, timeout);
    }

    public Map<Object, Object> hashGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    // ---------- Set（点赞去重等） ----------

    public void setAdd(String key, String member, Duration timeout) {
        redisTemplate.opsForSet().add(key, member);
        redisTemplate.expire(key, timeout);
    }

    public boolean setIsMember(String key, String member) {
        Boolean ok = redisTemplate.opsForSet().isMember(key, member);
        return Boolean.TRUE.equals(ok);
    }

    // ---------- ZSet（浏览量榜单等） ----------

    public void zIncrement(String key, String member, double delta, Duration timeout) {
        redisTemplate.opsForZSet().incrementScore(key, member, delta);
        redisTemplate.expire(key, timeout);
    }

    public Set<ZSetOperations.TypedTuple<String>> zRevRangeWithScores(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
    }

    // ---------- List（简单队列/批量读取） ----------

    public void listPushAll(String key, List<String> values, Duration timeout) {
        redisTemplate.opsForList().rightPushAll(key, values);
        redisTemplate.expire(key, timeout);
    }

    public Long listSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    // ---------- 通用 ----------

    public StringRedisTemplate template() {
        return redisTemplate;
    }

    public long getExpireSeconds(String key) {
        Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire == null ? 0 : expire;
    }
}
