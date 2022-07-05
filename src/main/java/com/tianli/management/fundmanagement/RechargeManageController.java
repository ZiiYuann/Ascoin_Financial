package com.tianli.management.fundmanagement;

import com.google.common.collect.Lists;
import com.tianli.charge.ChargeService;
import com.tianli.charge.ChargeType;
import com.tianli.charge.mapper.Charge;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.exception.Result;
import com.tianli.role.annotation.AdminPrivilege;
import com.tianli.role.annotation.Privilege;
import com.tianli.tool.MapTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("rechargeManage")
public class RechargeManageController {
    @Resource
    private ChargeService chargeService;

    @GetMapping("/page")
    @AdminPrivilege(and = Privilege.充值管理)
    public Result page(String phone, String txid, String startTime, String endTime,
                       @RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "size", defaultValue = "10") Integer size){
        int count = chargeService.selectCount(null, null, ChargeType.recharge, phone, txid, startTime, endTime);
        if(count <= 0){
            return Result.instance().setData(MapTool.Map().put("total", 0).put("list", Lists.newArrayList()).put("sumAmount", 0).put("sumBFAmount", 0));
        }
        CompletableFuture<Map<String, BigDecimal>> completableFuture = CompletableFuture.supplyAsync(() ->
            chargeService.sumChargeAmount(ChargeType.recharge, phone, txid, startTime, endTime)
        );
        List<Charge> charges = chargeService.selectPage(null, null, ChargeType.recharge, phone, txid, startTime, endTime, page, size);
        List<RechargeManageVO> voList = charges.stream().map(RechargeManageVO::trans).collect(Collectors.toList());
        MapTool map = MapTool.Map().put("total", count)
                .put("list", voList);
        try {
            Map<String, BigDecimal> sumMap = completableFuture.get();
            BigDecimal sumAmountErc20 = sumMap.get("sumAmountErc20");
            double sumErc20Double = TokenCurrencyType.usdt_erc20.money(sumAmountErc20.toBigInteger());
            BigDecimal sumAmountBep20 = sumMap.get("sumAmountBep20");
            double sumBep20Double = TokenCurrencyType.usdt_bep20.money(sumAmountBep20.toBigInteger());
            BigDecimal sumAmountTrc20 = sumMap.get("sumAmountTrc20");
            double sumTrc20Double = TokenCurrencyType.usdt_trc20.money(sumAmountTrc20.toBigInteger());
            BigDecimal sumAmountBF = sumMap.get("sumAmountBF");

            map.put("sumBFAmount", TokenCurrencyType.BF_bep20.money(sumAmountBF.toBigInteger()))
                    .put("sumAmount", sumErc20Double+sumBep20Double+sumTrc20Double);
        } catch (Exception e) {
            log.warn("充值管理page接口, 异步获取汇总数据异常!! ", e);
            map.put("sumAmount", 0).put("sumBFAmount", 0);
        }
        return Result.instance().setData(map);
    }

}
