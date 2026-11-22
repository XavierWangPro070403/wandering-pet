package com.wanderingpet.common.exception;

import com.wanderingpet.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常：携带业务错误码与用户可读提示
 */
@Getter
public class BizException extends RuntimeException {

    private final Integer code;

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BizException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public static BizException of(ResultCode resultCode) {
        return new BizException(resultCode);
    }

    public static BizException of(ResultCode resultCode, String message) {
        return new BizException(resultCode, message);
    }

    public static BizException of(Integer code, String message) {
        return new BizException(code, message);
    }
}
