package com.tianli.charge.controller;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.map.MapUtil;
import com.tianli.chain.entity.ChainCallbackLog;
import com.tianli.chain.enums.ChainType;
import com.tianli.chain.service.ChainCallbackLogService;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.query.GenerateOrderAdvanceQuery;
import com.tianli.charge.query.RedeemQuery;
import com.tianli.charge.query.WithdrawQuery;
import com.tianli.charge.service.ChargeService;
import com.tianli.charge.service.OrderAdvanceService;
import com.tianli.charge.vo.OrderSettleRecordVO;
import com.tianli.common.PageQuery;
import com.tianli.common.RedisConstants;
import com.tianli.common.RedisLockConstants;
import com.tianli.common.webhook.WebHookService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.query.PurchaseQuery;
import com.tianli.financial.service.FinancialProductService;
import com.tianli.financial.service.FinancialService;
import com.tianli.financial.vo.FinancialPurchaseResultVO;
import com.tianli.financial.vo.OrderFinancialVO;
import com.tianli.management.query.FinancialOrdersQuery;
import com.tianli.sso.init.RequestInitService;
import com.tianli.tool.crypto.Crypto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetUtils;
import org.bouncycastle.crypto.util.DigestFactory;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * @author wangqiyun
 * @since 2020/3/31 15:19
 */
@Slf4j
@RestController
@RequestMapping("/charge")
public class ChargeController {

    @Resource
    private ChargeService chargeService;
    @Resource
    private RequestInitService requestInitService;
    @Resource
    private FinancialService financialService;
    @Resource
    private FinancialProductService financialProductService;
    @Resource
    private ChainCallbackLogService chainCallbackLogService;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private OrderAdvanceService orderAdvanceService;
    @Resource
    private WebHookService webHookService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 充值回调
     */
    @RequestMapping(value = {"/recharge/{chain}", "/recharge"}, produces = "text/plain")
    public String rechargeCallback(@PathVariable(required = false) ChainType chain,
                                   @RequestBody(required = false) String str,
                                   @RequestHeader("Sign") String sign,
                                   @RequestHeader("timestamp") String timestamp) {
        if (chain == null) { //等于ping
            return "success";
        }

        if (!Crypto.hmacToString(DigestFactory.createSHA256(), "vUfV1n#JdyG^oKUp", timestamp).equals(sign)) {
            throw ErrorCodeEnum.SIGN_ERROR.generalException();
        }

        long l = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));
        if ((Long.parseLong(timestamp) + 10) < l) {
            throw ErrorCodeEnum.SIGN_ERROR.generalException();
        }
        log.info("验签成功， {},充值信息为：{}", sign, str);

        ChainCallbackLog chainCallbackLog = chainCallbackLogService.insert(ChargeType.recharge, chain, str);
        try {
            chargeService.rechargeCallback(chain, str);
            chainCallbackLog.setStatus("success");
        } catch (Exception e) {
            chainCallbackLog.setMsg(ExceptionUtil.getMessage(e));
            chainCallbackLog.setStatus("fail");
            webHookService.dingTalkSend("充值回调失败", e);
            throw e;
        } finally {
            chainCallbackLogService.updateById(chainCallbackLog);
        }
        return "success";
    }

    /**
     * 提现回调
     */
    @RequestMapping(value = {"/withdraw/{chain}", "/withdraw"}, produces = "text/plain")
    public String withdrawCallback(@PathVariable(required = false) ChainType chain,
                                   @RequestBody(required = false) String str,
                                   @RequestHeader("Sign") String sign,
                                   @RequestHeader("timestamp") String timestamp) {
        if (chain == null) { //等于ping
            return "success";
        }

        if (!Crypto.hmacToString(DigestFactory.createSHA256(), "vUfV1n#JdyG^oKUp", timestamp).equals(sign)) {
            throw ErrorCodeEnum.SIGN_ERROR.generalException();
        }


        long l = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));
        if ((Long.parseLong(timestamp) + 10) < l) {
            throw ErrorCodeEnum.SIGN_ERROR.generalException();
        }

        ChainCallbackLog chainCallbackLog = chainCallbackLogService.insert(ChargeType.withdraw, chain, str);
        try {
            chargeService.withdrawCallback(chain, str);
            chainCallbackLog.setStatus("success");
        } catch (Exception e) {
            chainCallbackLog.setMsg(ExceptionUtil.getMessage(e));
            chainCallbackLog.setStatus("fail");
            throw e;
        } finally {
            chainCallbackLogService.updateById(chainCallbackLog);
        }
        return "success";

    }

    /**
     * 提现申请
     */
    @PostMapping("/withdraw/apply")
    public Result withdraw(@RequestBody @Valid WithdrawQuery withdrawDTO) {
        Long uid = requestInitService.uid();
        RLock lock = redissonClient.getLock(RedisLockConstants.PRODUCT_WITHDRAW + uid + ":" + withdrawDTO.getCoin());

        try {
            lock.lock();
            chargeService.withdrawApply(uid, withdrawDTO);
            return Result.instance();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 赎回
     */
    @PostMapping("/redeem")
    public Result redeem(@RequestBody @Valid RedeemQuery query) {
        Long uid = requestInitService.uid();
        RLock lock = redissonClient.getLock(RedisLockConstants.PRODUCT_REDEEM + uid + ":" + query.getRecordId());
        try {
            lock.lock();
            String orderNo = chargeService.redeem(uid, query);
            HashMap<Object, Object> result = MapUtil.newHashMap();
            result.put("orderNo", orderNo);
            return Result.instance().setData(result);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 申购理财产品（余额）
     */
    @PostMapping("/purchase/balance")
    public Result balancePurchase(@RequestBody @Valid PurchaseQuery purchaseQuery) {
        Long uid = requestInitService.uid();
        RLock lock = redissonClient.getLock(RedisLockConstants.PRODUCT_PURCHASE + uid + ":" + purchaseQuery.getProductId());
        try {
            lock.lock();
            return Result.instance().setData(financialProductService.purchase(uid, purchaseQuery, FinancialPurchaseResultVO.class));
        } finally {
            lock.unlock();
        }
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
        return Result.instance().setData(chargeService.settleOrderPage(page.page(), uid, productType));
    }

    /**
     * 交易记录【申购、赎回、转存】
     */
    @GetMapping("/orders")
    public Result order(PageQuery<OrderFinancialVO> pageQuery, ProductType productType, ChargeType chargeType) {
        if (Objects.nonNull(chargeType) && !ChargeType.purchase.equals(chargeType) && !ChargeType.redeem.equals(chargeType)
                && !ChargeType.transfer.equals(chargeType)) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }

        FinancialOrdersQuery query = new FinancialOrdersQuery();
        query.setProductType(productType);
        query.setChargeType(chargeType);
        query.setUid(requestInitService.uid() + "");
        query.setDefaultChargeType(List.of(ChargeType.purchase, ChargeType.redeem, ChargeType.transfer));
        return Result.instance().setData(financialService.orderPage(pageQuery.page(), query));
    }

    /**
     * 交易订单详情【申购、赎回、转存】
     */
    @GetMapping("/order/{orderNo}")
    public Result orderDetails(@PathVariable String orderNo) {
        Long uid = requestInitService.uid();
        return Result.instance().setData(chargeService.orderDetails(uid, orderNo));
    }

    /**
     * 下拉列表，订单状态
     */
    @GetMapping("/pull/order/status")
    public Result orderStatus(ChargeType chargeType) {
        return Result.instance().setData(ChargeType.orderStatusPull(chargeType));
    }

    /**
     * 生成预订单
     */
    @PostMapping("/order/advance")
    public Result generateOrderAdvance(@RequestBody GenerateOrderAdvanceQuery query) {
        return Result.instance().setData(orderAdvanceService.generateOrderAdvance(query));
    }

    /**
     * 更新预订单
     */
    @PutMapping("/order/advance")
    public Result updateOrderAdvance(@RequestBody GenerateOrderAdvanceQuery query) {
        return Result.success(orderAdvanceService.updateOrderAdvance(query));
    }


}
