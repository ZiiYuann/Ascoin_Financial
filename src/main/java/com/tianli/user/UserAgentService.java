package com.tianli.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianli.agent.team.AgentTeamService;
import com.tianli.agent.team.mapper.AgentTeam;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.role.annotation.GrcCheckModular;
import com.tianli.user.logs.UserIpLogService;
import com.tianli.user.mapper.User;
import com.tianli.user.mapper.UserStatus;
import com.tianli.user.referral.UserReferralService;
import com.tianli.user.referral.mapper.UserReferral;
import com.tianli.user.statistics.UserStatisticsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author wangqiyun
 * @Date 2019-11-13 10:59
 */

@Service
public class UserAgentService {

    /**
     * 邀请用户更新团队信息
     *
     * @param referralCode 邀请码
     * @param uid          用户id
     */
    public void syncAgentTeam(String referralCode, long uid) {
        User byReferralCode = userService.getByReferralCode(referralCode);
        if (Objects.nonNull(byReferralCode) && Objects.equals(byReferralCode.getStatus(), UserStatus.enable)) {
            // 增加邀请记录
            LocalDateTime now = LocalDateTime.now();
            UserReferral one = userReferralService.getOne(new LambdaQueryWrapper<UserReferral>().eq(UserReferral::getId, uid));
            if (Objects.nonNull(one)) ErrorCodeEnum.throwException("重复设置邀请码");
            userReferralService.save(UserReferral.builder()
                    .create_time(now).id(uid)
                    .referral(referralCode)
                    .referral_id(byReferralCode.getId())
                    .referral_username(byReferralCode.getUsername())
                    .build());
            userIpLogService.updateBehaviorId(GrcCheckModular.邀请绑定, uid);
            int changeNum = 1;
            userStatisticsService.incrementReferralNum(byReferralCode.getId(), changeNum);

            LinkedList<Long> list = userReferralService.userReferralChain(byReferralCode.getId());
//            LinkedList<Agent> agents = agentService.agentChain(byReferralCode.getId());
//            if (CollectionUtils.isEmpty(list)) {
//                agentTeamList.add(AgentTeam.builder().referral_id(byReferralCode.getId()).uid(uid).referral_time(now).build());
//                //增加团队人数
//                userStatisticsService.incrementTeamNum(byReferralCode.getId(), changeNum);
//            } else {
//                List<AgentTeam> agentTeams = agents.stream().map(e -> AgentTeam.builder().referral_id(e.getId()).referral_time(now).uid(uid).build()).collect(Collectors.toList());
//                //增加团队人数
//                userStatisticsService.incrementTeamNumByIds(agents.stream().map(Agent::getId).collect(Collectors.toList()), changeNum);
//                agentTeamList.addAll(agentTeams);
//            }
            List<AgentTeam> agentTeams = list.stream().map(e -> AgentTeam.builder().referral_id(e).referral_time(now).uid(uid).build()).collect(Collectors.toList());
            //增加团队人数
            userStatisticsService.incrementTeamNumByIds(list, changeNum);
//                agentTeamList.addAll(agentTeams);
            //新增team数据
            agentTeamService.saveList(agentTeams);
        } else {
            ErrorCodeEnum.INVALID_REFERRAL_CODE.throwException();
        }
    }

    @Resource
    private UserService userService;

    @Resource
    private UserIpLogService userIpLogService;

    @Resource
    private UserStatisticsService userStatisticsService;
    @Resource
    private UserReferralService userReferralService;
    @Resource
    private AgentTeamService agentTeamService;
}
