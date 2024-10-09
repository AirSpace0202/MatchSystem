package com.jixuan.user_centerbackend.job;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jixuan.user_centerbackend.model.domain.User;
import com.jixuan.user_centerbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *  缓存预热任务
 *
 * @author JxZhang
 */
@Slf4j
@Component
public class PreCacheJob {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private UserService userService;
    @Resource
    private RedissonClient redissonClient;

    // 重点用户
    private List<Long> mainUserList = Arrays.asList(7L);

    // 每天执行，预热推荐用户
    @Scheduled(cron = "0 18 17 * * ? ")           // 表达式可以网上查找
    public void doCacheRecommendUser() {
        RLock lock = redissonClient.getLock("jixuan:precachejob:docache:lock");
        try {
            // 只有一个线程能获取到锁，参数中第一个为等待时间，表示线程多久进行一次抢锁，第二个参数为锁的过期时间，设为 -1 表示利用看门狗机制进行自动续期
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                for (Long userId: mainUserList) {
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);    // 将查询到的数据进行展示，缓存 1 页，20 条数据
                    String redisKey = String.format("jixuan:user:recommend:%s", userId);
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    // 写缓存
                    try {
                        valueOperations.set(redisKey, userPage, 30000, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        log.error("redis set key error", e);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error" + e);
        } finally {
            // 只能释放自己的锁，通过判断当前线程 id 是否是原线程 id
             if (lock.isHeldByCurrentThread()) {
                 lock.unlock();
             }
        }
    }
}