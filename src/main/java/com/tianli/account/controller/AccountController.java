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
import com.tianli.chain.entity.CoinBase;
import com.tianli.chain.enums.ChainType;
import com.tianli.chain.service.CoinBaseService;
import com.tianli.chain.service.CoinService;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeGroup;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.ChargeService;
import com.tianli.charge.service.OrderChargeTypeService;
import com.tianli.charge.vo.OrderChargeInfoVO;
import com.tianli.common.PageQuery;
import com.tianli.common.RedisConstants;
import com.tianli.common.RedisLockConstants;
import com.tianli.common.annotation.AppUse;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.common.lock.RedissonClientTool;
import com.tianli.common.webhook.WebHookService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.openapi.query.UserTransferQuery;
import com.tianli.rpc.RpcService;
import com.tianli.rpc.dto.UserInfoDTO;
import com.tianli.sso.init.RequestInitService;
import com.tianli.sso.init.SignUserInfo;
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
    private OrderChargeTypeService orderChargeTypeService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RpcService rpcService;
    @Resource
    private RedissonClientTool redissonClientTool;
    @Resource
    private CoinBaseService coinBaseService;

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
    public Result<Set<AddressBookDTO>> transferBook() {
        Long uid = requestInitService.uid();
        String key = RedisConstants.ACCOUNT_TRANSFER_ADDRESS_BOOK + uid; // 获取
        Set<String> range = stringRedisTemplate.opsForZSet().range(key, 0, -1);
        range = Optional.ofNullable(range).orElse(Collections.emptySet());
        Set<AddressBookDTO> result = range.stream().map(str -> JSONUtil.toBean(str, AddressBookDTO.class)).collect(Collectors.toSet());
        return Result.success(result);
    }

    @DeleteMapping("/transfer/book")
    public Result<Void> transferBookDelete(@RequestBody IdsQuery idsQuery) {
        Long uid = requestInitService.uid();
        String key = RedisConstants.ACCOUNT_TRANSFER_ADDRESS_BOOK + uid; // 删除
        stringRedisTemplate.opsForZSet().removeRangeByScore(key, idsQuery.getId(), idsQuery.getId());
        return Result.success();
    }

    @PostMapping("/transfer/check")
    public Result<Void> transferCheck(@RequestBody IdsQuery idsQuery) {
        UserInfoDTO userInfoDTO = rpcService.userInfoDTOChatId(idsQuery.getChatId());
        if (Objects.isNull(userInfoDTO)) {
            ErrorCodeEnum.USER_NOT_FUND_CHAT.throwException();
        }
        return Result.success();
    }

    @GetMapping("/transfer/config")
    public Result transferConfig(String coin){
        CoinBase coinBase = coinBaseService.getById(coin);
        if (Objects.isNull(coinBase))ErrorCodeEnum.throwException("币种不存在");
        CoinBase base = CoinBase.builder()
                .withdrawMin(coinBase.getWithdrawMin())
                .withdrawDecimals(coinBase.getWithdrawDecimals())
                .build();
        return Result.success(base);
    }

    /**
     * id 转账
     * @param query
     * @return
     */
    @PostMapping("/transfer")
    public Result<AccountTransferVO> transfer(@RequestBody AccountTransferQuery query) {
        String coin = query.getCoin();
        CoinBase coinBase = coinBaseService.getById(coin);
        if(Objects.isNull(coinBase))ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        int withdrawDecimals = coinBase.getWithdrawDecimals();
        BigDecimal withdrawMin = coinBase.getWithdrawMin();
        BigDecimal amount = query.getAmount();
        if (amount.scale() > withdrawDecimals)ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        if ((amount.compareTo(withdrawMin) < 0))ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        Long uid = requestInitService.uid();
        Long chatId = requestInitService.userInfo().getChatId();
        if (Objects.isNull(chatId)) {
            ErrorCodeEnum.ACCOUNT_ERROR.throwException();
        }

        UserInfoDTO userInfoDTO = rpcService.userInfoDTOChatId(query.getToChatId());
        if (Objects.isNull(userInfoDTO)) {
            throw ErrorCodeEnum.USER_NOT_FUND_CHAT.generalException();
        }

        if(chatId.equals(userInfoDTO.getChatId())){
            ErrorCodeEnum.WITHDRAW_RECHARGE_SAME_ADDRESS.throwException();
        }

        String repeatCheckKey = RedisConstants.ACCOUNT_TRANSFER_REPEAT
                + query.getToChatId() + ":" + query.getCoin() + ":" + query.getAmount().toPlainString();
        if (query.isRepeatCheck()) {
            Boolean hasKey = stringRedisTemplate.hasKey(repeatCheckKey);
            return Result.success(AccountTransferVO.builder().repeat(Boolean.TRUE.equals(hasKey)).build());
        }

        UserTransferQuery userTransferQuery = UserTransferQuery.builder()
                .receiveChatId(userInfoDTO.getChatId())
                .transferChatId(chatId)
                .transferUid(uid)
                .receiveUid(userInfoDTO.getId())
                .amount(query.getAmount())
                .coin(query.getCoin())
                .chargeType(ChargeType.assure_withdraw).build();

        String key = RedisLockConstants.LOCK_TRANSFER + uid;

        redissonClientTool.tryLock(key, () -> accountUserTransferService.transfer(userTransferQuery), ErrorCodeEnum.SYSTEM_BUSY);

        if (query.isAddressBook()) {
            String addressBookRemarks = MoreObjects.firstNonNull(query.getAddressBookRemarks(), query.getToChatId() + "");
            AddressBookDTO addressBookDTO = AddressBookDTO.builder().chatId(query.getToChatId()).remarks(addressBookRemarks).build();
            stringRedisTemplate.opsForZSet()
                    .add(RedisConstants.ACCOUNT_TRANSFER_ADDRESS_BOOK + uid // 新增
                            , JSONUtil.toJsonStr(addressBookDTO), query.getToChatId());
        }

        stringRedisTemplate.opsForValue().set(repeatCheckKey, "transfer", 5, TimeUnit.MINUTES);
        return new Result<>(new AccountTransferVO());
    }


}
