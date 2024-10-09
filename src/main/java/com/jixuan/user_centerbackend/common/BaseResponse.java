package com.jixuan.user_centerbackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回类
 * @param <T>
 * @author JxZhang
 */
@Data
public class BaseResponse<T> implements Serializable {

    private int code;

    private T data;

    private String message;

    private String description;

    // 构造函数，在需要四个参数时进行调用
    public BaseResponse(int code, T data, String message, String description) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.description = description;
    }

    //在不需要详细描述时进行调用
    public BaseResponse(int code, T data, String message) {
        this(code, data, message, "");
    }

    // 在只需要状态码和数据时进行调用
    public BaseResponse(int code, T data) {
        this(code, data, "", "");
    }

    // 从错误码中获取状态码、信息和描述
    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage(), errorCode.getDescription());
    }

}
