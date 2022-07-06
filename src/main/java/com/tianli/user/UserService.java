package com.tianli.user;

import com.tianli.common.CommonFunction;
import com.tianli.common.init.RequestInitService;
import com.tianli.common.lock.RedisLock;
import com.tianli.currency.CurrencyService;
import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.currency.mapper.Currency;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.user.mapper.User;
import com.tianli.user.mapper.UserIdentity;
import com.tianli.user.mapper.UserMapper;
import com.tianli.user.mapper.UserStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @Author wangqiyun
 * @Date 2019-11-13 10:59
 */

@Service
public class UserService {
    public User _get(long id) {
        return userMapper.get(id);
    }

    public User _getByUsername(String username) {
        return userMapper.getByUsername(username);
    }

    public User _getByHashKey(String hashKey) {
        return userMapper.getByHashKey(hashKey);
    }

    @Transactional
    public User reg(String username) {
        User user = new User();
        long id = CommonFunction.generalId();
        user.setId(id);
        user.setCreate_time(LocalDateTime.now());
//        String username = Randoms.alphaString();
//        User exist = _getByUsername(username);
//        while (Objects.nonNull(exist)){
//            username = Randoms.alphaString();
//            exist = _getByUsername(username);
//        }
        user.setUsername(username);
//        user.setHash_key(hashKey);
        String referral_code = String.valueOf(ThreadLocalRandom.current().nextInt(10000000, 99999999));
        while (Objects.nonNull(getByReferralCode(referral_code))) {
            referral_code = String.valueOf(ThreadLocalRandom.current().nextInt(10000000, 99999999));
        }
        user.setReferral_code(referral_code);
        user.setIdentity(UserIdentity.normal);
        user.setStatus(UserStatus.enable);
        userMapper.insert(user);
        boolean saveCurrency = currencyService.save(Currency.builder().id(CommonFunction.generalId()).uid(id).type(CurrencyTypeEnum.normal).build());
        if (!saveCurrency) ErrorCodeEnum.SYSTEM_ERROR.throwException();
        return user;
    }

    public User _my() {
        return _get(requestInitService.uid());
    }

    public User my() {
        User user = _my();
        if(user == null){
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        return user;
    }

    @Cacheable(value = "UserService_status", key = "#id")
    public boolean status(long id) {
        User user = userMapper.selectById(id);
        return user != null && user.getStatus().equals(UserStatus.enable);
    }

    public List<User> getByIds(List<Long> uidList) {
        List<String> idStringList = uidList.stream().map(String::valueOf).collect(Collectors.toList());
        String inSqlString = String.join(",", idStringList);
        return userMapper.getByIds(inSqlString);
    }

    /**
     * 记录访问信息
     */
    public void last(long id, String ip) {
        userMapper.last(id, LocalDateTime.now(), ip);
    }

    public User getByReferralCode(String code) {
        if(StringUtils.isBlank(code)){
            return null;
        }
        return userMapper.selectByReferralCode(code);
    }
    @Resource
    private RedisLock redisLock;
    @Resource
    private RequestInitService requestInitService;
    @Resource
    private UserMapper userMapper;
    @Resource
    private CurrencyService currencyService;

    public boolean negateStatus(long id) {
        User user = _get(id);
        if(Objects.isNull(user)){
            return false;
        }
        return userMapper.updateStatus(id, user.getStatus().negate()) > 0;
    }

    public boolean updateIdentityById(UserIdentity identity, long id) {
        return userMapper.updateIdentityById(id, identity) > 0;
    }

    public void updateRobot(Long id, RobotUpdateDTO robotUpdate) {
        long update = userMapper.updateRobotConfig(id,
                robotUpdate.getAuto_count(),
                robotUpdate.getAuto_amount(),
                robotUpdate.getInterval_time(),
                robotUpdate.getWin_rate(),
                robotUpdate.getProfit_rate());
        if(update <= 0) ErrorCodeEnum.SYSTEM_ERROR.throwException();
    }

    public boolean decrementCount(long uid) {
        return userMapper.decrementCount(uid) > 0;
    }

    public void resetAutoCount() {
        userMapper.resetAutoCount();
    }

    public void updateNode(Long id, String node) {
        long update = userMapper.updateNode(id, node);
        if(update <= 0) ErrorCodeEnum.SYSTEM_ERROR.throwException();
    }

    public void updateUserType(Long id, Integer user_type) {
        long update = userMapper.updateType(id, user_type);
        if(update <= 0) ErrorCodeEnum.SYSTEM_ERROR.throwException();
    }

    public void updateCreditScore(Long id, Integer credit_score,String adjust_reason) {
        long update = userMapper.updateCreditScore(id,credit_score, adjust_reason);
        if(update <= 0) ErrorCodeEnum.SYSTEM_ERROR.throwException();
    }
}
