package com.wanderingpet.common.exception;

import com.wanderingpet.common.result.R;
import com.wanderingpet.common.result.ResultCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.stream.Collectors;

/**
 * 全局异常处理：业务异常 → 业务码；参数校验 → 400 + 首个错误；
 * 未捕获异常 → 500 + traceId（响应体不暴露堆栈，完整堆栈仅进日志）
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public R<Void> handleBiz(BizException e) {
        log.warn("业务异常 code={} message={}", e.getCode(), e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(ResultCode.BAD_REQUEST.getMessage());
        return R.fail(ResultCode.BAD_REQUEST.getCode(), message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public R<Void> handleConstraint(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .findFirst()
                .map(v -> v.getMessage())
                .orElse(ResultCode.BAD_REQUEST.getMessage());
        return R.fail(ResultCode.BAD_REQUEST.getCode(), message);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class})
    public R<Void> handleBadRequest(Exception e) {
        log.warn("请求参数错误: {}", e.getMessage());
        return R.fail(ResultCode.BAD_REQUEST);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public R<Void> handleMaxUpload(MaxUploadSizeExceededException e) {
        return R.fail(ResultCode.FILE_TOO_LARGE);
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("未捕获异常", e);
        return R.fail(ResultCode.ERROR);
    }
}
