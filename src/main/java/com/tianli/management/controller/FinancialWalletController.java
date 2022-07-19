package com.tianli.management.controller;

import com.tianli.charge.service.ChargeService;
import com.tianli.charge.vo.OrderChargeInfoVO;
import com.tianli.common.PageQuery;
import com.tianli.exception.Result;
import com.tianli.financial.service.FinancialService;
import com.tianli.management.query.FinancialBoardQuery;
import com.tianli.management.query.FinancialRechargeQuery;
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
    private FinancialService financialService;
    @Resource
    private ChargeService chargeService;

    /**
     * 云钱包数据board
     */
    @GetMapping("/board")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result board(FinancialBoardQuery query) {
        query.calTime();
        return Result.success().setData(financialService.walletBoard(query));
    }

    @GetMapping("/order/recharge")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result rechargeOrder(PageQuery<OrderChargeInfoVO> page, FinancialRechargeQuery query) {
        return Result.success().setData(chargeService.selectOrderChargeInfoVOPage(page.page(),query));
    }
}
