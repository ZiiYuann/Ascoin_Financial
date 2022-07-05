package com.tianli.currency_token.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.tianli.address.AddressService;
import com.tianli.address.mapper.Address;
import com.tianli.captcha.email.service.CaptchaEmailService;
import com.tianli.captcha.phone.mapper.CaptchaPhoneType;
import com.tianli.charge.controller.WithdrawApplyChargeVO;
import com.tianli.charge.mapper.ChargeStatus;
import com.tianli.common.CommonFunction;
import com.tianli.common.async.AsyncService;
import com.tianli.common.init.RequestInitService;
import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.currency.log.CurrencyLogType;
import com.tianli.currency_token.CurrencyTokenLogService;
import com.tianli.currency_token.CurrencyTokenService;
import com.tianli.currency_token.TokenFavoriteService;
import com.tianli.currency_token.WithdrawService;
import com.tianli.currency_token.dto.*;
import com.tianli.currency_token.mapper.*;
import com.tianli.currency_token.order.CurrencyTokenOrderService;
import com.tianli.currency_token.order.TokenDealService;
import com.tianli.currency_token.order.dto.CurrencyTokenOrderPageDTO;
import com.tianli.currency_token.order.mapper.CurrencyTokenOrder;
import com.tianli.currency_token.order.mapper.CurrencyTokenOrderStatus;
import com.tianli.currency_token.token.TokenListService;
import com.tianli.currency_token.token.mapper.TokenList;
import com.tianli.currency_token.transfer.mapper.TokenContract;
import com.tianli.currency_token.transfer.service.TokenContractService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.exchange.entity.KLinesInfo;
import com.tianli.exchange.enums.PlatformTypeEnum;
import com.tianli.exchange.processor.CoinProcessor;
import com.tianli.kline.KLineService;
import com.tianli.management.ruleconfig.ConfigConstants;
import com.tianli.management.spot.entity.SGCharge;
import com.tianli.management.spot.service.SGChargeService;
import com.tianli.mconfig.ConfigService;
import com.tianli.role.annotation.GrcCheckModular;
import com.tianli.role.annotation.GrcPrivilege;
import com.tianli.tool.MapTool;
import com.tianli.user.UserService;
import com.tianli.user.mapper.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/currency/token")
public class CurrencyTokenController {


    /**
     * 我的现货余额
     *
     * @param removeZero
     * @param page
     * @param size
     * @param token
     * @return
     */
    @GetMapping("/my/list")
    public Result my(@RequestParam(value = "removeZero", defaultValue = "0") Integer removeZero,
                     @RequestParam(value = "page", defaultValue = "1") Integer page,
                     @RequestParam(value = "size", defaultValue = "10") Integer size,
                     String token) {
        Long uid = requestInitService.uid();
        List<CurrencyToken> list = currencyTokenService.list(
                new LambdaQueryWrapper<CurrencyToken>().gt(CurrencyToken::getBalance, BigDecimal.ZERO)
                        .eq(CurrencyToken::getUid, uid).ne(CurrencyToken::getToken, "luna")
        );
        List<CurrencyCoinEnum> owner = list.stream().map(CurrencyToken::getToken).collect(Collectors.toList());
        Map<CurrencyCoinEnum, TokenList> m = tokenListService.list().stream().collect(Collectors.toMap(TokenList::getToken, Function.identity()));
        Set<CurrencyCoinEnum> currencyCoinEnums = m.keySet();
        if (removeZero == 0) {
            currencyCoinEnums.forEach(o -> {
                if (!owner.contains(o)) list.add(CurrencyToken.builder()
                        .balance(BigDecimal.ZERO).freeze(BigDecimal.ZERO).remain(BigDecimal.ZERO)
                        .token(o).type(CurrencyTypeEnum.actual).build());
            });
        }
        List<CurrencyTokenPage> result = list.stream().map(o -> {
            TokenList tokenList = m.get(o.getToken());
            if (o.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                String price = "0";
                if (tokenList.getPlatform_token()) {
                    KLinesInfo kLinesInfo = coinProcessor.getDayKLinesInfoBySymbol(o.getToken().name().toUpperCase() + CurrencyCoinEnum.usdt.getName().toUpperCase());
                    if (ObjectUtil.isNotNull(kLinesInfo)) {
                        price = o.getBalance().multiply(kLinesInfo.getClosing_price()).setScale(6, RoundingMode.FLOOR).toString();
                    }
                } else {
                    price = o.getBalance().multiply(tokenDealService.getBianPrice(CurrencyCoinEnum.usdt, o.getToken())).setScale(6, RoundingMode.FLOOR).toString();
                }
                return CurrencyTokenPage.builder()
                        .tokenInfo(tokenList)
                        .token(o.getToken())
                        .balance(o.getBalance().setScale(6, RoundingMode.FLOOR).toString())
                        .remain(o.getRemain().setScale(6, RoundingMode.FLOOR).toString())
                        .freeze(o.getFreeze().setScale(6, RoundingMode.FLOOR).toString())
                        .is_platform(tokenList.getPlatform_token())
                        .value_u(price).build();
            }
            return CurrencyTokenPage.builder()
                    .tokenInfo(m.get(o.getToken()))
                    .token(o.getToken())
                    .balance(BigDecimal.ZERO.setScale(6, RoundingMode.FLOOR).toString())
                    .remain(BigDecimal.ZERO.setScale(6, RoundingMode.FLOOR).toString())
                    .freeze(BigDecimal.ZERO.setScale(6, RoundingMode.FLOOR).toString())
                    .is_platform(tokenList.getPlatform_token())
                    .value_u(BigDecimal.ZERO.setScale(6, RoundingMode.FLOOR).toString()).build();

        }).collect(Collectors.toList());
        String all_value = BigDecimal.valueOf(result.stream().mapToDouble(o -> Double.parseDouble(o.getValue_u())).sum()).setScale(6, RoundingMode.FLOOR).toString();
        if (!StringUtils.isEmpty(token))
            result = result.stream().filter(o -> o.getToken().name().contains(token.toLowerCase())).collect(Collectors.toList());
        return Result.instance().setData(MapTool.Map()
                .put("list", result.stream().skip((page - 1) * size).limit(size).collect(Collectors.toList()))
                .put("total", result.size())
                .put("account_value", all_value)
        );
    }

    /**
     * 获取某个币的余额
     *
     * @param token
     * @return
     */
    @GetMapping("/my/{token}")
    public Result myToken(@PathVariable("token") CurrencyCoinEnum token) {
        Long uid = requestInitService.uid();
        Map<CurrencyCoinEnum, TokenList> m = tokenListService.list().stream().collect(Collectors.toMap(TokenList::getToken, Function.identity()));
        CurrencyToken currencyToken = currencyTokenService.get(uid, CurrencyTypeEnum.actual, token);
        TokenList tokenList = m.get(currencyToken.getToken());
        BigDecimal price = BigDecimal.ZERO;
        if (tokenList.getPlatform_token()) {
            KLinesInfo kLinesInfo = coinProcessor.getDayKLinesInfoBySymbol(currencyToken.getToken().name().toUpperCase() + CurrencyCoinEnum.usdt.getName().toUpperCase());
            if (ObjectUtil.isNotNull(kLinesInfo)) {
                price = kLinesInfo.getClosing_price();
            }
        } else {
            price = tokenDealService.getBianPrice(CurrencyCoinEnum.usdt, currencyToken.getToken());
        }
        String value_u = currencyToken.getBalance().multiply(price).setScale(6, RoundingMode.FLOOR).toString();
        return Result.instance().setData(CurrencyTokenPage.builder()
                .token(currencyToken.getToken())
                .tokenInfo(m.get(currencyToken.getToken()))
                .balance(currencyToken.getBalance().setScale(6, RoundingMode.FLOOR).toString())
                .remain(currencyToken.getRemain().setScale(6, RoundingMode.FLOOR).toString())
                .freeze(currencyToken.getFreeze().setScale(6, RoundingMode.FLOOR).toString())
                .is_platform(tokenList.getPlatform_token())
                .value_u(value_u).build());
    }

    /**
     * 购买现货
     *
     * @param purchaseTokenDTO
     * @return
     */
    @PostMapping("/purchase")
    public Result purchase(@RequestBody @Valid PurchaseTokenDTO purchaseTokenDTO) {
        Long uid = requestInitService.uid();
        CurrencyToken currencyToken;
        BigDecimal price = tokenDealService.getBianPrice(purchaseTokenDTO.getFiat(), purchaseTokenDTO.getStock());
        BigDecimal fiat_amount = BigDecimal.ZERO;
        BigDecimal stock_amount = BigDecimal.ZERO;

        if (purchaseTokenDTO.getFiat().equals(purchaseTokenDTO.getAmount_unit())) {
            fiat_amount = purchaseTokenDTO.getAmount();
            stock_amount = purchaseTokenDTO.getAmount().divide(price, 10, RoundingMode.HALF_UP);
        }
        if (purchaseTokenDTO.getStock().equals(purchaseTokenDTO.getAmount_unit())) {
            fiat_amount = purchaseTokenDTO.getAmount().multiply(price);
            stock_amount = purchaseTokenDTO.getAmount();
        }

        switch (purchaseTokenDTO.getDirection()) {
            case buy:
                currencyToken = currencyTokenService.get(uid, CurrencyTypeEnum.actual, purchaseTokenDTO.getFiat());
                if (currencyToken.getRemain().compareTo(fiat_amount) < 0) ErrorCodeEnum.CREDIT_LACK.throwException();
                tokenDealService.marketBuy(uid, purchaseTokenDTO.getFiat(), purchaseTokenDTO.getStock(), fiat_amount, stock_amount, price, 0L);
                break;
            case sell:
                currencyToken = currencyTokenService.get(uid, CurrencyTypeEnum.actual, purchaseTokenDTO.getStock());
                if (currencyToken.getRemain().compareTo(stock_amount) < 0) ErrorCodeEnum.CREDIT_LACK.throwException();
                tokenDealService.marketSell(uid, purchaseTokenDTO.getFiat(), purchaseTokenDTO.getStock(), fiat_amount, stock_amount, price, 0L);
                break;
        }
        return Result.instance();
    }

    /**
     * 撤销委托
     *
     * @param stock
     * @param id
     * @return
     */
    @GetMapping("/purchase/cancel")
    public Result cancel(CurrencyCoinEnum stock, Long id) {
        Long uid = requestInitService.uid();
        if (ObjectUtil.isNotNull(stock)) {
            check(stock);
        }
        List<CurrencyTokenOrder> list = currencyTokenOrderService.list(
                new LambdaQueryWrapper<CurrencyTokenOrder>()
                        .eq(CurrencyTokenOrder::getUid, uid)
                        .eq(Objects.nonNull(id), CurrencyTokenOrder::getId, id)
                        .eq(Objects.nonNull(stock), CurrencyTokenOrder::getToken_stock, stock)
                        .eq(CurrencyTokenOrder::getStatus, CurrencyTokenOrderStatus.created)
                        .eq(CurrencyTokenOrder::getType, TokenOrderType.limit)
                        .ne(CurrencyTokenOrder::getPlatform_type, PlatformTypeEnum.own)
        );
        for (CurrencyTokenOrder i : list) {
            asyncService.async(() -> {
                currencyTokenOrderService.cancel(i);
            });
        }
        return Result.instance();
    }

    /**
     * 委托列表
     *
     * @param page
     * @param size
     * @param stock
     * @param fiat
     * @param status
     * @return
     */
    @GetMapping("/purchase/order/list")
    public Result purchaseOrderList(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                    @RequestParam(value = "size", defaultValue = "10") Integer size,
                                    CurrencyCoinEnum stock, CurrencyCoinEnum fiat, CurrencyTokenOrderStatus status
    ) {
        List<CurrencyTokenOrderStatus> statuses = getStatus(status);
        Long uid = requestInitService.uid();
        List<CurrencyTokenOrder> list = currencyTokenOrderService.page(new Page<>(page, size),
                new LambdaQueryWrapper<CurrencyTokenOrder>()
                        .eq(CurrencyTokenOrder::getUid, uid)
                        .eq(Objects.nonNull(fiat), CurrencyTokenOrder::getToken_fiat, fiat)
                        .eq(Objects.nonNull(stock), CurrencyTokenOrder::getToken_stock, stock)
                        .in(CollUtil.isNotEmpty(statuses), CurrencyTokenOrder::getStatus, statuses)
                        .orderByDesc(CurrencyTokenOrder::getCreate_time)
        ).getRecords();
        long count = currencyTokenOrderService.count(
                new LambdaQueryWrapper<CurrencyTokenOrder>()
                        .eq(CurrencyTokenOrder::getUid, uid)
                        .eq(Objects.nonNull(fiat), CurrencyTokenOrder::getToken_fiat, fiat)
                        .eq(Objects.nonNull(stock), CurrencyTokenOrder::getToken_stock, stock)
                        .in(CollUtil.isNotEmpty(statuses), CurrencyTokenOrder::getStatus, statuses)
        );
        return Result.instance().setList(list.stream().map(CurrencyTokenOrderPageDTO::trans).collect(Collectors.toList()), count);
    }

    private List<CurrencyTokenOrderStatus> getStatus(CurrencyTokenOrderStatus status) {
        List<CurrencyTokenOrderStatus> statuses = null;
        if (ObjectUtil.isNotNull(status)) {
            switch (status) {
                case created:
                    statuses = Arrays.asList(CurrencyTokenOrderStatus.created, CurrencyTokenOrderStatus.partial_deal);
                    break;
                case canceled:
                    statuses = Arrays.asList(CurrencyTokenOrderStatus.canceled, CurrencyTokenOrderStatus.canceled_partial_deal);
                    break;
                case success:
                    statuses = Arrays.asList(CurrencyTokenOrderStatus.partial_deal, CurrencyTokenOrderStatus.canceled_partial_deal, CurrencyTokenOrderStatus.success);
                    break;
            }
        }
        return statuses;
    }

    /**
     * 购买现货2.0
     *
     * @param purchaseTokenDTO
     * @return
     */
    @PostMapping("/purchase/order")
    @Transactional
    public Result purchaseOrder(@RequestBody @Valid PurchaseTokenDTO purchaseTokenDTO) {
        Long uid = requestInitService.uid();
        Long id = CommonFunction.generalId();
        if (purchaseTokenDTO.getType().equals(TokenOrderType.limit)) {
            if (ObjectUtil.isNull(purchaseTokenDTO.getLimit_price()) || purchaseTokenDTO.getLimit_price().compareTo(BigDecimal.ZERO) <= 0) {
                throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
            }
            currencyTokenOrderService.freeze(uid, id, purchaseTokenDTO);
        }
        check(purchaseTokenDTO.getStock());
        CurrencyTokenOrder currencyTokenOrder = CurrencyTokenOrder.builder()
                .amount(purchaseTokenDTO.getAmount())
                .uid(uid)
                .amount_unit(purchaseTokenDTO.getAmount_unit())
                .direction(purchaseTokenDTO.getDirection())
                .id(id)
                .type(purchaseTokenDTO.getType())
                .price(purchaseTokenDTO.getType().equals(TokenOrderType.market) ? BigDecimal.ZERO : purchaseTokenDTO.getLimit_price())
                .status(CurrencyTokenOrderStatus.created)
                .token_fiat(purchaseTokenDTO.getFiat())
                .token_stock(purchaseTokenDTO.getStock())
                .platform_type(PlatformTypeEnum.bian)
                .deal_price(BigDecimal.ZERO)
                .deal_amount(BigDecimal.ZERO)
                .deal_tr_amount(BigDecimal.ZERO)
                .create_time(requestInitService.now()).deal_time(requestInitService.now())
                .build();
        currencyTokenOrderService.save(currencyTokenOrder);
        switch (purchaseTokenDTO.getType()) {
            case market:
                currencyTokenOrderService.tradeMarket(currencyTokenOrder);
                break;
            case limit:
                currencyTokenOrderService.tradeLimit(currencyTokenOrder);
                break;
        }
        return Result.instance();
    }

    private void check(CurrencyCoinEnum stock) {
        if (ObjectUtil.isNull(stock)) {
            throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
        }
        TokenList tokenList = tokenListService.getByToken(stock);
        if (ObjectUtil.isNull(tokenList) || tokenList.getPlatform_token()) {
            throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
        }
    }

    /**
     * 现货交易记录
     *
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/trade/log/list")
    public Result tradeLog(@RequestParam(value = "page", defaultValue = "1") Integer page,
                           @RequestParam(value = "size", defaultValue = "10") Integer size) {
        long uid = requestInitService.uid();

        List<TradeTokenLog> list = tradeTokenLogService.page(new Page<>(page, size), new LambdaQueryWrapper<TradeTokenLog>()
                .eq(TradeTokenLog::getUid, uid).orderByDesc(TradeTokenLog::getCreate_time)
        ).getRecords();
        long count = tradeTokenLogService.count(new LambdaQueryWrapper<TradeTokenLog>().eq(TradeTokenLog::getUid, uid));

        return Result.instance().setList(list.stream().map(TradeLogPage::trans).collect(Collectors.toList()), count);
    }

    /**
     * 现货提现
     *
     * @param withdrawDTO
     * @return
     */
    @PostMapping("/withdraw")
    @GrcPrivilege(mode = GrcCheckModular.提现)
    public Result withdraw(@RequestBody @Valid WithdrawDTO withdrawDTO) {
        User user = userService.my();
        Integer credit_score = user.getCredit_score();
        if (credit_score < 100) {
            ErrorCodeEnum.NO_CREDIT_SCORE.throwException();
        }
//        captchaPhoneService.verify(user.getUsername(), CaptchaPhoneType.withdraw, withdrawDTO.getCode());
        captchaEmailService.verify(user.getUsername(), CaptchaPhoneType.withdraw, withdrawDTO.getCode());
        withdrawDTO.setType(CurrencyTypeEnum.actual);
        withdrawService.withdraw(withdrawDTO);
        return Result.instance();
    }

    /**
     * 现货提现记录
     *
     * @param status
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/withdraw/list")
    public Result withdrawList(@RequestParam(value = "status", defaultValue = "created") ChargeStatus status,
                               @RequestParam(value = "page", defaultValue = "1") Integer page,
                               @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Long uid = requestInitService.uid();
        List<SGCharge> sgChargeList = sgChargeService.page(new Page<>(page, size), new LambdaQueryWrapper<SGCharge>()
                .eq(SGCharge::getStatus, status).eq(SGCharge::getUid, uid)
                .orderByDesc(SGCharge::getCreate_time)).getRecords();
        long count = sgChargeService.count(new LambdaQueryWrapper<SGCharge>()
                .eq(SGCharge::getStatus, status).eq(SGCharge::getUid, uid));
        return Result.instance().setList(sgChargeList.stream().map(WithdrawApplyChargeVO::trans).collect(Collectors.toList()), count);
    }

    /**
     * 现货提现链信息
     *
     * @param token
     * @return
     * @throws IOException
     */
    @GetMapping("/withdraw/chain")
    public Result withdrawInfo(CurrencyCoinEnum token) throws IOException {
        Long uid = requestInitService.uid();
        Address address = addressService.get_(uid, CurrencyTypeEnum.normal);
        List<TokenContract> list = tokenContractService.list(new LambdaQueryWrapper<TokenContract>()
                .eq(TokenContract::getToken, token)
        );

        List<ChainInfoDTO> result = list.stream().map(o -> {
            String chain_address = null;
            Object chain_info = ChainInfoDTO.chainInfos.get(o.getChain().name());
            switch (o.getChain()) {
                case bep20:
                    chain_address = address.getBsc();
                    break;
                case erc20:
                    chain_address = address.getEth();
                    break;
                case trc20:
                    chain_address = address.getTron();
                    break;
            }
            String withdraw_rate = configService.get(ConfigConstants.ACTUAL_WITHDRAW_RATE);
            return ChainInfoDTO.builder()
                    .address(chain_address)
                    .chain_info(chain_info)
                    .min_recharge("0.0001")
                    .withdraw_rate(o.getWithdraw_rate().toString())
                    .withdraw_fixed_amount(o.getWithdraw_fixed_amount().toString())
                    .withdraw_min_amount(o.getWithdraw_min_amount().toString())
                    .chain(o.getChain()).build();
        }).collect(Collectors.toList());

        return Result.instance().setData(result);
    }

    /**
     * 现货提现手续费
     *
     * @param token
     * @return
     */
    @GetMapping("/withdraw/config")
    public Result withdrawConfig(CurrencyCoinEnum token) {
        String config = configService.get(ConfigConstants.ACTUAL_WITHDRAW_RATE);
        return Result.instance().setData(MapTool.Map().put(ConfigConstants.ACTUAL_WITHDRAW_RATE, config));
    }

    /**
     * 现货流水
     *
     * @param page
     * @param size
     * @param des
     * @return
     */
    @GetMapping("/log/list")
    public Result currencyTokenLogList(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                       @RequestParam(value = "size", defaultValue = "10") Integer size,
                                       CurrencyLogDes des) {
        long uid = requestInitService.uid();
        List<CurrencyTokenLog> list = currencyTokenLogService.page(new Page<>(page, size), new LambdaQueryWrapper<CurrencyTokenLog>()
                .eq(CurrencyTokenLog::getUid, uid)
                .in(CurrencyTokenLog::getLog_type, Lists.newArrayList(CurrencyLogType.reduce, CurrencyLogType.decrease, CurrencyLogType.increase, CurrencyLogType.withdraw))
                .in(CurrencyTokenLog::getDes, Lists.newArrayList(CurrencyLogDes.划入, CurrencyLogDes.划出, CurrencyLogDes.提现, CurrencyLogDes.提现手续费, CurrencyLogDes.充值))
                .eq(Objects.nonNull(des), CurrencyTokenLog::getDes, des)
                .orderByDesc(CurrencyTokenLog::getCreate_time)
        ).getRecords();
        long count = currencyTokenLogService.count(new LambdaQueryWrapper<CurrencyTokenLog>()
                .eq(CurrencyTokenLog::getUid, uid)
                .in(CurrencyTokenLog::getLog_type, Lists.newArrayList(CurrencyLogType.reduce, CurrencyLogType.decrease, CurrencyLogType.increase, CurrencyLogType.withdraw))
                .in(CurrencyTokenLog::getDes, Lists.newArrayList(CurrencyLogDes.划入, CurrencyLogDes.划出, CurrencyLogDes.提现, CurrencyLogDes.提现手续费, CurrencyLogDes.充值))
        );
        return Result.instance().setList(list.stream().map(CurrencyTokenLogPage::trans).collect(Collectors.toList()), count);
    }


//    private static List<Object> chainInfo = Arrays.asList(
//            MapTool.Map().put("chain", "bep20").put("min", 0.001),
//            MapTool.Map().put(""),
//            MapTool.Map().put(""),
//    )

    @Resource
    CoinProcessor coinProcessor;

    @Resource
    private TokenFavoriteService tokenFavoriteService;
    @Resource
    private RequestInitService requestInitService;
    @Resource
    private KLineService kLineService;
    @Resource
    private CurrencyTokenService currencyTokenService;
    @Resource
    private TokenDealService tokenDealService;
    @Resource
    private TradeTokenLogService tradeTokenLogService;
    @Resource
    private UserService userService;
    @Resource
    private CaptchaEmailService captchaEmailService;
    @Resource
    private WithdrawService withdrawService;
    @Resource
    private CurrencyTokenLogService currencyTokenLogService;
    @Resource
    private TokenListService tokenListService;
    @Resource
    private ConfigService configService;
    @Resource
    private CurrencyTokenOrderService currencyTokenOrderService;
    @Resource
    private AsyncService asyncService;
    @Resource
    private TokenContractService tokenContractService;
    @Resource
    private AddressService addressService;
    @Resource
    private SGChargeService sgChargeService;
}
