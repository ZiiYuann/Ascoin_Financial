package com.tianli.user.userinfo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.captcha.phone.service.CaptchaPhoneService;
import com.tianli.common.Constants;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.user.UserService;
import com.tianli.user.mapper.User;
import com.tianli.user.userinfo.mapper.UserInfo;
import com.tianli.user.userinfo.mapper.UserInfoMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author hd
 * @since 2020-12-03
 */
@Service
public class UserInfoService extends ServiceImpl<UserInfoMapper, UserInfo>{

    /**
     * 根据用户id获取用户信息, 如果为空则创建一个
     *
     * @param id 用户id
     */
    public UserInfo getOrSaveById(long id){
        UserInfo byId = super.getById(id);
        if (Objects.nonNull(byId)) {
//            if (Strings.isNullOrEmpty(byId.getRegion())) {
//                String region = captchaPhoneService.getRegion(byId.getUsername());
//                if (!Strings.isNullOrEmpty(region)) {
//                    byId.setRegion(region);
//                    CompletableFuture.runAsync(() ->super.update(new LambdaUpdateWrapper<UserInfo>().set(UserInfo::getRegion, region).eq(UserInfo::getId, id)));
//                }
//            }
            return byId;
        }
        return saveById(id);
    }

    public UserInfo saveById(long id){
        User user = userService._get(id);
        if(Objects.isNull(user)){
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }
//        String region = captchaPhoneService.getRegion(user.getUsername());
        UserInfo userInfo = UserInfo.builder()
                .id(id)
                .nick(Constants.defaultUserNick)
                .create_time(LocalDateTime.now())
                .username(user.getUsername())
                .build();
        boolean save = super.save(userInfo);
        if (!save) ErrorCodeEnum.TOO_FREQUENT.throwException();
        return userInfo;
    }

    @Resource
    private UserService userService;

    @Resource
    private CaptchaPhoneService captchaPhoneService;

}
