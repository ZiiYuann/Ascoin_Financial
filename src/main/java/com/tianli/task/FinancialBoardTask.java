package com.tianli.task;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.tianli.charge.service.OrderService;
import com.tianli.management.entity.FinancialBoardProduct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 【管理端】理财看板数据定时器
 * @author chenb
 * @apiNote
 * @since 2022-07-25
 **/
@Slf4j
@Component
public class FinancialBoardTask {

    @Resource
    private OrderService orderService;

    public void task(){
        LocalDateTime todayBegin = DateUtil.beginOfDay(new DateTime()).toLocalDateTime();
        LocalDateTime yesterdayBegin = todayBegin.plusDays(-1);

        FinancialBoardProduct boardProduct = new FinancialBoardProduct();



    }

    public static void main(String[] args) {
        LocalDateTime todayBegin = DateUtil.beginOfDay(new DateTime()).toLocalDateTime();
        LocalDateTime yesterdayBegin = todayBegin.plusDays(-1);
        log.info(todayBegin + "------"+yesterdayBegin);
    }

}
