package com.jixuan.user_centerbackend.service;

import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.jixuan.user_centerbackend.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    public void testAddUser() {
        User user = new User();
        user.setUserName("jixuanzhang");
        user.setUserAccount("123");
        user.setUserPassword("12345678");
        user.setAvatarUrl("https://cn.bing.com/images/search?q=logo%E5%9B%BE%E7%89%87&FORM=IQFRBA&id=22831101BAB5B3D5641DAEACD8F602B665118014");
        user.setGender(0);
        user.setPhone("123");
        user.setEmail("123");

        boolean result = userService.save(user);
        System.out.println(user.getId());
        Assertions.assertTrue(result);
    }

    @Test
    public void testRegister() {
        // 密码为空
        String userAccount = "Jixuan";
        String userPassword = "";
        String checkPassword = "123456";
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1, result);                // 添加断言，希望 result 为-1，因为上面是各种错误的测试

        // 密码小于8 位
        userAccount = "Jixuan";
        userPassword = "123456";
        result = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1, result);

        // 含有特殊字符
        userAccount = "Ji xuan";
        userPassword = "12345678";
        result = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1, result);

        // 校验密码与密码不相同
        checkPassword = "123456789";
        result = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1, result);

        // 账号重复
        userAccount = "jixuanzhang";
        checkPassword = "12345678";
        result = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1, result);

        // 正确，希望能够插入数据库
        userAccount = "zhangjixuan";
        result = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertNotEquals(-1, result);
    }

    @Test
    public void searchByTags() {
        List<String> tagNameList = Arrays.asList("java", "python");
        List<User> userList = userService.searchUsersByTags(tagNameList);
        Assertions.assertNotNull(userList);
    }
}
