package com.tianli.account.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.base.MoreObjects;
import com.tianli.account.entity.AccountBalanceOperationLog;
import com.tianli.account.query.*;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.account.service.AccountUserTransferService;
import com.tianli.account.vo.*;
import com.tianli.address.service.AddressService;
import com.tianli.address.mapper.Address;
import com.tianli.address.vo.AddressVO;
import com.tianli.chain.entity.Coin;
import com.tianli.chain.enums.ChainType;
import com.tianli.chain.service.CoinService;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeGroup;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.ChargeService;
import com.tianli.charge.vo.OrderChargeInfoVO;
import com.tianli.charge.service.IOrderChargeTypeService;
import com.tianli.common.PageQuery;
import com.tianli.common.RedisConstants;
import com.tianli.common.annotation.AppUse;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.common.webhook.WebHookService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.openapi.query.UserTransferQuery;
import com.tianli.sso.init.RequestInitService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author chenb
 * @apiNote 用户账户控制器
 * @since 2022-07-06
 **/
@RestController
@RequestMapping("/account")
public class AccountController {

    @Resource
    private AddressService addressService;
    @Resource
    private RequestInitService requestInitService;
    @Resource
    private AccountBalanceService accountBalanceService;
    @Resource
    private ChargeService chargeService;
    @Resource
    private WebHookService webHookService;
    @Resource
    private CoinService coinService;
    @Resource
    private AccountUserTransferService accountUserTransferService;
    @Resource
    private IOrderChargeTypeService orderChargeTypeService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 激活钱包
     */
    @AppUse
    @PostMapping("/activate")
    public Result<Address> activateWallet() {
        Long uid = requestInitService.uid();
        return new Result<>(addressService.activityAccount(uid));
    }

    /**
     * 激活钱包
     */
    @AppUse
    @PostMapping("/activate/uid")
    public Result<Address> activateWalletByUid(@RequestBody(required = false) IdsQuery idsQuery) {
        if (Objects.isNull(idsQuery.getUid())) {
            ErrorCodeEnum.ACCOUNT_ACTIVATE_UID_NULL.throwException();
        }

        return new Result<>(addressService.activityAccount(idsQuery.getUid()));
    }

    /**
     * 激活钱包
     */
    @PostMapping("/activate/uids")
    public Result<Void> activateWalletByUids(@RequestBody IdsQuery idsQuery) {
        try {
            addressService.activityAccount(idsQuery);
        } catch (Exception e) {
            webHookService.dingTalkSend("激活程序异常", e);
            throw e;
        }
        return Result.success();
    }

    /**
     * 钱包激活状态
     */
    @AppUse
    @GetMapping("/status")
    public Result<Map<String, Boolean>> status() {
        Long uid = requestInitService.uid();
        Address address = addressService.get(uid);
        Map<String, Boolean> result = new HashMap<>();
        result.put("activate", Objects.nonNull(address));
        return Result.success(result);
    }

    /**
     * 用户钱包地址
     */
    @AppUse
    @GetMapping("/address")
    public Result<AddressVO> address() {
        Long uid = requestInitService.uid();
        Address address = addressService.get(uid);
        if (Objects.isNull(address)) {
            address = addressService.activityAccount(uid);
        }
        return new Result<>(AddressVO.trans(address));
    }

    /**
     * 根据链获取钱包地址 非eth bsc tron等常用链 采用懒加载模式生成对应链的地址
     *
     * @param chain 所属链
     */
    @GetMapping("/address/{chain}")
    public Result<String> address(@PathVariable("chain") ChainType chain) {
        Long uid = requestInitService.uid();
        return Result.success(addressService.get(uid, chain));
    }

    /**
     * 主钱包地址
     */
    @GetMapping("/address/config")
    public Result<AddressVO> addressConfig() {
        return Result.success(AddressVO.trans(addressService.getConfigAddress()));
    }

    /**
     * 手续费
     */
    @GetMapping("/service/amount")
    public Result<HashMap<String, String>> serviceRate(String coin, NetworkType networkType) {
        if (StringUtils.isBlank(coin)) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        Coin coinEntity = coinService.getByNameAndNetwork(coin, networkType);
        BigDecimal withdrawFixedAmount = coinEntity.getWithdrawFixedAmount();
        HashMap<String, String> rateMap = new HashMap<>();
        rateMap.put("serviceAmount", withdrawFixedAmount.toPlainString());
        return Result.success(rateMap);
    }

    /**
     * 最低提币
     */
    @GetMapping("/withdraw/limit")
    public Result<HashMap<String, String>> withdrawLimit(String coin, NetworkType networkType) {
        if (StringUtils.isBlank(coin)) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        Coin coinEntity = coinService.getByNameAndNetwork(coin, networkType);
        var withdrawMin = coinEntity.getWithdrawMin();
        HashMap<String, String> rateMap = new HashMap<>();
        rateMap.put("withdrawLimitAmount", withdrawMin.toPlainString());
        return Result.success(rateMap);
    }

    /**
     * 【云钱包】总资产 + 账户列表
     */
    @AppUse
    @GetMapping("/balance/summary")
    public Result<AccountBalanceMainPageVO> accountBalance() {
        Long uid = requestInitService.uid();
        return new Result<>(accountBalanceService.accountSummary(uid, true, 0));
    }

    /**
     * 【云钱包】总资产 + 账户列表
     */
    @AppUse
    @GetMapping("/balance/summary/dynamic")
    public Result<AccountBalanceMainPageVO> accountBalanceDynamic(Integer version) {
        version = MoreObjects.firstNonNull(version, 0);
        Long uid = requestInitService.uid();
        return new Result<>(accountBalanceService.accountSummary(uid, version));
    }

    /**
     * 【云钱包】 账户列表
     */
    @GetMapping("/balances")
    public Result<List<AccountBalanceVO>> balances() {
        Long uid = requestInitService.uid();
        return Result.success(accountBalanceService.accountList(uid));
    }

    /**
     * 【云钱包】币别详情账户余额
     */
    @AppUse
    @GetMapping("/balance/{coin}")
    public Result<AccountBalanceVO> accountBalance(@PathVariable String coin) {
        Long uid = requestInitService.uid();
        return new Result<>(accountBalanceService.accountSingleCoin(uid, coin));
    }

    /**
     * 【云钱包】币别详情下方详情列表
     */
    @AppUse
    @GetMapping("/balance/details")
    public Result<IPage<OrderChargeInfoVO>> accountBalanceDetails(PageQuery<Order> pageQuery, AccountDetailsQuery query) {
        Long uid = requestInitService.uid();
        query = MoreObjects.firstNonNull(query, new AccountDetailsQuery());

        query.setUid(uid);
        return new Result<>(chargeService.pageByChargeGroup(query, pageQuery.page()));
    }

    /**
     * 【云钱包】交易类型
     */
    @AppUse
    @GetMapping("/transaction/type")
    public Result<List<TransactionGroupTypeVO>> transactionType() {
        Long uid = requestInitService.uid();
        return new Result<>(chargeService.listTransactionGroupType(uid, List.of(ChargeGroup.receive, ChargeGroup.pay)));
    }


    /**
     * 【云钱包】流水新交易类型
     */
    @GetMapping("/transaction/newType")
    public Result<List<OrderChargeTypeVO>> newType() {
        Long uid = requestInitService.uid();
        return new Result<>(orderChargeTypeService.listChargeType(uid));
    }

    /**
     * 【云钱包】币别详情下方流水列表
     */
    @GetMapping("/balance/newDetails")
    public Result<IPage<AccountBalanceOperationLogVO>> accountNewDetails(PageQuery<AccountBalanceOperationLog> pageQuery
            , AccountDetailsNewQuery query) {
        Long uid = requestInitService.uid();
        query = MoreObjects.firstNonNull(query, new AccountDetailsNewQuery());
        return new Result<>(chargeService.newPageByChargeGroup(uid, query, pageQuery.page()));
    }


    @GetMapping("/transfer/book")
    public Result<List<AddressBookDTO>> transferBoos() {
        Long uid = requestInitService.uid();
        String key = RedisConstants.ACCOUNT_TRANSFER_ADDRESS_BOOK + uid;
        List<String> range = stringRedisTemplate.opsForList().range(key, 0, -1);
        range = Optional.ofNullable(range).orElse(Collections.emptyList());
        List<AddressBookDTO> result = range.stream().map(str -> JSONUtil.toBean(str, AddressBookDTO.class)).collect(Collectors.toList());
        return Result.success(result);
    }

    @PostMapping("/transfer")
    public Result<AccountTransferVO> transfer(AccountTransferQuery query) {
        // todo 校验聊天id
        Long uid = requestInitService.uid();
        Long chatId = requestInitService.userInfo().getChatId();
        if (Objects.isNull(chatId)) {
            ErrorCodeEnum.ACCOUNT_ERROR.throwException();
        }

        String repeatCheckKey = RedisConstants.ACCOUNT_TRANSFER_REPEAT
                + query.getToChatId() + ":" + query.getCoin() + ":" + query.getAmount().toPlainString();
        if (query.isRepeatCheck()) {
            String s = stringRedisTemplate.opsForValue().get(repeatCheckKey);
            return Result.success(AccountTransferVO.builder().repeat(Objects.isNull(s)).build());
        }

        UserTransferQuery userTransferQuery = UserTransferQuery.builder()
                .transferUid(uid)
                .receiveUid(null)
                .amount(query.getAmount())
                .coin(query.getCoin())
                .chargeType(ChargeType.transfer_reduce).build();
        accountUserTransferService.transfer(userTransferQuery);

        if (query.isAddressBook()) {
            String addressBookRemarks = MoreObjects.firstNonNull(query.getAddressBookRemarks(), query.getToChatId() + "");
            AddressBookDTO addressBookDTO = AddressBookDTO.builder().chatId(query.getToChatId()).remarks(addressBookRemarks).build();
            stringRedisTemplate.opsForList().rightPush(RedisConstants.ACCOUNT_TRANSFER_ADDRESS_BOOK + uid, JSONUtil.toJsonStr(addressBookDTO));
        }

        stringRedisTemplate.opsForValue().set(repeatCheckKey, "transfer", 5, TimeUnit.MINUTES);
        return new Result<>(new AccountTransferVO());
    }


}
