package com.tianli.team.controller;

import com.tianli.agent.AgentService;
import com.tianli.agent.mapper.Agent;
import com.tianli.agent.team.AgentTeamService;
import com.tianli.common.init.RequestInitService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.team.TeamService;
import com.tianli.team.mapper.TeamAgentPageDTO;
import com.tianli.team.mapper.TeamReferralPageDTO;
import com.tianli.tool.MapTool;
import com.tianli.tool.time.TimeTool;
import com.tianli.user.UserService;
import com.tianli.user.mapper.User;
import com.tianli.user.referral.UserReferralService;
import com.tianli.user.statistics.UserStatisticsService;
import com.tianli.user.statistics.mapper.UserStatistics;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * <p>
 * 我的团队 前端控制器
 * </p>
 *
 * @author hd
 * @since 2020-12-10
 */
@RestController
@RequestMapping("/team")
public class TeamController {

    @Resource
    private AgentService agentService;

    @Resource
    private UserReferralService userReferralService;

    @Resource
    private UserStatisticsService userStatisticsService;

    @Resource
    private AgentTeamService agentTeamService;

    @Resource
    private TeamService teamService;

    @Resource
    private RequestInitService requestInitService;

    @Resource
    private UserService userService;

    /**
     * @param type 1: 直邀  2:下级代理
     */
    @GetMapping("/page")
    public Result page(@RequestParam(value = "type",defaultValue = "1") Integer type,
                       @RequestParam(value = "page",defaultValue = "1") Integer page,
                       @RequestParam(value = "size",defaultValue = "10") Integer size){
        Long uid = requestInitService.uid();
        MapTool map = getTeam(uid, type, page, size);
        return Result.instance().setData(map);
    }

    /**
     * @param type 1: 直邀  2:下级代理
     */
    @GetMapping("/sub/page")
    public Result subPage(@RequestParam(value = "subUid") Long subUid,
                          @RequestParam(value = "type",defaultValue = "2") Integer type,
                          @RequestParam(value = "page",defaultValue = "1") Integer page,
                          @RequestParam(value = "size",defaultValue = "10") Integer size){
        //获取当前登录用户的uid
        Long uid = requestInitService.uid();
        User user = userService._get(subUid);
        if(Objects.isNull(user)){
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }

        LinkedList<Long> list = userReferralService.userReferralChain(uid);
        if (!list.contains(uid)){
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        MapTool map = getTeam(subUid, 1, page, size);
        return Result.instance().setData(map);
    }


    private MapTool getTeam(long id,Integer type,Integer page,Integer size){
        CompletableFuture<Map<String, Object>> asyncCalculation = CompletableFuture.supplyAsync(() -> {
            Map<String, LocalDateTime> thisDay = TimeTool.thisDay();
            int day = agentTeamService.countWithInterval(id, thisDay.get("start"), thisDay.get("end"));
            Map<String, LocalDateTime> thisWeek = TimeTool.thisWeekMondayToSunday();
            int week = agentTeamService.countWithInterval(id, thisWeek.get("start"), thisWeek.get("end"));
            Map<String, LocalDateTime> thisMonth = TimeTool.thisMonth();
            int month = agentTeamService.countWithInterval(id, thisMonth.get("start"), thisMonth.get("end"));
            return MapTool.Map().put("month", month).put("week", week).put("day", day);
        });
        MapTool map = MapTool.Map();
        //1.直邀；2.下级代理
        if(Objects.equals(type, 2)){
            Agent agent = agentService.getById(id);
            if(Objects.isNull(agent)){
                ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
            }
            //判断当前用户是否是超级代理商
//            map.put("super_agent",agent.getSuper_agent());
            List<TeamAgentPageDTO> agentPage = teamService.teamAgentPage(id, page, size);
            List<TeamAgentPageVO> vos = agentPage.stream().map(TeamAgentPageVO::trans).collect(Collectors.toList());
            map.put("list", vos);
        } else {
            List<TeamReferralPageDTO> referralPage = teamService.teamReferralPage(id, page, size);
            List<TeamReferralPageVO> vos = referralPage.stream().map(TeamReferralPageVO::trans).collect(Collectors.toList());
            map.put("list", vos);
        }

        UserStatistics userStatistics = userStatisticsService.get(id);
        if(Objects.isNull(userStatistics)){
            ErrorCodeEnum.throwException("用户统计信息缺失");
        }
        Map<String, Object> task = null;
        try {
            task = asyncCalculation.get();
        } catch (Exception e) {
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }
        task.put("total", userStatistics.getTeam_number());
        return map.put("statistics", task);
    }

}

