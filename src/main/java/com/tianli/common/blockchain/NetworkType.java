package com.tianli.common.blockchain;

import com.tianli.chain.enums.ChainType;
import com.tianli.exception.ErrorCodeEnum;
import lombok.Getter;

import java.util.List;

public enum NetworkType {
    // erc20 以太坊公链代币标准 trc20 波场公链代币标准 bep20 币安链代币标准
    // 以太坊公链代币标准
    erc20(ChainType.ETH),
    // 波场公链代币标准
    trc20(ChainType.TRON),
    // 币安链代币标准
    bep20(ChainType.BSC),

    erc20_polygon(ChainType.POLYGON),
    erc20_optimistic(ChainType.OPTIMISTIC),
    erc20_arbitrum(ChainType.ARBITRUM);
//    btc(ChainType.BTC);

    NetworkType(ChainType chainType){
        this.chainType = chainType;
    }

    @Getter
    private final ChainType chainType;

     public static NetworkType getInstance(ChainType chainType) {
        for (NetworkType networkType : NetworkType.values()){
            if(networkType.chainType.equals(chainType)){
                return networkType;
            }
        }
        throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
    }
}
