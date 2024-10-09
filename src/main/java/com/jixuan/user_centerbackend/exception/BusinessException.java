package com.jixuan.user_centerbackend.exception;

import com.jixuan.user_centerbackend.common.ErrorCode;

/**
 * 自定义异常类，对 RuntimeException 进行封装，新增加一些字段（code, description）
 * @author JxZhang
 */
public class BusinessException extends RuntimeException {

    private final int code;

    private final String description;

    public BusinessException(String message, int code, String description) {
        // 将子类的 message 传递给父类
        super(message);
        this.code = code;
        this.description = description;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }

    public BusinessException(ErrorCode errorCode, String description) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }
    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
