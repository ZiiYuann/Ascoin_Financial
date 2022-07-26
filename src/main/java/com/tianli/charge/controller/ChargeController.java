package com.tianli.charge.controller;

import cn.hutool.core.map.MapUtil;
import com.google.gson.Gson;
import com.tianli.chain.enums.ChainType;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.query.RedeemQuery;
import com.tianli.charge.query.WithdrawQuery;
import com.tianli.charge.service.ChargeService;
import com.tianli.charge.vo.OrderSettleRecordVO;
import com.tianli.common.PageQuery;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.query.PurchaseQuery;
import com.tianli.financial.service.FinancialService;
import com.tianli.financial.vo.OrderFinancialVO;
import com.tianli.management.query.FinancialOrdersQuery;
import com.tianli.mconfig.ConfigService;
import com.tianli.sso.init.RequestInitService;
import com.tianli.tool.crypto.Crypto;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.util.DigestFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * @author wangqiyun
 * @since 2020/3/31 15:19
 */
@Slf4j
@RestController
@RequestMapping("/charge")
public class ChargeController {

    @Resource
    private Gson gson;
    @Resource
    private ConfigService configService;
    @Resource
    private ChargeService chargeService;
    @Resource
    private RequestInitService requestInitService;
    @Resource
    private FinancialService financialService;

    /**
     * 充值回调
     */
    @RequestMapping(value = {"/recharge/{chain}","/recharge"} , produces = "text/plain")
    public String rechargeCallback(@PathVariable(required = false) ChainType chain,
                                   @RequestBody String str ,
                                   @RequestHeader("Sign") String sign,
                                   @RequestHeader("timestamp") String timestamp) {
        if (chain == null) { //等于ping
            return "success";
        }

        if (Crypto.hmacToString(DigestFactory.createSHA256(), "vUfV1n#JdyG^oKUp", timestamp).equals(sign)) {
            long l = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));
            if ((Long.parseLong(timestamp) + 10) >= l) {
                log.info("验签成功， {}", sign);
                chargeService.rechargeCallback(str);
                return "success";
            } else {
                throw ErrorCodeEnum.SIGN_ERROR.generalException();
            }
        }
           throw ErrorCodeEnum.SIGN_ERROR.generalException();
    }

    /**
     * 提现申请
     */
    @PostMapping("/withdraw/apply")
    public Result withdraw(@RequestBody @Valid WithdrawQuery withdrawDTO) {
        Long uid = requestInitService.uid();
        chargeService.withdraw(uid,withdrawDTO);
        return Result.instance();
    }

    /**
     * 赎回
     */
    @PostMapping("/redeem")
    public Result redeem(@RequestBody @Valid RedeemQuery query){
        Long uid = requestInitService.uid();
        String orderNo = chargeService.redeem(uid,query);
        HashMap<Object, Object> result = MapUtil.newHashMap();
        result.put("orderNo",orderNo);
        return Result.instance().setData(result);
    }

    /**
     * 申购理财产品（余额）
     */
    @PostMapping("/purchase/balance")
    public Result balancePurchase(@RequestBody @Valid PurchaseQuery purchaseQuery){
        return Result.instance().setData(financialService.purchase(purchaseQuery));
    }

    /**
     * 订单详情【充值、提币】
     */
    @GetMapping("/details/{orderNo}")
    public Result chargeOrderDetails(@PathVariable String orderNo) {
        Long uid = requestInitService.uid();
        return Result.instance().setData(chargeService.chargeOrderDetails(uid, orderNo));
    }

    /**
     * 结算记录
     */
    @GetMapping("/settles")
    public Result settles(PageQuery<OrderSettleRecordVO> page, ProductType productType) {
        Long uid = requestInitService.uid();
        return Result.instance().setData(chargeService.settleOrderPage(page.page(),uid,productType));
    }

    /**
     * 交易记录【申购、赎回、转存】
     */
    @GetMapping("/orders")
    public Result order(PageQuery<OrderFinancialVO> pageQuery, ProductType productType, ChargeType chargeType) {
        if(Objects.nonNull(chargeType) && !ChargeType.purchase.equals(chargeType) && !ChargeType.redeem.equals(chargeType)
                && !ChargeType.transfer.equals(chargeType)){
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }

        FinancialOrdersQuery query  = new FinancialOrdersQuery();
        query.setProductType(productType);
        query.setChargeType(chargeType);
        query.setUid(requestInitService.uid());
        query.setDefaultChargeType(List.of(ChargeType.purchase,ChargeType.redeem,ChargeType.transfer));
        return Result.instance().setData(financialService.orderPage(pageQuery.page(),query));
    }

    /**
     * 交易订单详情【申购、赎回、转存】
     */
    @GetMapping("/order/{orderNo}")
    public Result orderDetails(@PathVariable String orderNo) {
        Long uid = requestInitService.uid();
        return Result.instance().setData(chargeService.orderDetails(uid, orderNo));
    }

}
