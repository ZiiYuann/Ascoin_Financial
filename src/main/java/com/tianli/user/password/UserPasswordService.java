package com.tianli.user.password;

import com.tianli.exception.ErrorCodeEnum;
import com.tianli.user.password.mapper.UserPassword;
import com.tianli.user.password.mapper.UserPasswordMapper;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @Author wangqiyun
 * @Date 2020/3/11 17:12
 */

@Service
public class UserPasswordService {
    public UserPassword get(long id) {
        UserPassword userPassword = userPasswordMapper.get(id);
        if (userPassword == null) {
            userPassword = new UserPassword();
            userPassword.setId(id);
            userPasswordMapper.insert(id);
        }
        return userPassword;
    }

    public void updateLogin(long id, String password) {
        get(id);
        userPasswordMapper.updateLoginPassword(id, passwordEncoder.encode(password));
    }

    public void updatePay(long id, String password) {
        get(id);
        BoundValueOperations<String, Object> boundValueOps = redisTemplate.boundValueOps("resetPayPassword:flag:" + id);
        boundValueOps.set(true, 24, TimeUnit.HOURS);
        userPasswordMapper.updatePayPassword(id, passwordEncoder.encode(password));
    }

    public void checkPayPassword(UserPassword userPassword, String password) {
        BoundValueOperations<String, Object> ops = redisTemplate.boundValueOps("UserPasswordService.checkPassword." + userPassword.getId());
        ops.increment();
        ops.expire(1L, TimeUnit.DAYS);
        Object timesObject = ops.get();
        Long times = null;
        if (timesObject != null) {
            times = Long.valueOf(timesObject.toString());
        }
        if ((times != null && times >= 256) || !_checkPayPassword(userPassword, password))
            ErrorCodeEnum.PASSWORD_ERROR.throwException();
    }

    public void checkLoginPassword(UserPassword userPassword, String password) {
        BoundValueOperations<String, Object> ops = redisTemplate.boundValueOps("UserPasswordService.checkPassword." + userPassword.getId());
        ops.increment();
        ops.expire(1L, TimeUnit.DAYS);
        Object timesObject = ops.get();
        Long times = null;
        if (timesObject != null) {
            times = Long.valueOf(timesObject.toString());
        }
        if ((times != null && times >= 256) || !_checkLoginPassword(userPassword, password))
            ErrorCodeEnum.PASSWORD_ERROR.throwException();
    }

    public boolean _checkPayPassword(UserPassword userPassword, String password) {
        if (StringUtils.isEmpty(userPassword.getPay_password())) return false;
        return passwordEncoder.matches(password, userPassword.getPay_password());
    }

    public boolean _checkLoginPassword(UserPassword userPassword, String password) {
        if (StringUtils.isEmpty(userPassword.getLogin_password())) return false;
        return passwordEncoder.matches(password, userPassword.getLogin_password());
    }


    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private UserPasswordMapper userPasswordMapper;
}
