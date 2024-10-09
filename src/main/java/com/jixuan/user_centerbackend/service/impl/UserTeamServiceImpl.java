package com.jixuan.user_centerbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jixuan.user_centerbackend.model.domain.UserTeam;
import com.jixuan.user_centerbackend.service.UserTeamService;
import com.jixuan.user_centerbackend.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author JxZhang
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2024-09-17 19:13:44
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




