package com.tianli.chain.enums;

import com.tianli.currency.enums.TokenAdapter;
import lombok.Getter;

/**
 * 链类型
 */
public enum ChainType {
    BSC(TokenAdapter.bnb),
    ETH(TokenAdapter.eth),
    TRON(null);

    @Getter
    private TokenAdapter tokenAdapter;

    ChainType(TokenAdapter tokenAdapter){
        this.tokenAdapter = tokenAdapter;
    }
}
