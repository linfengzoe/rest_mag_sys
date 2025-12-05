package com.rest_mag_sys.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回结果类，服务端响应的数据最终都会封装成此对象
 * @param <T> 返回数据的类型
 */
@Data
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 编码：200成功，其它数字为失败
     */
    private Integer code;

    /**
     * 错误信息
     */
    private String msg;

    /**
     * 数据
     */
    private T data;

    /**
     * 成功结果
     * @param <T> 数据类型
     * @return 成功结果
     */
    public static <T> R<T> success() {
        R<T> r = new R<>();
        r.code = 200;
        r.msg = "成功";
        return r;
    }

    /**
     * 成功结果
     * @param data 数据
     * @param <T> 数据类型
     * @return 成功结果
     */
    public static <T> R<T> success(T data) {
        R<T> r = new R<>();
        r.code = 200;
        r.msg = "成功";
        r.data = data;
        return r;
    }

    /**
     * 失败结果
     * @param <T> 数据类型
     * @return 失败结果
     */
    public static <T> R<T> error() {
        R<T> r = new R<>();
        r.code = 0;
        r.msg = "失败";
        return r;
    }

    /**
     * 失败结果
     * @param msg 错误信息
     * @param <T> 数据类型
     * @return 失败结果
     */
    public static <T> R<T> error(String msg) {
        R<T> r = new R<>();
        r.code = 0;
        r.msg = msg;
        return r;
    }

    /**
     * 失败结果
     * @param code 错误码
     * @param msg 错误信息
     * @param <T> 数据类型
     * @return 失败结果
     */
    public static <T> R<T> error(Integer code, String msg) {
        R<T> r = new R<>();
        r.code = code;
        r.msg = msg;
        return r;
    }
} 