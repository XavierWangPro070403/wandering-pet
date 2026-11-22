package com.wanderingpet.module.auth.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.wanderingpet.common.constant.RedisKeyConstant;
import com.wanderingpet.common.exception.BizException;
import com.wanderingpet.common.result.ResultCode;
import com.wanderingpet.common.util.RedisUtil;
import com.wanderingpet.config.props.SmsProperties;
import com.wanderingpet.module.auth.dto.SmsSendDTO;
import com.wanderingpet.module.auth.service.SmsService;
import com.wanderingpet.module.auth.sms.AliyunSmsSender;
import com.wanderingpet.module.auth.sms.DevSmsSender;
import com.wanderingpet.module.auth.sms.SmsScene;
import com.wanderingpet.module.auth.sms.SmsSender;
import com.wanderingpet.module.auth.vo.SmsSendVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 短信验证码实现：Redis 存储 + 重发节流 + 单日上限 + 失败计数
 *
 * <p>未配置阿里云凭证时走 {@link DevSmsSender}（不真实发送，验证码随响应回传），
 * 便于本地双端联调；配置后自动切到 {@link AliyunSmsSender}。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final SmsProperties smsProperties;
    private final RedisUtil redisUtil;
    private final DevSmsSender devSmsSender;
    private final AliyunSmsSender aliyunSmsSender;

    @Override
    public SmsSendVO sendCode(SmsSendDTO dto) {
        String phone = dto.getPhone().trim();
        String scene = SmsScene.normalize(dto.getScene());
        boolean devMode = smsProperties.devMode();

        // 1. 重发节流：同一手机号同一场景 60 秒内只允许发送一次
        String lockKey = RedisKeyConstant.SMS_SEND_LOCK.formatted(scene, phone);
        if (!redisUtil.tryLock(lockKey, "1", Duration.ofSeconds(smsProperties.getResendSeconds()))) {
            throw BizException.of(ResultCode.SMS_SEND_TOO_FREQUENT);
        }

        // 2. 单日发送上限（防刷）
        String dayKey = RedisKeyConstant.SMS_SEND_DAY.formatted(phone, LocalDate.now().format(DAY_FORMAT));
        Long sentToday = redisUtil.incr(dayKey, Duration.ofHours(24));
        if (sentToday != null && sentToday > smsProperties.getDailyLimit()) {
            redisUtil.delete(lockKey);
            throw BizException.of(ResultCode.SMS_DAILY_LIMIT);
        }

        // 3. 生成验证码并覆盖旧码（重新获取后旧码立即失效）
        String code = RandomUtil.randomNumbers(6);
        String codeKey = RedisKeyConstant.SMS_CODE.formatted(scene, phone);
        redisUtil.set(codeKey, code, Duration.ofSeconds(smsProperties.getCodeExpireSeconds()));
        redisUtil.delete(RedisKeyConstant.SMS_VERIFY_FAIL.formatted(scene, phone));

        // 4. 下发（失败则回滚验证码与节流锁，允许用户立即重试）
        SmsSender sender = devMode ? devSmsSender : aliyunSmsSender;
        try {
            sender.send(phone, code, scene);
        } catch (RuntimeException e) {
            redisUtil.delete(codeKey);
            redisUtil.delete(lockKey);
            throw e;
        }

        SmsSendVO vo = new SmsSendVO();
        vo.setDevMode(devMode);
        vo.setCode(devMode ? code : null);
        vo.setExpireSeconds(smsProperties.getCodeExpireSeconds());
        vo.setResendSeconds(smsProperties.getResendSeconds());
        vo.setProvider(sender.provider());
        return vo;
    }

    @Override
    public void verifyCode(String phone, String code, String scene) {
        String sceneValue = SmsScene.normalize(scene);
        String codeKey = RedisKeyConstant.SMS_CODE.formatted(sceneValue, phone);
        String cached = redisUtil.get(codeKey);
        if (cached == null) {
            throw BizException.of(ResultCode.SMS_CODE_ERROR);
        }

        String failKey = RedisKeyConstant.SMS_VERIFY_FAIL.formatted(sceneValue, phone);
        Long attempts = redisUtil.incr(failKey, Duration.ofSeconds(smsProperties.getCodeExpireSeconds()));
        if (attempts != null && attempts > smsProperties.getMaxVerifyAttempts()) {
            redisUtil.delete(codeKey);
            redisUtil.delete(failKey);
            throw BizException.of(ResultCode.SMS_CODE_ERROR, "验证码错误次数过多，请重新获取");
        }
        if (!cached.equals(code)) {
            throw BizException.of(ResultCode.SMS_CODE_ERROR);
        }

        // 一次性使用：校验通过立即作废
        redisUtil.delete(codeKey);
        redisUtil.delete(failKey);
    }
}
