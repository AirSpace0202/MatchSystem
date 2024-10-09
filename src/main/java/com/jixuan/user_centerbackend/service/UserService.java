package com.jixuan.user_centerbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jixuan.user_centerbackend.model.domain.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author JxZhang
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2024-09-04 10:57:26
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册功能
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @return 新注册的用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录功能，登录后返回脱敏后的用户信息
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param request 前端传来的请求
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 进行用户脱敏，返回脱敏后安全的用户信息
     * @param originUser 未脱敏的用户
     * @return  安全的用户信息
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销功能
     * @param request 前端传来的请求
     * @return 1 表示注销成功
     */
    int userLogout(HttpServletRequest request);

    /**
     * 根据标签列表查询所对应的用户列表
     * @param tagNameList 标签列表
     * @return 查询出的用户列表
     */
    List<User> searchUsersByTags(List<String> tagNameList);

    /**
     * 更新用户信息
     * @param user 用户
     * @return 整数值，表示更新成功
     */
    int updateUser(User user, User loginUser);

    /**
     * 获取当前登录用户
     * @return 用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     * @param request 前端请求
     * @return 是否为管理员
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     * @param loginUser 当前登录用户
     * @return 是否为管理员
     */
    boolean isAdmin(User loginUser);

    /**
     * 匹配用户
     * @param num 数量
     * @param loginUser 当前登录用户
     * @return 用户列表
     */
    List<User> matchUsers(long num, User loginUser);

}
