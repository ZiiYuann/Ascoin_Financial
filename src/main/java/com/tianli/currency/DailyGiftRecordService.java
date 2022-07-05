package com.tianli.currency;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.CommonFunction;
import com.tianli.currency.mapper.DailyGiftRecord;
import com.tianli.currency.mapper.DailyGiftRecordMapper;
import com.tianli.exception.ErrorCodeEnum;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 每日礼包记录 Service
 * </p>
 */
@Service
public class DailyGiftRecordService extends ServiceImpl<DailyGiftRecordMapper, DailyGiftRecord> {

    public DailyGiftRecord addGift(long uid, String username, String nick, TokenCurrencyType token, BigInteger amount) {
        DailyGiftRecord dailyGiftRecord = DailyGiftRecord.builder()
                .id(CommonFunction.generalId())
                .uid(uid)
                .username(username)
                .nick(nick)
                .create_time(LocalDateTime.now())
                .receive_date(LocalDate.now())
                .token(token)
                .amount(amount)
                .build();
        boolean save = super.save(dailyGiftRecord);
        if (!save) ErrorCodeEnum.TOO_FREQUENT.throwException();
        return dailyGiftRecord;
    }


}
