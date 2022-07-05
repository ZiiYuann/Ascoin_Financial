package com.tianli.currency_token.transfer.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.currency_token.mapper.ChainType;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.currency_token.transfer.mapper.TokenContract;
import com.tianli.currency_token.transfer.mapper.TokenContractMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TokenContractService extends ServiceImpl<TokenContractMapper, TokenContract> {

    public List<TokenContract> findByChainType(ChainType chainType) {
        return this.list(Wrappers.lambdaQuery(TokenContract.class)
                .eq(TokenContract::getChain, chainType)
                .isNotNull(TokenContract::getContract_address));
    }

    public List<TokenContract> findByToken(CurrencyCoinEnum currencyCoinEnum) {
        return this.list(Wrappers.lambdaQuery(TokenContract.class).eq(TokenContract::getToken, currencyCoinEnum));
    }
}
