package com.wanderingpet.module.auth.service;

import com.wanderingpet.module.auth.dto.SmsSendDTO;
import com.wanderingpet.module.auth.vo.SmsSendVO;

/**
 * 短信验证码服务：发送（限流 + 单日上限）与校验（一次性 + 失败计数）
 */
public interface SmsService {

    /** 下发验证码：同场景同手机号 60 秒内只能发一次，单日上限 10 条 */
    SmsSendVO sendCode(SmsSendDTO dto);

    /** 校验验证码：一次性使用，失败超过上限即作废 */
    void verifyCode(String phone, String code, String scene);
}
