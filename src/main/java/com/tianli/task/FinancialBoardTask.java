package com.tianli.task;

import com.tianli.charge.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

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

    }


}
