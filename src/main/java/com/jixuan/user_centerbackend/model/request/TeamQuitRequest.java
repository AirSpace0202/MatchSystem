package com.jixuan.user_centerbackend.model.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户退出队伍请求体
 *
 * @author JxZhang
 */
@Data
public class TeamQuitRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -8710987050666189391L;

    /**
     * 队伍 id
     */
    private Long teamId;

}

