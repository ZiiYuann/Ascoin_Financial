package com.tianli.exception;

/**
 * @author:yangkang
 * @create: 2023-02-24 11:38
 * @Description: 自定义异常类
 */
public class CustomException extends  RuntimeException{

    private Integer code;

    public CustomException() {
        super();
    }

    public CustomException(String msg) {
        super(msg);
    }

    public CustomException(Integer code, String msg) {
        super(msg);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }


}
