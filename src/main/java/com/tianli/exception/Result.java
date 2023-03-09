package com.tianli.exception;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by wangqiyun on 2018/7/11.
 */
@Data
@NoArgsConstructor
public class Result<T> {
    private String code = "0";
    private String msg = "成功";
    private String enMsg = "Succeed";
    private long time = System.currentTimeMillis();
    private T data = null;

    public static <T> Result<T> instance() {
        return new Result<>();
    }

    public Result(T data) {
        this.data = data;
    }

    public Result<T> setData(T data) {
        this.data = data;
        return this;
    }

    public static <T> Result<T> success(T data) {
        Result<T> instance = Result.instance();
        instance.setData(data);
        return instance;
    }

    public static <T> Result<T> success() {
        return Result.instance();
    }

    public static <T> Result<T> fail(ErrorCodeEnum errorCodeEnum) {
        Result<T> result = Result.instance();
        result.setCode(String.valueOf(errorCodeEnum.getErrorNo()));
        result.setMsg(errorCodeEnum.getErrorMsg());
        return result;
    }

    public static <T> Result<T> fail(String msg) {
        Result<T> result = Result.instance();
        result.setCode(String.valueOf(ErrorCodeEnum.SYSTEM_ERROR.getErrorNo()));
        result.setMsg(msg);
        return result;
    }
}
