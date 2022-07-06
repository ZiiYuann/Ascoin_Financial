package com.tianli.exception;

import com.tianli.tool.MapTool;

import java.util.List;

/**
 * Created by wangqiyun on 2018/7/11.
 */
public class Result {
    private String code = "0";
    private String msg = "成功";
    private String enMsg = "Succeed";
    private long time = System.currentTimeMillis();
    private Object data = null;

    public static Result instance() {
        return new Result();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getEnMsg() {
        return enMsg;
    }

    public void setEnMsg(String enMsg) {
        this.enMsg = enMsg;
    }

    public Object getData() {
        return data;
    }

    public Result setData(Object data) {
        this.data = data;
        return this;
    }

    public Result setList(List list, long count) {
        this.data = MapTool.Map().put("list", list).put("total", count);
        return this;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public static Result success(Object data) {
        return Result.instance().setData(data);
    }

    public static Result success() {
        return Result.instance();
    }

    public static Result fail(ErrorCodeEnum errorCodeEnum) {
        Result result = Result.instance();
        result.setCode(String.valueOf(errorCodeEnum.getErrorNo()));
        result.setMsg(errorCodeEnum.getErrorMsg());
        return result;
    }

    public static Result fail(String msg) {
        Result result = Result.instance();
        result.setCode(String.valueOf(ErrorCodeEnum.SYSTEM_ERROR.getErrorNo()));
        result.setMsg(msg);
        return result;
    }
}
