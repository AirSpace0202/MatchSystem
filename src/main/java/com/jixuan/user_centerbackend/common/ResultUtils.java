package com.jixuan.user_centerbackend.common;


/**
 * 返回工具类，在不同场景下能够创建相同的响应结果
 * @author JxZhang
 */
public class ResultUtils {

    // 将前端返回的 json 数据进行传递，调用通用返回类创建新的 BaseResponse

    /**
     * 成功
     * @param data 泛型数据
     * @return 基本返回类
     * @param <T> 通用数据类型
     */
    public static<T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    /**
     * 失败
     * @param errorCode 错误码
     * @return 返回封装好的错误码、错误信息、信息描述
     */
    public static BaseResponse error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 失败
     * @param code 自定义错误码
     * @param message 自定义错误信息
     * @param description 自定义信息描述
     * @return 基本返回类
     */
    public static BaseResponse error(int code, String message, String description) {
        return new BaseResponse(code, null, message, description);
    }
    /**
     * 失败
     * @param errorCode 错误码
     * @param message 自定义错误信息
     * @param description 自定义信息描述
     * @return 基本返回类
     */
    public static BaseResponse error(ErrorCode errorCode, String message, String description) {
        return new BaseResponse(errorCode.getCode(),null,  message, description);
    }

    /**
     * 失败
     * @param errorCode 错误码
     * @param description 自定义错误信息描述
     * @return 基本返回类
     */
    public static BaseResponse error(ErrorCode errorCode, String description) {
        return new BaseResponse(errorCode.getCode(), errorCode.getMessage(), description);
    }

}

