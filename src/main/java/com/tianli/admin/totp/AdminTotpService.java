package com.tianli.admin.totp;

import com.tianli.admin.AdminAndRoles;
import com.tianli.admin.AdminService;
import com.tianli.admin.mapper.Admin;
import com.tianli.admin.totp.mapper.AdminTotp;
import com.tianli.admin.totp.mapper.AdminTotpMapper;
import com.tianli.common.Constants;
import com.tianli.common.init.RequestInitService;
import com.tianli.exception.ErrorCodeEnum;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import org.javatuples.Pair;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * @Author wangqiyun
 * @Date 2020/6/1 17:53
 */

@Service
public class AdminTotpService {

    public boolean loginVerify(String username, String code) {
        Admin admin = adminService.getByUsername(username);
        if(admin == null) return true;
        if(get(admin.getId()) == null) return true;
        return verify(admin.getId(), code);
    }

    public boolean verify(long uid, String code) {
        AdminTotp userTotp = get(uid);
        if (userTotp == null) return true;
        if (code == null) return false;
        BoundValueOperations<String, String> ops = stringRedisTemplate.boundValueOps(VALID_TIMES + LocalDateTime.now().format(Constants.dateFormatter) + "_" + uid);
        String times = ops.get();
        if (times != null && Long.parseLong(times) > 64) {
            ErrorCodeEnum.throwException("二次校验验证码错误次数过多!请明天再试");
        }
        boolean verify = verify(userTotp.getSecret(), code);
        if (!verify) {
            ops.increment();
            ops.expire(1L, TimeUnit.DAYS);
        }
        return verify;
    }

//    public boolean verify(String code) {
//        return verify(requestInitService.uid(), code);
//    }

    public boolean verify(String secret, String code) {
        DefaultCodeVerifier verifier = new DefaultCodeVerifier(new DefaultCodeGenerator(HashingAlgorithm.SHA512), new SystemTimeProvider());
        return verifier.isValidCode(secret, code);
    }

    public boolean unbind(String code) {
        AdminTotp userTotp = get(adminService.my().getId());
        if (userTotp == null) return true;
        if (!verify(userTotp.getSecret(), code))
            return false;
        return userTotpMapper.deleteById(adminService.my().getId()) > 0;
    }

    public AdminTotp get(long id) {
        return userTotpMapper.selectById(id);
    }

    public Pair<String, String> general(String username, String issuer) {
        String secret = new DefaultSecretGenerator().generate();
        return Pair.with(secret,
                new QrData.Builder().algorithm(HashingAlgorithm.SHA512).secret(secret).issuer(issuer).label(username).build().getUri());
    }


    public boolean bind(String secret, String code) {
        AdminAndRoles admin = adminService.my();
        AdminTotp userTotp = get(admin.getId());
        if (userTotp != null)
            return false;
        if (!verify(secret, code))
            return false;
        return userTotpMapper.insert(AdminTotp.builder().id(admin.getId()).secret(secret).build()) > 0;
    }

    public void clear(long uid) {
        stringRedisTemplate.delete(VALID_TIMES + LocalDateTime.now().format(Constants.dateFormatter) + "_" + uid);
    }


    private static final String VALID_TIMES = "UserTotpService_VALID_TIMES_";
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private AdminTotpMapper userTotpMapper;
    @Resource
    private RequestInitService requestInitService;
    @Resource
    private AdminService adminService;
}
