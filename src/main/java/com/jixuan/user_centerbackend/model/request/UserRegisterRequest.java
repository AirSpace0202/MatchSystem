package com.jixuan.user_centerbackend.model.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户注册请求体,作为数据传输对象，接收前端传来的请求，将前端传来的 json 数据进行数据绑定，自动赋值给函数中的变量
 */
@Data
public class UserRegisterRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;            // 序列化 id ,可以持久化到硬盘

    private String userAccount;
    private String userPassword;
    private String checkPassword;
}
