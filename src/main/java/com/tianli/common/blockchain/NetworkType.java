package com.tianli.common.blockchain;

import com.tianli.chain.enums.ChainType;
import com.tianli.exception.ErrorCodeEnum;
import lombok.Getter;

public enum NetworkType {
    // erc20 以太坊公链代币标准 trc20 波场公链代币标准 bep20 币安链代币标准
    // 以太坊公链代币标准
    erc20(ChainType.ETH, "Ethereum", "ERC20"),
    // 波场公链代币标准
    trc20(ChainType.TRON, "TRON", "TRC20"),
    // 币安链代币标准
    bep20(ChainType.BSC, "BNB Chain", "BEP20"),

    erc20_polygon(ChainType.POLYGON, "Polygon", null),
    erc20_optimistic(ChainType.OPTIMISTIC, "Optimism", null),
    erc20_arbitrum(ChainType.ARBITRUM, "Arbitrum", null);
//    btc(ChainType.BTC);

    NetworkType(ChainType chainType, String networkDesc, String shortName) {
        this.chainType = chainType;
        this.desc = networkDesc;
        this.shortName = shortName;
    }

    @Getter
    private final ChainType chainType;
    @Getter
    private final String desc;
    @Getter
    private final String shortName;

    public static NetworkType getInstance(ChainType chainType) {
        for (NetworkType networkType : NetworkType.values()) {
            if (networkType.chainType.equals(chainType)) {
                return networkType;
            }
        }
        throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
    }
}
