package com.tianli.currency;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.enums.ProductType;
import com.tianli.account.service.AccountSummaryService;
import com.tianli.common.init.admin.AdminContent;
import com.tianli.common.init.admin.AdminInfo;
import com.tianli.common.CommonFunction;
import com.tianli.sso.admin.AdminContent;
import com.tianli.sso.admin.AdminInfo;
import com.tianli.currency.log.CurrencyLog;
import com.tianli.account.entity.AccountBalanceOperationLog;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.account.service.AccountBalanceOperationLogService;
import com.tianli.currency.log.CurrencyLogService;
import com.tianli.currency.mapper.ArtificialRecharge;
import com.tianli.currency.log.CurrencyLogType;
import com.tianli.currency.entity.ArtificialRecharge;
import com.tianli.currency.mapper.ArtificialRechargeMapper;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.user.UserService;
import com.tianli.user.mapper.User;
import com.tianli.user.userinfo.UserInfoService;
import com.tianli.user.userinfo.mapper.UserInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class ArtificialRechargeService extends ServiceImpl<ArtificialRechargeMapper, ArtificialRecharge> {


    @Resource
    private AccountSummaryService accountBalanceService;

    @Resource
    private AccountBalanceOperationLogService currencyLogService;

    @Resource
    private ArtificialRechargeMapper artificialRechargeMapper;

    public long getCount(String username, String adminNick, String startTime, String endTime) {
        return artificialRechargeMapper.getCount(username, adminNick, startTime, endTime);
    }

    public BigInteger getSumAmount(String username, String adminNick, String startTime, String endTime) {
        return artificialRechargeMapper.getSumAmount(username, adminNick, startTime, endTime);
    }
}