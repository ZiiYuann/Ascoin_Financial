package com.tianli.user.service;

import com.tianli.account.entity.AccountBalance;
import com.tianli.common.CommonFunction;
import com.tianli.common.async.AsyncService;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.user.entity.UserInfo;
import com.tianli.user.mapper.UserInfoMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-13
 **/
@Service
public class UserInfoServiceImpl implements UserInfoService{

    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private AsyncService asyncService;

    @Override
    public UserInfo getBySignInfo(String signAddress, String signChain) {
        UserInfo userInfo = userInfoMapper.getBySignInfo(signAddress, signChain);

        if (Objects.isNull(userInfo)) {
            userInfo = UserInfo.builder()
                    .id(CommonFunction.generalId())
                    .signAddress(signAddress)
                    .signChain(signChain)
                    .build();
            final UserInfo userInfoFinal = userInfo;
            asyncService.async(() -> userInfoMapper.insert(userInfoFinal));
        }
        return userInfo;
    }

}
