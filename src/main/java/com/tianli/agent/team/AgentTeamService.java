package com.tianli.agent.team;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.agent.team.mapper.AgentTeam;
import com.tianli.agent.team.mapper.AgentTeamMapper;
import com.tianli.tool.time.TimeTool;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 代理商团队表(缓存表) 服务实现类
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@Service
public class AgentTeamService extends ServiceImpl<AgentTeamMapper, AgentTeam> {

    /**
     * 批量增加, 不会出现异常, 即使可能会数据不一致
     * @param agentTeamList 需要添加的关系集合
     */
    public void saveList(List<AgentTeam> agentTeamList) {
        if(CollectionUtils.isEmpty(agentTeamList)){
            return;
        }
        agentTeamList.forEach(e -> {
            try {
                baseMapper.insert(e);
            } catch (Exception ex) {
                // do nothing
            }
        });
    }

    /**
     * 批量替换
     * @param agentTeamList 需要替换的关系集合
     */
    public void replaceBatch(List<AgentTeam> agentTeamList) {
        if(CollectionUtils.isEmpty(agentTeamList)){
            return;
        }
        baseMapper.replaceList(agentTeamList);
    }

    public int countWithInterval(Long uid, LocalDateTime start, LocalDateTime end) {
        return baseMapper.selectCountWithInterval(uid, start, end);
    }

    public List<Long> lowAgentNotMyReferral(Long uid) {
        return baseMapper.selectLowAgentNotMyReferral(uid);
    }

    public boolean removeByReferralId(long id) {
        return baseMapper.deleteByReferralId(id) > 0;
    }

    public List<Map<String, Object>> statTeamNumDaily(Long uid) {
        Map<String, String> last15Days = TimeTool.theLast15DaysStr();
        return baseMapper.selectTeamStatNumDaily(uid, last15Days.get("today"), last15Days.get("pastDays"));
    }

    public List<Map<String, Object>> statTeamIncrementDaily(Long uid, String today, String startDate, int totalNum,  Integer page, Integer size) {
        return baseMapper.selectTeamIncrementStatDaily(uid, today ,startDate, totalNum, (page-1)*size, size);
    }
}
