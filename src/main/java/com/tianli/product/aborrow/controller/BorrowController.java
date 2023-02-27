package com.tianli.product.aborrow.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.common.PageQuery;
import com.tianli.common.RedisLockConstants;
import com.tianli.common.lock.RedissonClientTool;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.product.aborrow.entity.BorrowOperationLog;
import com.tianli.product.aborrow.entity.BorrowRecord;
import com.tianli.product.aborrow.enums.PledgeType;
import com.tianli.product.aborrow.query.*;
import com.tianli.product.aborrow.service.*;
import com.tianli.product.aborrow.vo.*;
import com.tianli.sso.init.RequestInitService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    @Resource
    private BorrowOperationLogService borrowOperationLogService;
    @Resource
    private BorrowRecordPledgeService borrowRecordPledgeService;
    @Resource
    private BorrowRecordService borrowRecordService;
    @Resource
    private BorrowRecordCoinService borrowRecordCoinService;

    /**
     * 借币
     */
    @PostMapping("/borrow")
    public Result<Void> borrow(@RequestBody @Valid BorrowCoinQuery query) {
        Long uid = requestInitService.uid();
        String key = RedisLockConstants.LOCK_BORROW + uid;

        redissonClientTool.tryLock(key, () -> borrowService.borrowCoin(uid, query)
                , ErrorCodeEnum.BORROW_COIN_ERROR, true);

        return new Result<>();
    }

    /**
     * 还币
     */
    @PostMapping("/repay")
    public Result<Void> repay(@RequestBody @Valid RepayCoinQuery query) {
        Long uid = requestInitService.uid();
        String key = RedisLockConstants.LOCK_BORROW + uid;

        redissonClientTool.tryLock(key, () -> borrowService.repayCoin(uid, query)
                , ErrorCodeEnum.BORROW_COIN_ERROR, true);

        return new Result<>();
    }

    /**
     * 增减质押物
     */
    @PostMapping("/pledge/context")
    public Result<Void> repay(@RequestBody @Valid ModifyPledgeContextQuery query) {
        Long uid = requestInitService.uid();
        String key = RedisLockConstants.LOCK_BORROW + uid;

        redissonClientTool.tryLock(key, () -> borrowService.modifyPledgeContext(uid, query)
                , ErrorCodeEnum.BORROW_COIN_ERROR);

        return new Result<>();
    }

    /**
     * 质押计算
     */
    @PostMapping("/pre/cal")
    public Result<CalPledgeVO> pledge(@RequestBody @Valid CalPledgeQuery query) {
        Long uid = requestInitService.uid();
        return new Result<>(new CalPledgeVO(borrowService.preCalPledgeRate(uid, query, false)));
    }

    /**
     * 质押币配置
     */
    @GetMapping("/config/pledge")
    public Result<List<BorrowConfigPledgeVO>> pledgeConfig() {
        return new Result<>(borrowConfigPledgeService.getVOs());
    }

    @GetMapping("/borrow/account")
    public Result<List<AccountBorrowVO>> borrowAccount() {
        Long uid = requestInitService.uid();
        return new Result<>(borrowConfigCoinService.getAccountBorrowVOs(uid));
    }

    @GetMapping("/pledge/account")
    public Result<List<AccountPledgeVO>> pledgeAccount() {
        Long uid = requestInitService.uid();
        return new Result<>(borrowConfigPledgeService.getAccountPledgeVOs(uid));
    }

    @GetMapping("/pledge/record")
    public Result<List<BorrowRecordPledgeVO>> pledgeRecord(
            @RequestParam(value = "pledgeType", required = false) PledgeType pledgeType) {
        Long uid = requestInitService.uid();
        BorrowRecord borrowRecord = borrowRecordService.getValid(uid);
        return new Result<>(borrowRecordPledgeService.vos(uid, borrowRecord.getId(), pledgeType));
    }

    @GetMapping("/borrow/record")
    public Result<List<BorrowRecordPledgeVO>> borrowRecord() {
        Long uid = requestInitService.uid();
        BorrowRecord borrowRecord = borrowRecordService.get(uid);
        if (Objects.isNull(borrowRecord)) {
            return new Result<>(new ArrayList<>());
        }
        return new Result<>(borrowRecordCoinService.vos(uid, borrowRecord.getId()));
    }

    @GetMapping("/pledge/product")
    public Result<List<ProductPledgeVO>> pledgeProduct() {
        Long uid = requestInitService.uid();
        return new Result<>(borrowConfigPledgeService.getProductPledgeVOs(uid));
    }

    /**
     * 借币配置
     */
    @GetMapping("/config/coin")
    public Result<List<BorrowConfigCoinVO>> coinConfig() {
        return new Result<>(borrowConfigCoinService.getVOs());
    }

    /**
     * 借币详情
     */
    @GetMapping("/details")
    public Result<BorrowRecordSnapshotVO> details() {
        Long uid = requestInitService.uid();
        return new Result<>(borrowService.newestSnapshot(uid));
    }

    @GetMapping("/logs")
    public Result<IPage<BorrowOperationLogVO>> logs(PageQuery<BorrowOperationLog> pageQuery, BorrowOperationLogQuery query) {
        Long uid = requestInitService.uid();
        query.setUid(uid);
        return new Result<>(borrowOperationLogService.logs(pageQuery.page(), query));
    }


}
