package com.tianli.chain.service.contract;

import com.tianli.address.Service.ChargeAddressMnemonicService;
import com.tianli.chain.entity.Coin;
import com.tianli.chain.enums.ChainType;
import com.tianli.currency.enums.TokenAdapter;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import org.bitcoinj.core.Base58;
import org.springframework.stereotype.Component;
import party.loveit.bip44forjava.core.ECKey;
import party.loveit.bip44forjava.utils.Bip44Utils;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;

/**
 * @Author cs
 * @Date 2022-12-26 10:19
 */
@Component
public class BtcOperation extends AbstractContractOperation implements ComputeAddress{
    @Resource
    private ChargeAddressMnemonicService chargeAddressMnemonicService;

    @Override
    Result tokenTransfer(String to, BigInteger val, Coin coin) {
        throw ErrorCodeEnum.NOT_OPEN.generalException();
    }

    @Override
    public boolean match(ChainType chain) {
        return ChainType.BTC.equals(chain);
    }

    @Override
    public String computeAddress(long uid) throws IOException {
        String mnemonic = chargeAddressMnemonicService.getMnemonic(uid);
        BigInteger prvBtc = Bip44Utils.getDefaultPathPrivateKey(Collections.singletonList(mnemonic), 0);
        ECKey ecKey = ECKey.fromPrivate(prvBtc);
        byte[] pubKeyHash = ecKey.getPubKeyHash();
        return Base58.encodeChecked(0, pubKeyHash);
    }

    @Override
    Result mainTokenTransfer(String to, BigInteger val, Coin coin) {
        return null;
    }

    @Override
    public boolean successByHash(String hash) {
        return false;
    }

    @Override
    public BigDecimal mainBalance(String address) {
        return null;
    }

    @Override
    public BigDecimal tokenBalance(String address, TokenAdapter tokenAdapter) {
        throw ErrorCodeEnum.NOT_OPEN.generalException();
    }

    @Override
    public Integer decimals(String contractAddress) {
        throw ErrorCodeEnum.NOT_OPEN.generalException();
    }
}
