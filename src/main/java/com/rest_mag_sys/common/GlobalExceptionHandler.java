package com.rest_mag_sys.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理SQL完整性约束异常
     * @param ex 异常
     * @return 错误信息
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        log.error("SQL异常：{}", ex.getMessage());
        
        // 处理唯一约束异常
        if (ex.getMessage().contains("Duplicate entry")) {
            String[] split = ex.getMessage().split(" ");
            String msg = split[2] + "已存在";
            return R.error(msg);
        }
        
        return R.error("未知数据库错误");
    }

    /**
     * 处理自定义业务异常
     * @param ex 异常
     * @return 错误信息
     */
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException ex) {
        log.error("业务异常：{}", ex.getMessage());
        return R.error(ex.getMessage());
    }

    /**
     * 处理其他未知异常
     * @param ex 异常
     * @return 错误信息
     */
    @ExceptionHandler(Exception.class)
    public R<String> exceptionHandler(Exception ex) {
        log.error("系统异常：{}", ex.getMessage());
        return R.error("系统异常，请联系管理员");
    }

    /**
     * 处理参数校验异常（@Valid @RequestBody）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<String> exceptionHandler(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "请求参数校验失败";
        log.warn("参数校验异常：{}", message);
        return R.error(400, message);
    }

    /**
     * 处理参数绑定异常（query/path/form）
     */
    @ExceptionHandler(BindException.class)
    public R<String> exceptionHandler(BindException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "请求参数绑定失败";
        log.warn("参数绑定异常：{}", message);
        return R.error(400, message);
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public R<String> exceptionHandler(MethodArgumentTypeMismatchException ex) {
        log.warn("参数类型不匹配：{}", ex.getMessage());
        return R.error(400, "参数类型错误");
    }

    /**
     * 处理HTTP方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R<String> exceptionHandler(HttpRequestMethodNotSupportedException ex) {
        log.warn("请求方法不支持：{}", ex.getMessage());
        return R.error(405, "请求方法不支持");
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public R<String> exceptionHandler(IllegalArgumentException ex) {
        log.warn("非法参数：{}", ex.getMessage());
        return R.error(400, ex.getMessage());
    }
} 