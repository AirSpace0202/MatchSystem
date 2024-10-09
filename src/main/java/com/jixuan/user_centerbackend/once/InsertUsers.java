package com.jixuan.user_centerbackend.once;

import com.jixuan.user_centerbackend.mapper.UserMapper;
import com.jixuan.user_centerbackend.model.domain.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

@Component                      // 添加此注解能让 Spring 将识别，进行加载
public class InsertUsers {

    @Resource
    private UserMapper userMapper;

    /**
     * 批量插入用户
     */
    // @Scheduled(fixedDelay = 5000)
    public void doInsertUsers() {
        StopWatch stopWatch = new StopWatch();          // 记录插入数据的时间
        stopWatch.start();                      // 开始任务
        final int INSERT_NUM = 1000;

        for (int i = 0; i < INSERT_NUM; i ++ ) {
            User user = new User();
            user.setUserName("假用户");
            user.setUserAccount("fakeJiXuan");
            user.setAvatarUrl("https://tse3.mm.bing.net/th/id/OIP.MEmVm4wGyALRbVNZdc5zDQAAAA?rs=1&pid=ImgDetMain");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("123");
            user.setEmail("123@qq.com");
            user.setTags("[]");
            user.setUserStatus(0);
            user.setUserRole(0);
            // userMapper.insert(user);
        }
        stopWatch.stop();                   // 结束任务并打印总时间（ms 为单位）
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
