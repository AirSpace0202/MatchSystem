package com.jixuan.user_centerbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jixuan.user_centerbackend.model.domain.Team;
import com.jixuan.user_centerbackend.model.domain.User;
import com.jixuan.user_centerbackend.model.dto.TeamQuery;
import com.jixuan.user_centerbackend.model.request.TeamJoinRequest;
import com.jixuan.user_centerbackend.model.request.TeamQuitRequest;
import com.jixuan.user_centerbackend.model.request.TeamUpdateRequest;
import com.jixuan.user_centerbackend.model.vo.TeamUserVO;

import java.util.List;

/**
* @author JxZhang
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-09-17 19:12:13
*/
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     * @param team 队伍信息
     * @param loginUser 当前用户
     * @return 队伍 id
     */
    long addTeam(Team team, User loginUser);

    /**
     * 搜索队伍
     * @param teamQuery 队伍查询
     * @param isAdmin 是否为管理员
     * @return 队伍列表
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin);

    /**
     * 更新队伍
     * @param teamUpdateRequest 前端更新请求
     * @param loginUser 当前登录用户
     * @return 是否更新成功
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * 用户加入队伍
     * @param teamJoinRequest 前端请求
     * @param loginUser 当前登录用户
     * @return 是否加入成功
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * 用户退出队伍
     * @param teamQuitRequest 用户退出请求体
     * @param loginUser 当前登录用户
     * @return 是否退出成功
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 用户解散队伍
     * @param id 队伍 id
     * @return 是否解散成功
     */
    boolean deleteTeam(long id, User loginUser);
}
