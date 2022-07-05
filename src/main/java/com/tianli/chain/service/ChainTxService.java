package com.tianli.chain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.tianli.address.AddressService;
import com.tianli.address.mapper.Address;
import com.tianli.chain.mapper.ChainTx;
import com.tianli.chain.mapper.ChainTxMapper;
import com.tianli.charge.controller.ChargeWebhooksDTO;
import com.tianli.charge.mapper.ChargeStatus;
import com.tianli.common.CommonFunction;
import com.tianli.common.Constants;
import com.tianli.common.HttpUtils;
import com.tianli.common.async.AsyncService;
import com.tianli.common.lock.RedisLock;
import com.tianli.common.log.LoggerHandle;
import com.tianli.currency.DigitalCurrency;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.MapBuilder;
import com.tianli.tool.MapTool;
import com.tianli.tool.crypto.Crypto;
import com.tianli.tool.http.HttpHandler;
import com.tianli.tool.http.HttpRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.crypto.util.DigestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author wangqiyun
 * @since 2020/11/16 21:02
 */

@Service
public class ChainTxService extends ServiceImpl<ChainTxMapper, ChainTx> {
    //归集
    @Transactional
    public void collect(TokenCurrencyType currencyType, BigInteger amount, String collect_address, long uid, BigInteger other_amount) {
        if (amount.compareTo(BigInteger.ZERO) <= 0) return;
        redisLock.lock("ChainTxService_collect_" + collect_address, 2L, TimeUnit.MINUTES);
        Integer exist = chainTxMapper.selectCount(new LambdaQueryWrapper<ChainTx>().eq(ChainTx::getStatus, ChargeStatus.chaining)
                .eq(ChainTx::getCollect_address, collect_address));
        if (exist != null && exist > 0) return;
        String main_address = null;
        switch (currencyType) {
            case usdt_omni:
            case btc:
                main_address = configService.get("btc_address");
                break;
            case eth:
            case usdt_erc20:
                main_address = configService.get("eth_address");
                break;
            case tron:
            case usdt_trc20:
                main_address = configService.get("tron_address");
                break;
        }
        if (main_address == null) ErrorCodeEnum.SYSTEM_ERROR.throwException();
        long id = CommonFunction.generalId();

        ChainTx chainTx = ChainTx.builder().id(id).create_time(LocalDateTime.now())
                .status(ChargeStatus.chaining).uid(uid).sn("" + CommonFunction.generalSn(id))
                .currency_type(currencyType).amount(amount).other_amount(other_amount).main_address(main_address)
                .collect_address(collect_address).build();
        long insert = chainTxMapper.insert(chainTx);
        if (insert > 0) {
            asyncService.asyncSuccessRequest(() -> {
                String wallet_app_key = configService.get("wallet_app_key");
                String wallet_app_secret = configService.get("wallet_app_secret");
                String wallet_url = configService.getOrDefault("wallet_url", "https://www.twallet.pro/api");
                String url = configService.get("url");
                MapTool paramMap;
                switch (currencyType) {
                    case usdt_omni:
                    case btc:
                        paramMap = MapTool.Map().put("sn", chainTx.getSn()).put("amount", chainTx.getAmount().toString()).put("from_address",
                                chainTx.getCollect_address()).put("to_address", chainTx.getMain_address())
                                .put("fee_address", chainTx.getMain_address()).put("type", chainTx.getCurrency_type().toString())
                                .put("notify_url", url + "/chain/tx/webhooks").put("collect", "true");
                        break;
                    case eth:
                    case usdt_erc20:
                        paramMap = MapTool.Map().put("sn", chainTx.getSn()).put("amount", chainTx.getAmount().toString()).put("from_address",
                                chainTx.getMain_address()).put("to_address", chainTx.getCollect_address())
                                .put("type", chainTx.getCurrency_type().toString())
                                .put("notify_url", url + "/chain/tx/webhooks").put("collect", "true");
                        break;
                    case tron:
                    case usdt_trc20:
                        paramMap = MapTool.Map().put("sn", chainTx.getSn()).put("amount", chainTx.getAmount().toString())
                                .put("from_address", chainTx.getMain_address()).put("to_address", chainTx.getCollect_address())
                                .put("type", chainTx.getCurrency_type().toString())
                                .put("notify_url", url + "/chain/tx/webhooks").put("collect", "true");
                        break;
                    default:
                        throw new RuntimeException("不存在的类型");
                }
                String param = new Gson().toJson(paramMap);
                System.out.println("归集上链参数 ==> " + param);
                String stringResult = HttpHandler.execute(new HttpRequest().setMethod(HttpRequest.Method.POST).setUrl(wallet_url + "/order/loan/create")
                        .setRequestHeader(MapBuilder.Map().put("AppKey", wallet_app_key)
                                .put("Sign", Crypto.hmacToString(DigestFactory.createSHA256(), wallet_app_secret, param)).build())
                        .setJsonString(param)).getStringResult();
                System.out.println("归集上链结果 ==> " + stringResult);
                loggerHandle.log(MapTool.Map().put("collect_result", stringResult));
            });
        }
    }

    @Transactional
    public void webhooks(ChargeWebhooksDTO chargeWebhooksDTO) {

        ChainTx chainTx = chainTxMapper.selectOne(new LambdaQueryWrapper<ChainTx>().eq(ChainTx::getSn, chargeWebhooksDTO.getSn()));
        if (chainTx == null) return;
        if ("success".equals(chargeWebhooksDTO.getStatus())) {
            TokenCurrencyType currency_type = chainTx.getCurrency_type();
            LineTransactionDetail transactionDetail = getTransactionDetail(chargeWebhooksDTO.getTxid());
            LineTransactionDetail.Data data;
            BigInteger fee = null;
            TokenCurrencyType fee_type = null;
            if(Objects.nonNull(transactionDetail) && Objects.nonNull(data = transactionDetail.getData())){
                if(Objects.equals(TokenCurrencyType.usdt_omni, currency_type) && Objects.nonNull(data.getBtc())){
                    // BTC
                    fee_type = TokenCurrencyType.btc;
                    fee = data.getBtc().getFee();
                }else if(Objects.equals(TokenCurrencyType.usdt_erc20, currency_type) && Objects.nonNull(data.getEth())) {
                    // ETH
                    fee_type = TokenCurrencyType.eth;
                    fee = data.getEth().getFee();
                }else if(Objects.equals(TokenCurrencyType.usdt_trc20, currency_type) && Objects.nonNull(data.getTrx())) {
                    // TRX
                    fee_type = TokenCurrencyType.tron;
                    fee = data.getTrx().getFee();
                    if(fee == null){
                        fee = data.getEth().getFee();
                    }
                }
            }
            int update = chainTxMapper.update(null, new LambdaUpdateWrapper<ChainTx>().set(ChainTx::getComplete_time, LocalDateTime.now())
                    .set(ChainTx::getStatus, ChargeStatus.chain_success).set(ChainTx::getTxid, chargeWebhooksDTO.getTxid())
                    .set(Objects.nonNull(fee), ChainTx::getFee, fee)
                    .set(Objects.nonNull(fee_type), ChainTx::getFee_currency_type, fee_type)
                    .eq(ChainTx::getSn, chargeWebhooksDTO.getSn()).eq(ChainTx::getStatus, ChargeStatus.chaining));
            if (update > 0) {
                Address address = addressService.getById(chainTx.getUid());
                asyncService.asyncSuccessRequest(() -> chainService.update(chainTx.getUid(), address, currency_type, true));
            }
        } else if ("fail".equals(chargeWebhooksDTO.getStatus())) {
            chainTxMapper.update(null, new LambdaUpdateWrapper<ChainTx>().set(ChainTx::getComplete_time, LocalDateTime.now())
                    .set(ChainTx::getStatus, ChargeStatus.chain_fail)
                    .eq(ChainTx::getSn, chargeWebhooksDTO.getSn()).eq(ChainTx::getStatus, ChargeStatus.chaining));
        }
    }

    /**
     * 获取链上交易详情
     */
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

    public Page<ChainTx> page(int page, int size, String address, String txid, String startTime, String endTime, ChargeStatus status) {
        LambdaQueryWrapper<ChainTx> lambdaQueryWrapper = new LambdaQueryWrapper<ChainTx>()
                .like(StringUtils.isNotBlank(address), ChainTx::getCollect_address, address)
                .like(StringUtils.isNotBlank(txid), ChainTx::getTxid, txid)
                .ge(StringUtils.isNotBlank(startTime), ChainTx::getComplete_time, startTime)
                .le(StringUtils.isNotBlank(endTime), ChainTx::getComplete_time, endTime)
                .eq(Objects.nonNull(status), ChainTx::getStatus, status)
                .orderByDesc(ChainTx::getId)
                ;
        return chainTxMapper.selectPage(new Page<>(page, size), lambdaQueryWrapper);
    }

    @Resource
    private ChainService chainService;
    @Resource
    private LoggerHandle loggerHandle;
    @Resource
    private AsyncService asyncService;
    @Resource
    private ConfigService configService;
    @Resource
    private RedisLock redisLock;
    @Resource
    private AddressService addressService;
    @Resource
    private ChainTxMapper chainTxMapper;

    public double totalAmount() {
        List<StatCollectAmount> statCollectAmounts = chainTxMapper.totalAmount();
        return statCollectAmounts.stream().map(e -> new DigitalCurrency(e.getCurrency_type(), e.getTotal_amount()).toOther(TokenCurrencyType.usdt_omni).getMoney()).reduce(Double::sum).orElse(0.0);
    }
}
