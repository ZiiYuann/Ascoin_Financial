package com.tianli.chain.enums;

import com.tianli.currency.enums.TokenAdapter;
import com.tianli.exception.ErrorCodeEnum;
import lombok.Getter;

/**
 * 链类型
 */
@Getter
public enum ChainType {
//    BTC("btc"),
    ETH("eth", "ETH", 1),
    BSC("bnb", "BSC", 2),
    TRON("trx", "TRON", 3),
    POLYGON("matic", "Polygon", 4),
    OPTIMISTIC("eth", "Optimistic", 5),
    ARBITRUM("eth", "Arbitrum", 6);

    ChainType(String mainToken, String display, int sequence) {
        this.mainToken = mainToken;
        this.display = display;
        this.sequence = sequence;
    }

    private final String mainToken;
    private final String display;
    private final int sequence;

    public static TokenAdapter getTokenAdapter(ChainType chainType) {
        if (ChainType.BSC.equals(chainType)) return TokenAdapter.bnb;
        if (ChainType.ETH.equals(chainType)) return TokenAdapter.eth;
        throw ErrorCodeEnum.NOT_OPEN.generalException();
    }


}
