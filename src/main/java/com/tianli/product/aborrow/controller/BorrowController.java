package com.tianli.product.aborrow.controller;

import com.tianli.common.RedisLockConstants;
import com.tianli.common.lock.RedissonClientTool;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.product.aborrow.query.BorrowCoinQuery;
import com.tianli.product.aborrow.query.CalPledgeQuery;
import com.tianli.product.aborrow.query.ModifyPledgeContextQuery;
import com.tianli.product.aborrow.query.RepayCoinQuery;
import com.tianli.product.aborrow.service.BorrowConfigCoinService;
import com.tianli.product.aborrow.service.BorrowConfigPledgeService;
import com.tianli.product.aborrow.service.BorrowService;
import com.tianli.product.aborrow.vo.BorrowConfigCoinVO;
import com.tianli.product.aborrow.vo.BorrowConfigPledgeVO;
import com.tianli.product.aborrow.vo.CalPledgeVO;
import com.tianli.sso.init.RequestInitService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-09
 **/
@RestController
@RequestMapping("/loan")
public class BorrowController {

    @Resource
    private RequestInitService requestInitService;
    @Resource
    private BorrowService borrowService;
    @Resource
    private RedissonClientTool redissonClientTool;
    @Resource
    private BorrowConfigPledgeService borrowConfigPledgeService;
    @Resource
    private BorrowConfigCoinService borrowConfigCoinService;

    /**
     * 借币
     */
    @PostMapping("/borrow")
    public Result<Void> borrow(@RequestBody @Valid BorrowCoinQuery query) {
        Long uid = requestInitService.uid();
        String key = RedisLockConstants.LOCK_BORROW + uid;

        redissonClientTool.tryLock(key, () -> borrowService.borrowCoin(uid, query), ErrorCodeEnum.BORROW_COIN_ERROR);

        return new Result<>();
    }

    /**
     * 还币
     */
    @PostMapping("/repay")
    public Result<Void> repay(@RequestBody @Valid RepayCoinQuery query) {
        Long uid = requestInitService.uid();
        String key = RedisLockConstants.LOCK_BORROW + uid;

        redissonClientTool.tryLock(key, () -> borrowService.repayCoin(uid, query), ErrorCodeEnum.BORROW_COIN_ERROR);

        return new Result<>();
    }

    /**
     * 增减质押物
     */
    @PostMapping("/pledgeContext")
    public Result<Void> repay(@RequestBody @Valid ModifyPledgeContextQuery query) {
        Long uid = requestInitService.uid();
        String key = RedisLockConstants.LOCK_BORROW + uid;

        redissonClientTool.tryLock(key, () -> borrowService.modifyPledgeContext(uid, query), ErrorCodeEnum.BORROW_COIN_ERROR);

        return new Result<>();
    }

    /**
     * 质押计算
     */
    @PostMapping("/pledge/cal")
    public Result<CalPledgeVO> pledge(@RequestBody @Valid CalPledgeQuery query) {
        Long uid = requestInitService.uid();
        return new Result<>(new CalPledgeVO(borrowService.preCalPledgeRate(uid, query)));
    }

    /**
     * 质押币配置
     */
    @GetMapping("/config/pledge")
    public Result<List<BorrowConfigPledgeVO>> pledgeConfig() {
        return new Result<>(borrowConfigPledgeService.getVOs());
    }

    /**
     * 借币配置
     */
    @GetMapping("/config/coin")
    public Result<List<BorrowConfigCoinVO>> coinConfig() {
        return new Result<>(borrowConfigCoinService.getVOs());
    }

}
