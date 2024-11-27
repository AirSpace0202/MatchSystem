package com.jixuan.user_centerbackend.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jixuan.user_centerbackend.common.BaseResponse;
import com.jixuan.user_centerbackend.common.ErrorCode;
import com.jixuan.user_centerbackend.common.ResultUtils;
import com.jixuan.user_centerbackend.exception.BusinessException;
import com.jixuan.user_centerbackend.model.domain.User;
import com.jixuan.user_centerbackend.model.request.UserLoginRequest;
import com.jixuan.user_centerbackend.model.request.UserRegisterRequest;
import com.jixuan.user_centerbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.jixuan.user_centerbackend.constant.UserConstant.USER_LOGIN_STATE;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173", "https://match-system-frontend-lsvctc0g8-airspaces-projects-1a65db43.vercel.app/" }, allowCredentials = "true")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            // return ResultUtils.error(ErrorCode.PARAMS_ERROR);
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取前端 json 数据中的数据绑定到 UserRegisterRequest 中的变量
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();

        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        long result =  userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);                         // 调用返回工具类的 success 的方法
    }

    @PostMapping("/login")
    // 需要获取到 HttpServletRequest 对象来操作 session
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        User user =  userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        int result =  userService.userLogout(request);
        return ResultUtils.success(result);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(@NotNull HttpServletRequest request) {

        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);           // 获取当前用户登录态并进行数据类型转换
        User currentUser = (User) userObj;
        if(currentUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "当前未登录");
        }
        long userId = currentUser.getId();              // 通过 id 进行数据库查询，查询出当前用户信息并返回脱敏后的用户信息
        User user = userService.getById(userId);

        User safetyUser =  userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String userName, HttpServletRequest request) {

        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(userName)) {
            queryWrapper.like("userName", userName);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list =  userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());     // 先将 userList 转换为数据流，进行脱敏后再转换成 List
        return ResultUtils.success(list);
    }

    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageSize, long pageNum, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        // 根据用户 id 生成 redis 键，格式为 jixuan:user:recommend:%s
        String redisKey = String.format("jixuan:user:recommend:%s", loginUser.getId());
        // 对 redis 进行值操作
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        // 如果有缓存，直接读取缓存
        Page<User> userPage = (Page<User>) redisTemplate.opsForValue().get(redisKey);
        if (userPage != null) {
            return ResultUtils.success(userPage);
        }
        // 无缓存，从数据库中读取
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        userPage = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
        // 写缓存
        try {
            // 设置缓存失效时间为 30 秒，30 秒后 redis key 会自动删除
            valueOperations.set(redisKey, userPage, 30000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.info("redis set key error", e);
        }
        return ResultUtils.success(userPage);
    }

    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        int result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUsers(@RequestBody long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            return null;
        }
        if (id <= 0) {
            return null;
        }
        boolean result =  userService.removeById(id);
        return ResultUtils.success(result);
    }

    /**
     * 匹配用户
     * @param num 匹配的用户数量
     * @param request 前端请求
     * @return 用户列表
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long num, HttpServletRequest request) {
        if (num <= 0 || num >= 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.matchUsers(num, loginUser));
    }

}
