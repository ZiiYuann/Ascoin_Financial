package com.tianli.management.controller;

import com.tianli.account.service.AccountBalanceService;
import com.tianli.chain.entity.WalletImputation;
import com.tianli.chain.entity.WalletImputationLog;
import com.tianli.chain.entity.WalletImputationLogAppendix;
import com.tianli.chain.service.WalletImputationLogAppendixService;
import com.tianli.chain.service.WalletImputationLogService;
import com.tianli.chain.service.WalletImputationService;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.query.OrderReviewQuery;
import com.tianli.charge.service.ChargeService;
import com.tianli.charge.service.OrderReviewService;
import com.tianli.charge.vo.OrderChargeInfoVO;
import com.tianli.common.PageQuery;
import com.tianli.exception.Result;
import com.tianli.management.query.*;
import com.tianli.management.service.FinancialBoardWalletService;
import com.tianli.management.vo.FinancialSummaryDataVO;
import com.tianli.sso.permission.AdminPrivilege;
import com.tianli.sso.permission.Privilege;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author lzy
 * @since 2022/4/1 6:20 下午
 */
@RestController
@RequestMapping("/management/financial/wallet/")
public class FinancialWalletController {

    @Resource
    private ChargeService chargeService;
    @Resource
    private FinancialBoardWalletService financialWalletBoardService;
    @Resource
    private WalletImputationService walletImputationService;
    @Resource
    private WalletImputationLogService walletImputationLogService;
    @Resource
    private WalletImputationLogAppendixService walletImputationLogAppendixService;
    @Resource
    private AccountBalanceService accountBalanceService;
    @Resource
    private OrderReviewService orderReviewService;

    /**
     * 云钱包数据board
     */
    @GetMapping("/board")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result board(FinancialBoardQuery query) {
        query.calTime();
        return Result.success().setData(financialWalletBoardService.walletBoard(query));
    }

    @GetMapping("/order/recharge")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result rechargeOrder(PageQuery<OrderChargeInfoVO> page, FinancialChargeQuery query) {
        query.setChargeType(ChargeType.recharge);
        return Result.success().setData(chargeService.selectOrderChargeInfoVOPage(page.page(), query));
    }

    @GetMapping("/order/recharge/data")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result rechargeOrderData(FinancialChargeQuery query) {
        query.setChargeType(ChargeType.recharge);
        return Result.success()
                .setData(FinancialSummaryDataVO.builder().rechargeAmount(chargeService.orderAmountSum(query)).build());
    }

    @GetMapping("/order/withdraw")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result withdrawOrder(PageQuery<OrderChargeInfoVO> page, FinancialChargeQuery query) {
        query.setChargeType(ChargeType.withdraw);
        return Result.success().setData(chargeService.selectOrderChargeInfoVOPage(page.page(), query));
    }

    /**
     * 审核详情信息
     */
    @GetMapping("/order/withdraw/review/{orderNo}")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result orderReview(@PathVariable String orderNo) {
        return Result.success().setData(orderReviewService.getVOByOrderNo(orderNo));
    }

    /**
     * 管理端审核
     */
    @PostMapping("/order/withdraw/review")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result orderReview(@RequestBody @Valid OrderReviewQuery query) {
        orderReviewService.review(query);
        return Result.success();
    }

    @GetMapping("/order/withdraw/data")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result withdrawOrderData(FinancialChargeQuery query) {
        query.setChargeType(ChargeType.withdraw);
        return Result.success()
                .setData(FinancialSummaryDataVO.builder().withdrawAmount(chargeService.orderAmountSum(query)).build());
    }

    /**
     * 钱包余额
     */
    @GetMapping("/accounts")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result accounts() {
        return Result.success().setData(accountBalanceService.getTotalSummaryData());
    }

    /**
     * 归集列表
     */
    @GetMapping("/imputations")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result imputations(PageQuery<WalletImputation> page, WalletImputationQuery query) {
        return Result.success(walletImputationService.walletImputationVOPage(page.page(), query));
    }

    /**
     * 手动归集
     */
    @PostMapping("/imputation")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result imputation(@RequestBody WalletImputationManualQuery query) {
        walletImputationService.imputationOperation(query);
        return Result.success();
    }

    /**
     * 归集日志列表列表
     */
    @GetMapping("/imputationLogs")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result imputationLogs(PageQuery<WalletImputationLog> page, WalletImputationLogQuery query) {
        return Result.success(walletImputationLogService.walletImputationLogVOPage(page.page(), query));
    }

    /**
     * 归集日志详情地址列表
     */
    @GetMapping("/imputation/appendix")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result imputationLogs(PageQuery<WalletImputationLogAppendix> page, String txid) {
        return Result.success(walletImputationLogAppendixService.pageByTxid(page.page(), txid));
    }
}
