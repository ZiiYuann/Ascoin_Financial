package com.tianli.btc;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.gson.JsonObject;
import com.tianli.address.AddressService;
import com.tianli.address.controller.AddressWebhooksDTO;
import com.tianli.btc.mapper.Usdtin;
import com.tianli.btc.mapper.UsdtinMapper;
import com.tianli.charge.ChargeService;
import com.tianli.charge.controller.ChargeWebhooksDTO;
import com.tianli.charge.mapper.Charge;
import com.tianli.common.CommonFunction;
import com.tianli.common.Constants;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.dividends.settlement.ChargeSettlementService;
import com.tianli.dividends.settlement.mapper.ChargeSettlement;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.judge.JsonObjectTool;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @Author cs
 * @Date 2022-01-18 9:43 上午
 */
//@Service
public class BtcService {
    @Resource
    private RpcService rpcService;
    @Resource
    private ConfigService configService;
    @Resource
    private UsdtinMapper usdtinMapper;
    @Resource
    private AddressService addressService;
    @Resource
    private ChargeService chargeService;
    @Resource
    private ChargeSettlementService chargeSettlementService;

    public static final String BLOCK_COUNT = "block_count";

    @Transactional
    public void auto(Long height) {
        Long old_height = height - 1L;
        String blockhash = rpcService.getblockhash(height);
        List<String> txidList = rpcService.getblock(blockhash);
        String myAddress = configService.get("btc_address");
        for (String txid : txidList) {
            updateTx(txid, height, myAddress);
        }
        boolean cas = configService.cas(BLOCK_COUNT, old_height.toString(), height.toString());
        if(!cas) ErrorCodeEnum.SYSTEM_ERROR.throwException();
    }

    public void updateTx(String txid, long height, String myAddress) {
        RawTransaction rawTransaction = rpcService.getrawtransaction(txid);
        boolean usdt = false;
        LocalDateTime time = LocalDateTime.ofEpochSecond(rawTransaction.getTime(), 0, ZoneOffset.ofHours(8));
        for (RawTransaction.Vout vout : rawTransaction.getVout()) {
            if (vout.getScriptPubKey().getAsm().startsWith("OP_RETURN")) {
                usdt = true;
                break;
            }
        }

        if (usdt) {
            JsonObject jsonObject = rpcService.omni_gettransaction(txid);
            if (jsonObject != null) {
                String sendingaddress = JsonObjectTool.getAsString(jsonObject, "sendingaddress");
                String referenceaddress = JsonObjectTool.getAsString(jsonObject, "referenceaddress");
                String type = JsonObjectTool.getAsString(jsonObject, "type");
                String amount = JsonObjectTool.getAsString(jsonObject, "amount");
                String fee = JsonObjectTool.getAsString(jsonObject, "fee");
                Integer propertyid = JsonObjectTool.getAsInt(jsonObject, "propertyid");
                Boolean valid = JsonObjectTool.getAsBool(jsonObject, "valid");
                Long block = JsonObjectTool.getAsLong(jsonObject, "block");
                if (!StringUtils.isEmpty(sendingaddress) && !StringUtils.isEmpty(referenceaddress) && !StringUtils.isEmpty(type)
                        && !StringUtils.isEmpty(amount)
                        && propertyid != null && valid != null && block != null) {
                    if (propertyid == 31 && "Simple Send".equals(type)) {
                        boolean from = referenceaddress.equals(myAddress);
                        boolean to =  addressService.getByBtc(sendingaddress) != null;
                        if (from || to) {
                            usdtinMapper.insert(Usdtin.builder().txid(txid).amount(CommonFunction.parseSatoshis(amount)).fee(CommonFunction.parseSatoshis(fee))
                                    .referenceaddress(referenceaddress).sendingaddress(sendingaddress).block(block).create_time(time).valid(valid).build());
                            if(to) {
                                CompletableFuture.runAsync(() -> {
                                    AddressWebhooksDTO dto = new AddressWebhooksDTO();
                                    dto.setTxid(txid);
                                    dto.setCreate_time(time);
                                    dto.setBlock("" + height);
                                    dto.setFrom_address(sendingaddress);
                                    dto.setTo_address(referenceaddress);
                                    dto.setType(TokenCurrencyType.usdt_omni);
                                    dto.setSn("C" + CommonFunction.generalSn(CommonFunction.generalId()));
                                    dto.setValue(BigInteger.valueOf(CommonFunction.parseSatoshis(amount)));
                                    chargeService.receive(dto);
                                }, Constants.COMPLETABLE_FUTURE_EXECUTOR);
                            } else {
                                CompletableFuture.runAsync(() -> {
                                    Charge c = chargeService.getOne(Wrappers.<Charge>lambdaQuery().eq(Charge::getTxid, txid));
                                    if(c != null) {
                                        chargeService.withdrawWebhooks(ChargeWebhooksDTO.builder()
                                                .status("success").txid(txid)
                                                .miner_fee(BigInteger.valueOf(CommonFunction.parseSatoshis(fee))).sn(c.getSn())
                                                .miner_fee_type(TokenCurrencyType.btc).build());
                                    } else {
                                        ChargeSettlement cs = chargeSettlementService.getOne(Wrappers.<ChargeSettlement>lambdaQuery().eq(ChargeSettlement::getTxid, txid));
                                        if (cs != null) {
                                            chargeService.withdrawWebhooks(ChargeWebhooksDTO.builder()
                                                    .status("success").txid(txid)
                                                    .miner_fee(BigInteger.valueOf(CommonFunction.parseSatoshis(fee))).sn(cs.getSn())
                                                    .miner_fee_type(TokenCurrencyType.btc).build());
                                        }
                                    }
                                }, Constants.COMPLETABLE_FUTURE_EXECUTOR);
                            }
                        }
                    }
                }
            }
        }
    }
}
