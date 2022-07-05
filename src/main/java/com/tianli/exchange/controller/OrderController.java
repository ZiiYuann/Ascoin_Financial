package com.tianli.exchange.controller;

import cn.hutool.core.util.StrUtil;
import com.tianli.common.init.RequestInitService;
import com.tianli.common.lock.RedisLock;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.exchange.dto.PlaceOrderDTO;
import com.tianli.exchange.dto.RevokeOrderDTO;
import com.tianli.exchange.push.DepthStream;
import com.tianli.exchange.service.IOrderService;
import com.tianli.exchange.vo.DepthVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author lzy
 * @date 2022/5/24 11:42
 * 订单
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource
    IOrderService orderService;

    @Resource
    RedisLock redisLock;

    @Resource
    RequestInitService requestInitService;

    private final String SEND_ORDER_KEY = "send_order_key_uid:{}";

    /**
     * 下单
     *
     * @param placeOrderDTO
     * @return
     */
    @PostMapping("/place")
    public Result placeOrder(@RequestBody @Validated PlaceOrderDTO placeOrderDTO) {
        Long uid = requestInitService.uid();
        if (!redisLock._lock(StrUtil.format(SEND_ORDER_KEY, uid), 10L, TimeUnit.SECONDS)) {
            throw ErrorCodeEnum.TOO_FREQUENT.generalException();
        }
        try {
            orderService.placeOrder(placeOrderDTO, uid);
        } finally {
            redisLock.unlock();
        }
        return Result.success();
    }

    /**
     * 撤销订单
     *
     * @return
     */
    @PostMapping("/revokeOrder")
    public Result revokeOrder(@RequestBody RevokeOrderDTO revokeOrderDTO) {
        orderService.revokeOrder(revokeOrderDTO);
        return Result.success();
    }

    @GetMapping("/depth")
    public Result depth(String symbol) {
        DepthVo depthVo = orderService.queryDepth(symbol);
        return Result.success(depthVo);
    }

}
