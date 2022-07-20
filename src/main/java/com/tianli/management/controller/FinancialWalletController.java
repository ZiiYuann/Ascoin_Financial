package com.tianli.management.controller;

import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.ChargeService;
import com.tianli.charge.vo.OrderChargeInfoVO;
import com.tianli.common.PageQuery;
import com.tianli.exception.Result;
import com.tianli.management.query.FinancialBoardQuery;
import com.tianli.management.query.FinancialChargeQuery;
import com.tianli.management.service.FinancialBoardWalletService;
import com.tianli.management.vo.FinancialSummaryDataVO;
import com.tianli.sso.permission.AdminPrivilege;
import com.tianli.sso.permission.Privilege;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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
        return Result.success().setData(chargeService.selectOrderChargeInfoVOPage(page.page(),query));
    }

    @GetMapping("/order/recharge/data")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result rechargeOrderData(FinancialChargeQuery query) {
        query.setChargeType(ChargeType.recharge);

        return Result.success()
                .setData(FinancialSummaryDataVO.builder().rechargeAmount(chargeService.orderChargeSummaryAmount(query)).build());
    }

    @GetMapping("/order/withdraw")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result withdrawOrder(PageQuery<OrderChargeInfoVO> page, FinancialChargeQuery query) {
        query.setChargeType(ChargeType.withdraw);
        return Result.success().setData(chargeService.selectOrderChargeInfoVOPage(page.page(),query));
    }

    @GetMapping("/order/withdraw/data")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result withdrawOrderData(FinancialChargeQuery query) {
        query.setChargeType(ChargeType.recharge);
        return Result.success()
                .setData(FinancialSummaryDataVO.builder().withdrawAmount(chargeService.orderChargeSummaryAmount(query)).build());
    }
}
