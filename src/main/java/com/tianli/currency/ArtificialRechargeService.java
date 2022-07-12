package com.tianli.currency;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.currency.log.CurrencyLogService;
import com.tianli.currency.mapper.ArtificialRecharge;
import com.tianli.currency.mapper.ArtificialRechargeMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigInteger;

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