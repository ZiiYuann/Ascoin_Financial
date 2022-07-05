package com.tianli.user.statistics;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.init.RequestInitService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.user.statistics.mapper.UserStatistics;
import com.tianli.user.statistics.mapper.UserStatisticsMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户统计表 服务实现类
 * </p>
 *
 * @author hd
 * @since 2020-12-08
 */
@Service
public class UserStatisticsService extends ServiceImpl<UserStatisticsMapper, UserStatistics> {

    public void incrementReferralNum(long id, int num) {
        get(id);
        int referralNum = userStatisticsMapper.incrementReferralNum(id, num);
        if (referralNum <= 0) ErrorCodeEnum.SYSTEM_ERROR.throwException();
    }

    public void incrementTeamNum(long id, int num) {
        get(id);
        int teamNum = userStatisticsMapper.incrementTeamNum(id, num);
        if (teamNum <= 0) ErrorCodeEnum.SYSTEM_ERROR.throwException();
    }

    public void incrementTeamNumByIds(List<Long> ids, int num) {
        if(CollectionUtils.isEmpty(ids)){
            return;
        }
        List<String> idStringList = ids.stream().map(String::valueOf).collect(Collectors.toList());
        String inSqlString = String.join(",", idStringList);
        int teamNumByIds = userStatisticsMapper.incrementTeamNumByIds(inSqlString, num);
        if (teamNumByIds <= 0) ErrorCodeEnum.SYSTEM_ERROR.throwException();
    }

    public void incrementTeamAndReferralNum(long id, int num) {
        get(id);
        int teamAndReferralNum = userStatisticsMapper.incrementTeamAndReferralNum(id, num);
        if (teamAndReferralNum <= 0) ErrorCodeEnum.SYSTEM_ERROR.throwException();
    }

    public void incrementMyAmount(long uid, BigInteger betAmount) {
        get(uid);
        int incrementMyAmount = userStatisticsMapper.incrementMyAmount(uid, betAmount);
        if (incrementMyAmount <= 0) ErrorCodeEnum.SYSTEM_ERROR.throwException();
    }

    public void incrementRebate(long uid, BigInteger amount) {
        get(uid);
        int incrementMyAmount = userStatisticsMapper.incrementRebate(uid, amount);
        if (incrementMyAmount <= 0) ErrorCodeEnum.SYSTEM_ERROR.throwException();
    }

    public void incrementTeamAmount(long uid, BigInteger betAmount) {
        get(uid);
        int incrementMyAmount = userStatisticsMapper.incrementTeamAmount(uid, betAmount);
        if (incrementMyAmount <= 0) ErrorCodeEnum.SYSTEM_ERROR.throwException();
    }

    public void incrementTeamAmountByIds(List<Long> ids, BigInteger betAmount) {
        if(CollectionUtils.isEmpty(ids)){
            return;
        }
        List<String> idStringList = ids.stream().map(String::valueOf).collect(Collectors.toList());
        String inSqlString = String.join(",", idStringList);
        int incrementMyAmount = userStatisticsMapper.incrementTeamAmountByIds(inSqlString, betAmount);
        if (incrementMyAmount <= 0) ErrorCodeEnum.SYSTEM_ERROR.throwException();
    }

    public UserStatistics get() {
        Long uid = requestInitService.uid();
        return get(uid);
    }

    public UserStatistics get(long uid) {
        UserStatistics byId = super.getById(uid);
        if(byId != null){
            return byId;
        }
        return saveOne(uid);
    }

    public UserStatistics saveOne(long uid) {
        UserStatistics userStatistics = UserStatistics.builder().id(uid).build();
        super.save(userStatistics);
        return userStatistics;
    }

    @Resource
    private UserStatisticsMapper userStatisticsMapper;

    @Resource
    private RequestInitService requestInitService;
}
