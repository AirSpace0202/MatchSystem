package com.jixuan.user_centerbackend.service;
import java.util.Date;


import com.jixuan.user_centerbackend.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

@SpringBootTest
public class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 编写增删改查测试方法
     */
    @Test
    void test() {
        ValueOperations valueOperations = redisTemplate.opsForValue();

        // 增
        valueOperations.set("jixuanString", "dog");
        valueOperations.set("jixuanInt", 1);
        valueOperations.set("jixuanDouble", 2.0);
        User user = new User();
        user.setId(0L);
        user.setUserName("jixuan");
        valueOperations.set("jixuanUser", user);

        // 查
        Object jixuan = valueOperations.get("jixuanString");
        Assertions.assertEquals("dog", (String) jixuan);
        jixuan = valueOperations.get("jixuanInt");
        Assertions.assertEquals(1, (Integer) jixuan);
        jixuan = valueOperations.get("jixuanDouble");
        Assertions.assertEquals(2.0, (Double) jixuan);
        System.out.println(valueOperations.get("jixuanUser"));

        // 删
        valueOperations.set("jixuanString", "dog");
        redisTemplate.delete("jixuanString");
    }
}
