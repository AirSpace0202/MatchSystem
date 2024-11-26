package com.jixuan.user_centerbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jixuan.user_centerbackend.common.ErrorCode;
import com.jixuan.user_centerbackend.exception.BusinessException;
import com.jixuan.user_centerbackend.mapper.UserMapper;
import com.jixuan.user_centerbackend.model.domain.User;
import com.jixuan.user_centerbackend.service.UserService;
import com.jixuan.user_centerbackend.utils.AlgorithmUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.jixuan.user_centerbackend.constant.UserConstant.ADMIN_ROLE;
import static com.jixuan.user_centerbackend.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author JxZhang
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2024-09-04 10:57:26
*/
@Service
@Slf4j              // 生成日志文件
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    @Resource
    private UserMapper userMapper;

    private static final String SALT = "jixuan";

    /**
     * 用户注册
     * @param userAccount 账户名
     * @param userPassword  密码
     * @param checkPassword 校验密码，和密码相同
     * @return 用户 id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {

        // 1、校验参数
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");          // 详细描述为用户账号过短
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");          // 错误信息为请求参数错误，详细描述为用户密码过短
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入密码不相同");
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            return -1;
        }
        // 密码加密，md5 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        boolean saveResult = this.save(user);           // this 指的是 UserService
        if (!saveResult) {
            return -1;
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {

        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        if (userAccount.length() < 4 || userPassword.length() < 8) {
            return null;
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return null;
        }
        // 对传来的密码进行 md5 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 账户要已经存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);   // 查询加密后的密码
        User user = userMapper.selectOne(queryWrapper);     // 查找出数据库中账号密码相对应的账号
        if (user == null) {
            return null;
        }
        // 进行用户脱敏
        User safetyUser = getSafetyUser(user);
        // 记录用户登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;

    }

    @Override
    public User getSafetyUser(User originUser) {

        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUserName(originUser.getUserName());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setTags(originUser.getTags());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setUserRole(originUser.getUserRole());
        return safetyUser;
    }

    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);     // 直接移除登录态键
        return 1;
    }

    /**
     * 根据标签搜索用户（内存版本）
     * @param tagNameList 标签列表
     * @return 搜到的脱敏用户列表
     */
    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签列表为空");
        }
        // 1、先查询所有用户，查询出的结果会加载到内存中
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        // 2、在内存中判断是否包含要求的标签
        return userList.stream().filter(user -> {           // user 代表 userList 中的遍历变量，代表 userList 中的每一个 User 对象，过滤掉不满足条件的 user
            String tagStr = user.getTags();             // tags 字段存的是 json 数组
            if (StringUtils.isBlank(tagStr)) {
                return false;
            }
            // 将 json 格式的数据反序列化成一个 Set<String> 的数据类型
            Set<String> tempTagNameSet = gson.fromJson(tagStr, new TypeToken<Set<String>>(){}.getType());
            // 将反序列化得到的结果封装为一个 Optional 对象，如果对象为空，那么返回一个新的 HashSet 对象，可以处理 NPE 异常
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for (String tagName: tagNameList) {
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());           // 进行脱敏处理
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return (User) userObj;
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {                       // 用户鉴权
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public List<User> matchUsers(long num, User loginUser) {
        // 只在有标签的用户列表中循环判断，同时只查需要的数据(id, tags)，同时 tags 不为空
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "tags");
        queryWrapper.isNotNull("tags");
        List<User> userList = this.list(queryWrapper);
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {}.getType());
        // 将用户列表的用户 => 相似度进行排序
        List<Pair<User, Long>> list = new ArrayList<>();
        // 依次计算所有用户和当前用户的相似度
        for (User user : userList) {
            // 依次获取 userList 中的 user 的标签，和登录用户的标签进行比较
            String userTags = user.getTags();
            if (StringUtils.isBlank(userTags) || Objects.equals(user.getId(), loginUser.getId())) {        // 如果用户未设置标签或用户为自己则跳过到下一个用户
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            // 进行标签匹配
            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
            list.add(new Pair<>(user, distance));
        }
        // 按编辑距离从小到大排序 user => distance
        List<Pair<User, Long>> topUserPairList = list.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .limit(num)
                .collect(Collectors.toList());
        // 按照 distance 排序后的 userId 列表
        List<Long> userIdList = topUserPairList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIdList);
        // id => List<User>
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper)
                .stream()
                .map(this::getSafetyUser)
                .collect(Collectors.groupingBy(User::getId));
        List<User> finalUserList = new ArrayList<>();
        // 由于每个 id 只对应一个用户，所以通过 id 获取之后直接 get(0) 就行
        for (Long userId : userIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }

    @Override
    public int updateUser(User user, User loginUser) {
        long userId = user.getId();
        // 如果是管理员，可以更新任何用户
        // 如果不是管理员，只能更新自己的信息
        if (!isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);
    }

    /**
     * 根据标签搜索用户（SQL 版本）
     * @param tagNameList 标签列表
     * @return 搜到的脱敏用户列表
     */
    @Deprecated
    private List<User> searchUsersByTagsBySQL(List<String> tagNameList) {

        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签列表为空");    // 参数错误
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();     // 进行数据库查询
        // 拼接 and 查询，根据 like 进行模糊查询，不断叠加 queryWrapper
        for (String tagName: tagNameList) {
            queryWrapper = queryWrapper.like("tags", tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        // 返回脱敏后的用户列表，this 代表当前类的实例，用于调用当前类中的 getSafetyUser 方法
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

}




