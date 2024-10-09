package com.jixuan.user_centerbackend.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用删除请求参数
 *
 * @author JxZhang
 */
@Data
public class DeleteRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 930663913180668784L;
    private long id;
}
