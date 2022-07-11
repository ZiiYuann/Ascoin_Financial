package com.tianli.charge;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tianli.account.entity.AccountBalance;
import com.tianli.account.enums.ProductType;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.address.AddressService;
import com.tianli.address.mapper.Address;
import com.tianli.address.query.RechargeCallbackQuery;
import com.tianli.charge.controller.ChargeWebhooksDTO;
import com.tianli.charge.controller.WithdrawDTO;
import com.tianli.charge.converter.ChargeConverter;
import com.tianli.charge.dto.StatChargeAmount;
import com.tianli.charge.entity.Charge;
import com.tianli.charge.mapper.ChargeMapper;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.vo.ChargeVO;
import com.tianli.common.CommonFunction;
import com.tianli.common.ConfigConstants;
import com.tianli.common.async.AsyncService;
import com.tianli.common.blockchain.TronTriggerContract;
import com.tianli.common.blockchain.UsdtBscContract;
import com.tianli.common.blockchain.UsdtEthContract;
import com.tianli.sso.init.RequestInitService;
import com.tianli.common.log.LoggerHandle;
import com.tianli.currency.DigitalCurrency;
import com.tianli.currency.enums.CurrencyAdaptType;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.mconfig.ConfigService;
import com.tianli.sso.RequestInitService;
import com.tianli.tool.MapBuilder;
import com.tianli.tool.MapTool;
import com.tianli.tool.crypto.Crypto;
import com.tianli.tool.http.HttpHandler;
import com.tianli.tool.http.HttpRequest;
import com.tianli.tool.judge.JsonObjectTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.crypto.util.DigestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author wangqiyun
 * @since 2020/3/31 11:25
 */

@Slf4j
@Service
public class ChargeService extends ServiceImpl<ChargeMapper, Charge> {

    public static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    public static final BigDecimal TEN_BILLION = new BigDecimal("10000000000");

    @Resource
    private ChargeMapper chargeMapper;
    @Resource
    private UsdtBscContract usdtBscContract;
    @Resource
    private UsdtEthContract usdtEthContract;
    @Resource
    private LoggerHandle loggerHandle;
    @Resource
    private AsyncService asyncService;
    @Resource
    private ConfigService configService;
    @Resource
    private RequestInitService requestInitService;
    @Resource
    private AccountBalanceService accountBalanceService;
    @Resource
    private AddressService addressService;
    @Resource
    private TronTriggerContract tronTriggerContract;
    @Resource
    private ChargeConverter chargeConverter;

    /**
     * 充值回调:添加用户余额和记录
     *
     * @param query 充值信息
     */
    @Transactional
    public void rechargeCallback(RechargeCallbackQuery query) {
        Address address  = getAddress(query);
        Long uid = address.getUid();

        BigDecimal finalAmount = query.getType().moneyBigDecimal(query.getValue());

        boolean receiveFlag = false;
        if (chargeMapper.getBySn(query.getSn()) == null) {
            receiveFlag = financialRechargeReceive(query, finalAmount, query.getValue(), query.getType());
        }

        if (receiveFlag) {
            accountBalanceService.increase(uid, , query.getType(), finalAmount, query.getSn(), CurrencyLogDes.充值.name());
        }

    }

    @Transactional
    public void withdrawWebhooks(ChargeWebhooksDTO chargeWebhooksDTO) {
        String sn = chargeWebhooksDTO.getSn();
        if (StringUtils.isBlank(sn)) {
            return;
        }
        ProductType type = null;
        // TODO 根据订单好设置类型
        if (sn.startsWith("CD")) {
            type = ProductType.financial;
        }
        boolean update = false;
        CurrencyAdaptType currencyAdaptType = null;
        Long uid = null;
        //链上交易成功
        if ("success".equals(chargeWebhooksDTO.getStatus())) {
            BigDecimal realAmount = BigDecimal.ZERO;
            BigDecimal fee = BigDecimal.ZERO;
            CurrencyLogDes logDes = null;
            //用户的提现
            if (type == ProductType.financial) {
                Charge charge = chargeMapper.getBySn(sn);
                if (charge == null) return;
                BigInteger minerFee = chargeWebhooksDTO.getMinerFee();
                CurrencyAdaptType minerFeeType = chargeWebhooksDTO.getMinerFeeType();
                currencyAdaptType = charge.getCurrencyAdaptType();
                realAmount = charge.getRealAmount();
                fee = charge.getFee();
                logDes = CurrencyLogDes.提现;
                uid = charge.getUid();
                //修改提现表的状态
                update = chargeMapper.success(LocalDateTime.now(), ChargeStatus.chain_success, charge.getId(), ChargeStatus.chaining, minerFee, minerFeeType) > 0;
            }

            if (update) {
                realAmount = currencyAdaptType.moneyBigDecimal(realAmount.toBigInteger());
                fee = currencyAdaptType.moneyBigDecimal(fee.toBigInteger());

                accountBalanceService.reduce(uid, type, currencyAdaptType, realAmount, sn, logDes.name());
                accountBalanceService.reduce(uid, type, currencyAdaptType, fee, sn, CurrencyLogDes.提现手续费.name());
            }
        }
        //链上交易失败
        else if ("fail".equals(chargeWebhooksDTO.getStatus())) {
            BigDecimal amount;
            CurrencyLogDes logDes;
            //修改订单状态
            if (type == ProductType.financial) {
                Charge charge = chargeMapper.getBySn(sn);
                if (charge == null) return;
                currencyAdaptType = charge.getCurrencyAdaptType();
                amount = charge.getAmount();
                logDes = CurrencyLogDes.提现;
                uid = charge.getUid();
                update = chargeMapper.fail(LocalDateTime.now(), ChargeStatus.chain_fail, charge.getId(), ChargeStatus.chaining, chargeWebhooksDTO.getMinerFee(), chargeWebhooksDTO.getMinerFeeType()) > 0;
            } else {
                return;
            }
            //返还冻结余额
            if (update) {
                amount = currencyAdaptType.moneyBigDecimal(amount.toBigInteger());
                accountBalanceService.unfreeze(uid, type, currencyAdaptType, amount.multiply(ONE_HUNDRED), sn, logDes.name());
            }
        }
    }

    @Transactional
    public void withdraw(WithdrawDTO withdrawDTO) {
        Long uid = requestInitService.uid();
        String fromAddress = null;
        CurrencyAdaptType currencyAdaptType = withdrawDTO.getCurrencyAdaptType();
        //根据提现币种 选择对应的链的主钱包地址
        switch (currencyAdaptType) {
            case usdt_trc20:
            case usdc_trc20:
                fromAddress = configService.get(ConfigConstants.TRON_MAIN_WALLET_ADDRESS);
                break;
            case usdt_erc20:
            case usdc_erc20:
                fromAddress = configService.get(ConfigConstants.ETH_MAIN_WALLET_ADDRESS);
                break;
            case BF_bep20:
            case usdt_bep20:
            case bsc:
            case usdc_bep20:
                fromAddress = configService.get(ConfigConstants.BSC_MAIN_WALLET_ADDRESS);
                break;
            default:
                break;
        }
        if (fromAddress == null) ErrorCodeEnum.SYSTEM_ERROR.throwException();
        long id = CommonFunction.generalId();
        // 最小提现金额
        String withdrawMinAmount = configService._get(currencyAdaptType.name() + "_withdraw_min_amount");
        BigDecimal amount = currencyAdaptType.moneyBigDecimal(new BigInteger("" + withdrawDTO.getAmount()));
        if (amount.compareTo(new BigDecimal(withdrawMinAmount)) < 0) {
            ErrorCodeEnum.throwException("提现usdt数额过小");
        }

        //计算手续费  实际手续费 = 提现数额*手续费率 + 固定手续费数额
        String rate = configService._get(currencyAdaptType.name() + "_withdraw_rate");
        String fixedAmount = configService._get(currencyAdaptType.name() + "_withdraw_fixed_amount");
        BigDecimal fee = (amount.multiply(new BigDecimal(StringUtils.isNotBlank(rate) ? rate : "0")))
                .add(new BigDecimal(StringUtils.isNotBlank(fixedAmount) ? fixedAmount : "0"));
        //实际到账数额 = 提现数额 - 手续费
        BigDecimal realAmount = amount.subtract(fee);
        if (fee.compareTo(BigDecimal.ZERO) < 0)
            ErrorCodeEnum.FEE_LT_ZERO_ERROR.throwException();
        if (realAmount.compareTo(BigDecimal.ZERO) < 0)
            ErrorCodeEnum.WITHDRAWAL_AMOUNT_LT_FEE_ERROR.throwException();
        //
        JsonObject userInfo = requestInitService.get().getUserInfo();

        //创建提现订单
        Charge charge = Charge.builder()
                .id(id).createTime(LocalDateTime.now())
                .status(ChargeStatus.created)
                .uid(uid)
                .uidAvatar(JsonObjectTool.getAsString(userInfo, "avatar"))
                .uidNick(JsonObjectTool.getAsString(userInfo, "nick"))
                .uidUsername(JsonObjectTool.getAsString(userInfo, "username"))
                .sn("C" + CommonFunction.generalSn(id))
                .currencyAdaptType(currencyAdaptType)
                .chargeType(ChargeType.withdraw)
                .amount(amount)
                .fee(fee)
                .realAmount(realAmount)
                .fromAddress(fromAddress).toAddress(withdrawDTO.getAddress())
                .token(currencyAdaptType)
                .build();
        if (chargeMapper.insert(charge) <= 0) ErrorCodeEnum.SYSTEM_ERROR.throwException();
        //转换u后面的0的个数
        if (Objects.equals(currencyAdaptType, CurrencyAdaptType.usdt_erc20)
                || Objects.equals(currencyAdaptType, CurrencyAdaptType.usdt_trc20)
                || Objects.equals(currencyAdaptType, CurrencyAdaptType.usdc_erc20)
                || Objects.equals(currencyAdaptType, CurrencyAdaptType.usdc_trc20)) {
            amount = amount.multiply(ONE_HUNDRED);
        } else if (Objects.equals(currencyAdaptType, CurrencyAdaptType.usdt_bep20)
                || Objects.equals(currencyAdaptType, CurrencyAdaptType.usdc_bep20)
        ) {
            amount = amount.divide(TEN_BILLION, 8, RoundingMode.HALF_UP);
        }
        //冻结提现数额
        accountBalanceService.freeze(uid, ProductType.financial, currencyAdaptType, amount, charge.getSn(), CurrencyLogDes.提现.name());
    }

    /**
     * 获取充值DTO数据 不同链的usdt后面的0个数不一样  需要做一个对齐处理 目前是后面8个0为1个u
     */
    private Address getAddress(RechargeCallbackQuery query) {
        String addressQuery = query.getFromAddress();
        Address address = null;
        switch (query.getType()) {
            case usdt_erc20:
            case usdc_erc20:
                address = addressService.getByEth(addressQuery);
                break;
            case usdt_bep20:
            case usdc_bep20:
                address = addressService.getByBsc(addressQuery);
                break;
            case usdt_trc20:
            case usdc_trc20:
                address = addressService.getByTron(addressQuery);
                break;
            default:
                ErrorCodeEnum.CURRENCY_NOT_SUPPORT.throwException();
        }

        return address;
    }

    /**
     * 理财充值记录添加
     */
    private boolean financialRechargeReceive(RechargeCallbackQuery query,
                                             BigDecimal amount, BigDecimal realAmount, CurrencyAdaptType token) {
        JsonObject userInfo = requestInitService.get().getUserInfo();
        Long uid = requestInitService.get().getUid();
        LocalDateTime now = LocalDateTime.now();
        Charge charge = Charge.builder().id(CommonFunction.generalId()).createTime(now)
                .completeTime(now).status(ChargeStatus.chain_success).uid(uid)
                .uidUsername(JsonObjectTool.getAsString(userInfo, "username"))
                .uidNick(JsonObjectTool.getAsString(userInfo, "nick"))
                .uidAvatar(JsonObjectTool.getAsString(userInfo, "avatar"))
                .sn(query.getSn())
                .currencyAdaptType(query.getType())
                .chargeType(ChargeType.recharge)
                .amount(amount)
                .fee(BigDecimal.ZERO)
                .realAmount(realAmount).fromAddress(query.getFromAddress())
                .toAddress(query.getToAddress()).txid(query.getTxId())
                .token(token)
                .build();
        long insert = chargeMapper.insert(charge);
        return insert > 0;
    }

    /**
     * 提现
     *
     * @deprecated 暂时没用到
     */
    @Deprecated(since = "")
    public void uploadChain2(Charge charge) {
        // todo transfer 代币到对应地址
        CurrencyAdaptType token = charge.getToken();
        CurrencyAdaptType currencyAdaptType = charge.getCurrencyAdaptType();
        String txid = null;
        //BSC链的交易
        if (Objects.equals(currencyAdaptType, CurrencyAdaptType.usdt_bep20) || CurrencyAdaptType.BF_bep20.equals(currencyAdaptType) || CurrencyAdaptType.usdc_bep20.equals(currencyAdaptType)) {
            Result result = null;
            switch (token) {
                case usdt_bep20:
                    result = usdtBscContract.transfer(charge.getToAddress(), charge.getRealAmount());
                    break;
                case usdc_bep20:
                    result = usdtBscContract.transferUsdc(charge.getToAddress(), charge.getRealAmount().toBigInteger());
                    break;
                default:
                    break;
            }
            if (Objects.isNull(result) || !Objects.equals(result.getCode(), "0")) {
                throw ErrorCodeEnum.UPLOAD_CHAIN_ERROR.generalException();
            }
            txid = result.getData().toString();
        }
        //波场链 提现
        else if (CurrencyAdaptType.usdt_trc20.equals(currencyAdaptType) || CurrencyAdaptType.usdc_trc20.equals(currencyAdaptType)) {
            try {
                switch (token) {
                    case usdt_trc20:
                        txid = tronTriggerContract.transferUsdt(charge.getToAddress(), charge.getRealAmount().toBigInteger());
                        break;
                    case usdc_trc20:
                        txid = tronTriggerContract.transferUsdc(charge.getToAddress(), charge.getRealAmount().toBigInteger());
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                ErrorCodeEnum.UPLOAD_CHAIN_ERROR.throwException();
            }
        }
        //以太坊链提现
        else if (CurrencyAdaptType.usdt_erc20.equals(currencyAdaptType) || CurrencyAdaptType.usdc_erc20.equals(currencyAdaptType)) {
            Result result = null;
            try {
                switch (token) {
                    case usdt_erc20:
                        result = usdtEthContract.transfer(charge.getToAddress(), charge.getRealAmount().toBigInteger());
                        break;
                    case usdc_erc20:
                        result = usdtEthContract.transferUsdc(charge.getToAddress(), charge.getRealAmount().toBigInteger());
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                throw ErrorCodeEnum.UPLOAD_CHAIN_ERROR.generalException();
            }
            if (Objects.isNull(result) || !Objects.equals(result.getCode(), "0")) {
                throw ErrorCodeEnum.UPLOAD_CHAIN_ERROR.generalException();
            }
            txid = result.getData().toString();
        } else {
            ErrorCodeEnum.throwException("不支持的提现类型");
        }

        // 更新txid
        boolean update = false;
        while (!update) {
            update = this.update(Wrappers.<Charge>lambdaUpdate()
                    .set(Charge::getTxid, txid)
                    .eq(Charge::getId, charge.getId())
            );
        }

    }


    /**
     * @deprecated 暂时没用到
     */
    @Transactional
    @Deprecated(since = "")
    public void uploadChain(CurrencyAdaptType currencyAdaptType, BigInteger realAmount, String sn, String fromAddress, String toAddress) {
        asyncService.asyncSuccessRequest(() -> {
            String walletAppKey = configService.get("wallet_app_key");
            String walletAppSecret = configService.get("wallet_app_secret");
            String walletUrl = configService.getOrDefault("wallet_url", "https://www.twallet.pro/api");
            String url = configService.get("url");
            MapTool paramMap = MapTool.Map().put("sn", sn).put("amount", realAmount.toString()).put("from_address",
                            fromAddress).put("to_address", toAddress).put("type", currencyAdaptType.toString())
                    .put("notify_url", url + "/charge/webhooks");
            String param = new Gson().toJson(paramMap);
            log.info("提现审核上链参数 ==> " + param);
            String stringResult = HttpHandler.execute(new HttpRequest().setMethod(HttpRequest.Method.POST).setUrl(walletUrl + "/order/loan/create")
                    .setRequestHeader(MapBuilder.Map().put("AppKey", walletAppKey)
                            .put("Sign", Crypto.hmacToString(DigestFactory.createSHA256(), walletAppSecret, param)).build())
                    .setJsonString(param)).getStringResult();
            log.info("提现审核上链结果 ==> " + stringResult);
            loggerHandle.log(MapTool.Map().put("loan_result", stringResult));
        });
    }


    /**
     * 获取分页数据
     */
    public List<ChargeVO> pageByChargeType(Long uid, String address, ChargeType chargeType, Page<Charge> page) {

        LambdaQueryWrapper<Charge> wrapper = new LambdaQueryWrapper<Charge>()
                .eq(Charge::getUid, uid)
                .eq(Charge::getFromAddress, address);

        if(Objects.nonNull(chargeType)){
            wrapper = wrapper.eq(Charge::getChargeType, chargeType);
        }
        Page<Charge> charges = this.page(page,wrapper);
        return charges.getRecords().stream().map(chargeConverter :: toVO).collect(Collectors.toList());
    }

    public List<Charge> selectPage(Long uid,
                                   ChargeStatus status,
                                   ChargeType type,
                                   String phone,
                                   String txid,
                                   String startTime,
                                   String endTime,
                                   Integer page,
                                   Integer size) {
        return chargeMapper.selectPage(uid, status, type, phone, txid, startTime, endTime, Math.max((page - 1) * size, 0), size);
    }

    public int selectCount(Long uid,
                           ChargeStatus status,
                           ChargeType type,
                           String phone,
                           String txid,
                           String startTime,
                           String endTime) {
        return chargeMapper.selectCount(uid, status, type, phone, txid, startTime, endTime);
    }

    public int selectNewCount(String ip,
                              String equipment,
                              Boolean grcResult,
                              Boolean otherSec,
                              Long uid,
                              ChargeStatus status,
                              ChargeType type,
                              String phone,
                              String txid,
                              String startTime,
                              String endTime) {
        return chargeMapper.selectNewCount(ip, equipment, grcResult, otherSec, uid, status, type, phone, txid, startTime, endTime);
    }

    /**
     * @deprecated 1
     */
    @Deprecated(since = "")
    public Map<String, BigDecimal> sumChargeAmount(ChargeType recharge, String phone, String txid, String startTime, String endTime) {
        return chargeMapper.selectSumChargeAmount(recharge, phone, txid, startTime, endTime);
    }

    public Charge getById(long id) {
        return chargeMapper.getById(id);
    }

    public double totalWithdrawAmount() {
        List<StatChargeAmount> statCollectAmounts = chargeMapper.totalWithdrawAmount();
        return statCollectAmounts.stream().map(e -> new DigitalCurrency(e.getCurrency_type(), e.getTotal_amount()).toOther(CurrencyAdaptType.usdt_omni).getMoney()).reduce(Double::sum).orElse(0.0);
    }

}
