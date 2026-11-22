package com.wanderingpet.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.MDC;

import java.io.Serializable;

/**
 * 统一响应体：code / message / data / traceId / timestamp
 *
 * @param <T> 数据类型
 */
@Data
@AllArgsConstructor
public class R<T> implements Serializable {

    private Integer code;
    private String message;
    private T data;
    private String traceId;
    private Long timestamp;

    public static <T> R<T> ok() {
        return build(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    public static <T> R<T> ok(T data) {
        return build(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> R<T> ok(String message, T data) {
        return build(ResultCode.SUCCESS.getCode(), message, data);
    }

    public static <T> R<T> fail(ResultCode resultCode) {
        return build(resultCode.getCode(), resultCode.getMessage(), null);
    }

    public static <T> R<T> fail(Integer code, String message) {
        return build(code, message, null);
    }

    public static <T> R<T> fail(ResultCode resultCode, String message) {
        return build(resultCode.getCode(), message, null);
    }

    private static <T> R<T> build(Integer code, String message, T data) {
        return new R<>(code, message, data, MDC.get("traceId"), System.currentTimeMillis());
    }
}
