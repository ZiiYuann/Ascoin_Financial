package com.tianli.charge;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tianli.address.AddressService;
import com.tianli.chain.service.LineTransactionDetail;
import com.tianli.charge.controller.ChargeWebhooksDTO;
import com.tianli.charge.controller.WithdrawDTO;
import com.tianli.charge.dto.StatChargeAmount;
import com.tianli.charge.mapper.Charge;
import com.tianli.charge.mapper.ChargeMapper;
import com.tianli.charge.mapper.ChargeStatus;
import com.tianli.common.CommonFunction;
import com.tianli.common.ConfigConstants;
import com.tianli.common.Constants;
import com.tianli.common.HttpUtils;
import com.tianli.common.async.AsyncService;
import com.tianli.common.blockchain.TronTriggerContract;
import com.tianli.common.blockchain.UsdtBscContract;
import com.tianli.common.blockchain.UsdtEthContract;
import com.tianli.sso.RequestInitService;
import com.tianli.common.log.LoggerHandle;
import com.tianli.currency.*;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.deposit.ChargeDepositService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.MapBuilder;
import com.tianli.tool.MapTool;
import com.tianli.tool.crypto.Crypto;
import com.tianli.tool.http.HttpHandler;
import com.tianli.tool.http.HttpRequest;
import com.tianli.tool.judge.JsonObjectTool;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.crypto.util.DigestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author wangqiyun
 * @Date 2020/3/31 11:25
 */

@Service
public class ChargeService extends ServiceImpl<ChargeMapper, Charge> {

    public static final BigInteger ONE_HUNDRED = new BigInteger("100");
    public static final BigInteger TEN_BILLION = new BigInteger("10000000000");

    @Transactional
    public void withdrawWebhooks(ChargeWebhooksDTO chargeWebhooksDTO) {
        String sn = chargeWebhooksDTO.getSn();
        if(StringUtils.isBlank(sn)){
            return;
        }
        CurrencyTypeEnum type;
        if(sn.startsWith("CD")){
            type = CurrencyTypeEnum.deposit;
        } else if(sn.startsWith("CS")){
            type = CurrencyTypeEnum.settlement;
        } else if(sn.startsWith("C")){
            type = CurrencyTypeEnum.normal;
        } else {
            return;
        }
        boolean update;
        TokenCurrencyType currencyType;
        Long uid;
        //链上交易成功
        if ("success".equals(chargeWebhooksDTO.getStatus())) {
            BigInteger real_amount;
            BigInteger fee;
            CurrencyLogDes logDes;
            switch (type) {
                //用户的提现
                case normal: {
                    Charge charge = chargeMapper.getBySn(sn);
                    if (charge == null) return;
                    BigInteger minerFee = chargeWebhooksDTO.getMiner_fee();
                    TokenCurrencyType minerFeeType = chargeWebhooksDTO.getMiner_fee_type();
                    currencyType = charge.getCurrency_type();
                    real_amount = charge.getReal_amount();
                    fee = charge.getFee();
                    logDes = CurrencyLogDes.提现;
                    uid = charge.getUid();
                    //修改提现表的状态
                    update = chargeMapper.success(LocalDateTime.now(), ChargeStatus.chain_success, charge.getId(), ChargeStatus.chaining, minerFee, minerFeeType) > 0;
                    break;
                }
                default:
                    return;
            }
            if (update) {
                //成功扣款逻辑
                if (Objects.equals(currencyType, TokenCurrencyType.usdt_bep20)
                        || Objects.equals(currencyType, TokenCurrencyType.usdc_bep20)
                ) {
                    CurrencyTokenEnum token = CurrencyTokenEnum.valueOf(currencyType.name());
                    currencyService.reduce(uid, type, token, real_amount.divide(TEN_BILLION), sn, logDes.name());
                    currencyService.reduce(uid, type, token, fee.divide(TEN_BILLION), sn, CurrencyLogDes.提现手续费.name());
                } else if (Objects.equals(currencyType, TokenCurrencyType.BF_bep20)) {
                    currencyService.reduce(uid, type, real_amount, sn, logDes.name());
                    currencyService.reduce(uid, type, fee, sn, CurrencyLogDes.提现手续费.name());
                }else if (Objects.equals(currencyType, TokenCurrencyType.usdt_trc20)
                        || Objects.equals(currencyType, TokenCurrencyType.usdt_erc20)
                        || Objects.equals(currencyType, TokenCurrencyType.usdc_trc20)
                        || Objects.equals(currencyType, TokenCurrencyType.usdc_erc20)
                ) {
                    CurrencyTokenEnum token = CurrencyTokenEnum.valueOf(currencyType.name());
                    currencyService.reduce(uid, type, token, real_amount.multiply(ONE_HUNDRED), sn, logDes.name());
                    currencyService.reduce(uid, type, token, fee.multiply(ONE_HUNDRED), sn, CurrencyLogDes.提现手续费.name());
                }
            }
        }
        //链上交易失败
        else if ("fail".equals(chargeWebhooksDTO.getStatus())) {
            BigInteger amount;
            CurrencyLogDes logDes;
            switch (type) {
                //修改订单状态
                case normal:
                    Charge charge = chargeMapper.getBySn(sn);
                    if (charge == null) return;
                    currencyType = charge.getCurrency_type();
                    amount = charge.getAmount();
                    logDes = CurrencyLogDes.提现;
                    uid = charge.getUid();
                    update = chargeMapper.fail(LocalDateTime.now(), ChargeStatus.chain_fail, charge.getId(), ChargeStatus.chaining, chargeWebhooksDTO.getMiner_fee(), chargeWebhooksDTO.getMiner_fee_type()) > 0;
                    break;
                default:
                    return;
            }
            //返还冻结余额
            if (update) {
                if (Objects.equals(currencyType, TokenCurrencyType.usdt_bep20)
                        || Objects.equals(currencyType, TokenCurrencyType.usdc_bep20)
                ) {
                    CurrencyTokenEnum token = CurrencyTokenEnum.valueOf(currencyType.name());
                    currencyService.unfreeze(uid, type, token, amount.divide(TEN_BILLION), sn, logDes.name());
                } else if (Objects.equals(currencyType, TokenCurrencyType.usdt_trc20)
                        || Objects.equals(currencyType, TokenCurrencyType.usdt_erc20)
                        || Objects.equals(currencyType, TokenCurrencyType.usdc_trc20)
                        || Objects.equals(currencyType, TokenCurrencyType.usdc_erc20)
                ) {
                    CurrencyTokenEnum token = CurrencyTokenEnum.valueOf(currencyType.name());
                    currencyService.unfreeze(uid, type, token, amount.multiply(ONE_HUNDRED), sn, logDes.name());
                } else if(Objects.equals(currencyType, TokenCurrencyType.BF_bep20)){
                    currencyService.unfreeze(uid, type, amount, sn, logDes.name());
                }
            }
        }
    }

    private LineTransactionDetail getTransactionDetail(String txid) {
        LineTransactionDetail detail;
        Map<String, String> params = Maps.newHashMap();
        params.put("txid", txid);
        try {
            HttpResponse httpResponse = HttpUtils.doGet(Constants.TRANSACTION_DETAILS_HOST, Constants.TRANSACTION_DETAILS_PATH, "GET", Maps.newHashMap(), params);
            String dataJsonString = EntityUtils.toString(httpResponse.getEntity());
            detail = new Gson().fromJson(dataJsonString, LineTransactionDetail.class);
        } catch (Exception e) {
            return null;
        }
        return detail;
    }


    @Transactional
    public void withdraw(WithdrawDTO withdrawDTO) {
        Long uid = requestInitService.uid();
        String from_address = null;
        TokenCurrencyType tokenCurrencyType = withdrawDTO.getTokenCurrencyType();
        //根据提现币种 选择对应的链的主钱包地址
        switch (tokenCurrencyType) {
            case usdt_trc20:
            case usdc_trc20:
                from_address = configService.get(ConfigConstants.TRON_MAIN_WALLET_ADDRESS);
                break;
            case usdt_erc20:
            case usdc_erc20:
                from_address = configService.get(ConfigConstants.ETH_MAIN_WALLET_ADDRESS);
                break;
            case BF_bep20:
            case usdt_bep20:
            case bsc:
            case usdc_bep20:
                from_address = configService.get(ConfigConstants.BSC_MAIN_WALLET_ADDRESS);
                break;
        }
        if (from_address == null) ErrorCodeEnum.SYSTEM_ERROR.throwException();
        long id = CommonFunction.generalId();
        CurrencyTokenEnum token = withdrawDTO.getToken();
//        if (Objects.equals(tokenCurrencyType, TokenCurrencyType.BF_bep20)) {
//            token = CurrencyTokenEnum.BF_bep20;
//        } else {
//            token = CurrencyTokenEnum.usdt_omni;
//        }
        if(!Objects.equals(tokenCurrencyType, TokenCurrencyType.bsc) && !Objects.equals(tokenCurrencyType, TokenCurrencyType.BF_bep20) && Objects.equals(token, CurrencyTokenEnum.BF_bep20)){
            ErrorCodeEnum.throwException("BF代币提现链错误");
        }
        // 最小提现金额
        String withdraw_min_amount = configService._get(tokenCurrencyType.name() + "_withdraw_min_amount");
        BigInteger amount = tokenCurrencyType.amount(withdrawDTO.getAmount());
        if (amount.compareTo(new BigInteger(withdraw_min_amount)) < 0){
            ErrorCodeEnum.throwException("提现usdt数额过小");
        }

        //计算手续费  实际手续费 = 提现数额*手续费率 + 固定手续费数额
        String rate = configService._get(tokenCurrencyType.name() + "_withdraw_rate");
        String fixedAmount = configService._get(tokenCurrencyType.name() + "_withdraw_fixed_amount");
        BigInteger fee = (new BigDecimal(amount).multiply(new BigDecimal(StringUtils.isNotBlank(rate) ? rate : "0"))).toBigInteger()
                .add(new BigInteger(StringUtils.isNotBlank(fixedAmount) ? fixedAmount : "0"));
        //实际到账数额 = 提现数额 - 手续费
        BigInteger real_amount = amount.subtract(fee);
        if (fee.compareTo(BigInteger.ZERO) < 0)
            ErrorCodeEnum.FEE_LT_ZERO_ERROR.throwException();
        if (real_amount.compareTo(BigInteger.ZERO) < 0)
            ErrorCodeEnum.WITHDRAWAL_AMOUNT_LT_FEE_ERROR.throwException();
        //
        JsonObject userInfo = requestInitService.get().getUserInfo();

        //创建提现订单
        Charge charge = Charge.builder()
                .id(id).create_time(LocalDateTime.now())
                .status(ChargeStatus.created)
                .uid(uid)
                .uid_avatar(JsonObjectTool.getAsString(userInfo, "avatar"))
                .uid_nick(JsonObjectTool.getAsString(userInfo, "nick"))
                .uid_username(JsonObjectTool.getAsString(userInfo, "username"))
                .sn("C" + CommonFunction.generalSn(id))
                .currency_type(tokenCurrencyType)
                .charge_type(ChargeType.withdraw)
                .amount(amount)
                .fee(fee)
                .real_amount(real_amount)
                .from_address(from_address).to_address(withdrawDTO.getAddress())
                .token(token)
                .build();
        if (chargeMapper.insert(charge) <= 0) ErrorCodeEnum.SYSTEM_ERROR.throwException();
        //转换u后面的0的个数
        if (Objects.equals(tokenCurrencyType, TokenCurrencyType.usdt_erc20)
                || Objects.equals(tokenCurrencyType, TokenCurrencyType.usdt_trc20)
                || Objects.equals(tokenCurrencyType, TokenCurrencyType.usdc_erc20)
                || Objects.equals(tokenCurrencyType, TokenCurrencyType.usdc_trc20)) {
            amount = amount.multiply(ONE_HUNDRED);
        } else if(Objects.equals(tokenCurrencyType, TokenCurrencyType.usdt_bep20)
                || Objects.equals(tokenCurrencyType, TokenCurrencyType.usdc_bep20)
        ) {
            amount = amount.divide(TEN_BILLION);
        }
        //冻结提现数额
        currencyService.freeze(uid, CurrencyTypeEnum.normal, token, amount, charge.getSn(), CurrencyLogDes.提现.name());
    }

    @Resource
    private ChargeService chargeService;

    @Resource
    private UsdtBscContract usdtBscContract;

    @Resource
    private UsdtEthContract usdtEthContract;

    @Transactional
    public void uploadChain(TokenCurrencyType currency_type, BigInteger real_amount, String sn, String from_address, String to_address) {
        asyncService.asyncSuccessRequest(() -> {
            String wallet_app_key = configService.get("wallet_app_key");
            String wallet_app_secret = configService.get("wallet_app_secret");
            String wallet_url = configService.getOrDefault("wallet_url", "https://www.twallet.pro/api");
            String url = configService.get("url");
            MapTool paramMap = MapTool.Map().put("sn", sn).put("amount", real_amount.toString()).put("from_address",
                    from_address).put("to_address", to_address).put("type", currency_type.toString())
                    .put("notify_url", url + "/charge/webhooks");
            String param = new Gson().toJson(paramMap);
            System.out.println("提现审核上链参数 ==> " + param);
            String stringResult = HttpHandler.execute(new HttpRequest().setMethod(HttpRequest.Method.POST).setUrl(wallet_url + "/order/loan/create")
                    .setRequestHeader(MapBuilder.Map().put("AppKey", wallet_app_key)
                            .put("Sign", Crypto.hmacToString(DigestFactory.createSHA256(), wallet_app_secret, param)).build())
                    .setJsonString(param)).getStringResult();
            System.out.println("提现审核上链结果 ==> " + stringResult);
            loggerHandle.log(MapTool.Map().put("loan_result", stringResult));
        });
    }

    public List<Charge> withdrawApplyPage(Long uid, ChargeStatus status, Integer page, Integer size) {
        return selectPage(uid, status, ChargeType.withdraw, null, null, null, null, page, size);
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
                              Boolean grc_result,
                              Boolean otherSec,
                              Long uid,
                              ChargeStatus status,
                              ChargeType type,
                              String phone,
                              String txid,
                              String startTime,
                              String endTime) {
        return chargeMapper.selectNewCount(ip, equipment, grc_result, otherSec, uid, status, type, phone, txid, startTime, endTime);
    }

    public Map<String, BigDecimal> sumChargeAmount(ChargeType recharge, String phone, String txid, String startTime, String endTime) {
        return chargeMapper.selectSumChargeAmount(recharge, phone, txid, startTime, endTime);
    }

//    public BigInteger sumChargeBFAmount(ChargeType recharge, String phone, String txid, String startTime, String endTime) {
//        return chargeMapper.selectSumChargeAmount(recharge, phone, txid, startTime, endTime, CurrencyTokenEnum.BF_bep20);
//    }

    public Charge getById(long id) {
        return chargeMapper.getById(id);
    }

    public double totalWithdrawAmount() {
        List<StatChargeAmount> statCollectAmounts = chargeMapper.totalWithdrawAmount();
        return statCollectAmounts.stream().map(e -> new DigitalCurrency(e.getCurrency_type(), e.getTotal_amount()).toOther(TokenCurrencyType.usdt_omni).getMoney()).reduce(Double::sum).orElse(0.0);
    }

    @Resource
    private LoggerHandle loggerHandle;
    @Resource
    private AsyncService asyncService;
    @Resource
    private ConfigService configService;
    @Resource
    private RequestInitService requestInitService;
    @Resource
    private CurrencyService currencyService;
    @Resource
    private ChargeMapper chargeMapper;
    @Resource
    private TronTriggerContract tronTriggerContract;
    public List<Map<String,Object>> totalAmount(String ip, String equipment, Boolean grc_result, Boolean otherSec, Long uid, ChargeStatus status, ChargeType withdraw, String phone, String txid, String startTime, String endTime) {
        return chargeMapper.totalAmount(ip,equipment,grc_result,otherSec,uid,status,withdraw,phone,txid,startTime,endTime);
    }
}
