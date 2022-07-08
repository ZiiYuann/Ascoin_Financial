package com.tianli.currency;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.CommonFunction;
import com.tianli.sso.admin.AdminContent;
import com.tianli.sso.admin.AdminInfo;
import com.tianli.currency.log.CurrencyLog;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.currency.log.CurrencyLogService;
import com.tianli.currency.log.CurrencyLogType;
import com.tianli.currency.mapper.ArtificialRecharge;
import com.tianli.currency.mapper.ArtificialRechargeMapper;
import com.tianli.currency.mapper.ArtificialRechargeType;
import com.tianli.exception.ErrorCodeEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class ArtificialRechargeService extends ServiceImpl<ArtificialRechargeMapper, ArtificialRecharge> {


    @Resource
    private CurrencyService currencyService;

    @Resource
    private CurrencyLogService currencyLogService;

    @Resource
    private ArtificialRechargeMapper artificialRechargeMapper;

    public long getCount(String username, String adminNick, String startTime, String endTime) {
        return artificialRechargeMapper.getCount(username, adminNick, startTime, endTime);
    }

    public BigInteger getSumAmount(String username, String adminNick, String startTime, String endTime) {
        return artificialRechargeMapper.getSumAmount(username, adminNick, startTime, endTime);
    }
}