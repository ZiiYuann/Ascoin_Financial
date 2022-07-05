package com.tianli.currency;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tianli.currency.mapper.DailyGiftRecord;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.ruleconfig.ConfigConstants;
import com.tianli.mconfig.ConfigService;
import com.tianli.user.userinfo.UserInfoService;
import com.tianli.user.userinfo.mapper.UserInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Objects;

@Service
public class DailyGiftService{

    @Resource
    private DailyGiftRecordService dailyGiftRecordService;

    @Resource
    private DiscountCurrencyService discountCurrencyService;

    @Resource
    private ConfigService configService;

    @Resource
    private UserInfoService userInfoService;

    @Transactional
    public DailyGiftRecord receive(long uid){
        // 监测是否已经领取
        boolean checkReceive = checkReceive(uid);
        if(checkReceive){
            ErrorCodeEnum.throwException("明日再来");
        }
        // 生成领取记录数据
        String dailyGiftAmount = configService.getOrDefault(ConfigConstants.DAILY_GIFT_AMOUNT, "10");
        UserInfo my = userInfoService.getOrSaveById(uid);
        DailyGiftRecord giftRecord = dailyGiftRecordService.addGift(uid, my.getUsername(), my.getNick(), TokenCurrencyType.usdt_omni, TokenCurrencyType.usdt_omni.amount(dailyGiftAmount));
        // 添加用户DigitalCurrency金额
        discountCurrencyService.increase(uid, giftRecord.getAmount(), CurrencyTokenEnum.usdt_omni, giftRecord.getId(), "每日奖励");
        return giftRecord;
    }

    public boolean checkReceive(long uid){
        // 监测是否已经领取
        DailyGiftRecord dailyGiftRecord = dailyGiftRecordService.getOne(Wrappers.<DailyGiftRecord>lambdaQuery()
                .eq(DailyGiftRecord::getUid, uid)
                .eq(DailyGiftRecord::getReceive_date, LocalDate.now())
        );
        return Objects.nonNull(dailyGiftRecord);
    }

}