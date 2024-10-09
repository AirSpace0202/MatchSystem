package com.jixuan.user_centerbackend.service;

import com.jixuan.user_centerbackend.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@SpringBootTest
public class InsertUsersTest {

    @Resource
    private UserService userService;

    // 创建线程池，核心线程数为 10，最大线程数为 1000，当线程空闲时间超过 10000 分钟会进行回收，阻塞队列容量为 10000
    private ExecutorService executorService = new ThreadPoolExecutor(10, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));

    /**并发
     * 批量插入用户
     */
    @Test
    public void doInsertUsers() {
        StopWatch stopWatch = new StopWatch();          // 用于记录时间
        stopWatch.start();                      // 开始计时
        final int INSERT_NUM = 100000;
        int j = 0;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();   // 存储异步任务的 CompletableFuture 对象
        for (int i = 0; i < 10; i ++ ) {                // 分成 10 个线程池进行插入
            List<User> userList = new ArrayList<>();
            while (true) {
                j++;
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
                userList.add(user);
                if (j % 10000 == 0) {               // 每批包含 10000 个用户数据
                    break;
                }
            }
            // 创建一个异步任务
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("threadName: " + Thread.currentThread().getName());
                userService.saveBatch(userList, 10000);
            }, executorService);
            futureList.add(future);
        }
        // 等待所有异步任务完成，并进行类型转换，转换成 CompletableFuture 数组
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();

        stopWatch.stop();                   // 停止计时（ms 为单位）
        System.out.println(stopWatch.getTotalTimeMillis());
    }

}
