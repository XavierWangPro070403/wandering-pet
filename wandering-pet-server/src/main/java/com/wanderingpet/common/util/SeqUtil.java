package com.wanderingpet.common.util;

import com.wanderingpet.common.constant.RedisKeyConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 业务编号生成：Redis INCR 序列 + 日期，格式如 PET202611160001
 */
@Component
@RequiredArgsConstructor
public class SeqUtil {

    private final RedisUtil redisUtil;

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 生成业务编号
     *
     * @param biz    业务标识（seq:biz:date 作为 Redis Key）
     * @param prefix 编号前缀（PET/APP/ADO/PAY/RSR）
     * @return 编号（前缀 + yyyyMMdd + 4 位序号）
     */
    public String next(String biz, String prefix) {
        String date = LocalDate.now().format(DATE);
        Long seq = redisUtil.incr(RedisKeyConstant.SEQ_BIZ.formatted(biz, date),
                java.time.Duration.ofHours(48));
        long value = seq == null ? 1 : seq;
        return "%s%s%04d".formatted(prefix, date, value);
    }
}
