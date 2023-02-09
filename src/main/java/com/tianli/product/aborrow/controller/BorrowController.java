package com.tianli.product.aborrow.controller;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-09
 **/

import com.tianli.common.RedisLockConstants;
import com.tianli.common.lock.RedissonClientTool;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.product.aborrow.query.BorrowCoinQuery;
import com.tianli.product.aborrow.service.BorrowService;
import com.tianli.sso.init.RequestInitService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/borrow")
public class BorrowController {

    @Resource
    private RequestInitService requestInitService;
    @Resource
    private BorrowService borrowService;
    @Resource
    private RedissonClientTool redissonClientTool;

    @PostMapping("/coin")
    public Result<Void> pledge(@RequestBody @Valid BorrowCoinQuery query) {
        Long uid = requestInitService.uid();
        String key = RedisLockConstants.LOCK_BORROW_COIN + uid;

        redissonClientTool.tryLock(key, () -> borrowService.borrowCoin(uid, query), ErrorCodeEnum.BORROW_COIN_ERROR);

        return new Result<>();
    }


}
