package com.jixuan.user_centerbackend.model.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户加入队伍请求体
 *
 * @author JxZhang
 */
@Data
public class TeamJoinRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -1951673617554780753L;

    /**
     * 队伍 id
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;
}

