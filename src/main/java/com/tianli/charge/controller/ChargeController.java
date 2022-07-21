package com.tianli.charge.controller;

import com.google.gson.Gson;
import com.tianli.address.query.RechargeCallbackQuery;
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
import com.tianli.mconfig.ConfigService;
import com.tianli.sso.init.RequestInitService;
import com.tianli.tool.crypto.Crypto;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.util.DigestFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
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
    @PostMapping("/recharge")
    public Result rechargeCallback(@RequestBody String str, @RequestHeader("AppKey") String appKey
            , @RequestHeader("Sign") String sign) {
        String walletAppKey = configService.get("wallet_app_key");
        String walletAppSecret = configService.get("wallet_app_secret");

        log.info("充值回调参数 ==> {}", gson.toJson(str));

        if (walletAppKey.equals(appKey) && Crypto.hmacToString(DigestFactory.createSHA256(), walletAppSecret, str).equals(sign)) {
            ErrorCodeEnum.SIGN_ERROR.throwException();
        }

        RechargeCallbackQuery query = gson.fromJson(str, RechargeCallbackQuery.class);
        chargeService.rechargeCallback(query);
        return Result.success();
    }

    /**
     * 提现申请
     */
    @PostMapping("/withdraw/apply")
    public Result withdraw(@RequestBody @Valid WithdrawQuery withdrawDTO) {
        if (withdrawDTO.getCurrencyAdaptType().isFiat())
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
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
        chargeService.redeem(uid,query);
        return Result.instance();
    }

    /**
     * 申购理财产品（余额）
     */
    @PostMapping("/purchase/balance")
    public Result balancePurchase(@RequestBody @Valid PurchaseQuery purchaseQuery){
        //TODO 币种的转换，校验密码
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
        Long uid = requestInitService.uid();;
        return Result.instance().setData(chargeService.settleOrderPage(page.page(),uid,productType));
    }

    /**
     * 交易记录
     */
    @GetMapping("/orders")
    public Result order(PageQuery<OrderFinancialVO> pageQuery, ProductType productType, ChargeType chargeType) {
        if(Objects.nonNull(chargeType) && !ChargeType.purchase.equals(chargeType) && !ChargeType.redeem.equals(chargeType)
                && !ChargeType.transfer.equals(chargeType)){
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }

        Long uid = requestInitService.uid();
        return Result.instance().setData(financialService.orderPage(uid,pageQuery.page(),productType,chargeType));
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
