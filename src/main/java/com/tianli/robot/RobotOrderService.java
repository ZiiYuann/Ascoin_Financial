package com.tianli.robot;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.CommonFunction;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.robot.mapper.RobotOrder;
import com.tianli.robot.mapper.RobotOrderMapper;
import com.tianli.tool.time.TimeTool;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;


@Service
public class RobotOrderService extends ServiceImpl<RobotOrderMapper, RobotOrder> {

    public void saveOne(Long uid, Integer auto_count, Double auto_amount, long code, String interval_time, long nextTime, String coin) {
        int date = TimeTool.getNowInteger();
        RobotOrder robotOrder = super.getOne(Wrappers.lambdaQuery(RobotOrder.class)
                .eq(RobotOrder::getUid, uid)
        );
        if (Objects.nonNull(robotOrder)) {
            ErrorCodeEnum.throwException("重复操作");
        }
        super.save(RobotOrder.builder()
                .id(CommonFunction.generalId())
                .create_time(LocalDateTime.now())
                .uid(uid)
                .coin(coin)
                .count(auto_count)
                .amount(new BigDecimal(auto_amount + ""))
                .date(date)
                .status(true)
                .interval_time(interval_time)
                .next_bet_time(nextTime)
                .robot_code(code)
                .build());
    }

    public boolean decrementCount(long id) {
        RobotOrder robotOrder = super.getById(id);
        if (Objects.isNull(robotOrder)) {
            return false;
        }
        return baseMapper.decrementCount(id) > 0;
    }
}
