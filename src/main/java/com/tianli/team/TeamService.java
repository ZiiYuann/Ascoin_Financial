package com.tianli.team;

import com.tianli.team.mapper.TeamAgentPageDTO;
import com.tianli.team.mapper.TeamMapper;
import com.tianli.team.mapper.TeamReferralPageDTO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class TeamService {
    @Resource
    private TeamMapper teamMapper;

    public List<TeamReferralPageDTO> teamReferralPage(Long uid, Integer page, Integer size) {
        return teamMapper.selectTeamReferralPage(uid, Math.max((page - 1) * size, 0), size);
    }

    public List<TeamAgentPageDTO> teamAgentPage(Long uid, Integer page, Integer size) {
        return teamMapper.selectTeamAgentPage(uid, Math.max((page - 1) * size, 0), size);
    }
}
