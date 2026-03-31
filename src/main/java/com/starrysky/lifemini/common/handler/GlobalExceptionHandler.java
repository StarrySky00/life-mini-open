package com.starrysky.lifemini.common.handler;

import com.starrysky.lifemini.common.constant.MessageConstant;
import com.starrysky.lifemini.model.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.reactive.resource.NoResourceFoundException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Pattern DUPLICATE_ENTRY_PATTERN = Pattern.compile("Duplicate entry '(.+?)' for key '(.+?)'");

    /**
     * 统一返回错误结果
     *
     * @param message 错误信息
     * @return
     */
    private Result errorResult(String message) {
        return Result.error(message);
    }

    /**
     * 处理SQL异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result handleSqlIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException ex) {
        log.error("SQL异常：{}", ex.getMessage(), ex);
        Matcher matcher = DUPLICATE_ENTRY_PATTERN.matcher(ex.getMessage());
        try {
            if (matcher.find()) {
                String msg = matcher.group(2) + " " + MessageConstant.ALREADY_EXISTS;
                return errorResult(msg);
            }
        } catch (IndexOutOfBoundsException e) {
            log.error("解析SQL异常时发生错误：{}", e.getMessage(), e);
        }
        return errorResult(MessageConstant.UNKNOWN_ERROR);
    }

    /**
     * 处理参数校验异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("参数校验异常：{}", ex.getMessage(), ex);
        // 获取所有校验失败的字段和错误信息
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                .orElse("参数校验失败");
        return errorResult(errorMessage);
    }
    /**
     * 处理客户端主动断开连接引发的异常
     */
    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleAsyncRequestNotUsableException(AsyncRequestNotUsableException e) {
        log.error("客户端端主动断开了异步请求连接: {}", e.getMessage());
    }

    /**
     * 处理其他未知异常
     * @param ex
     * @return
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleException(Exception ex) {
        log.error("未知异常：{}", ex.getMessage(), ex);
        String message = StringUtils.hasLength(ex.getMessage()) ? ex.getMessage() : MessageConstant.UNKNOWN_ERROR;
        return errorResult(message);
    }

}