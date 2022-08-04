package com.tianli.chain.enums;

import com.tianli.currency.enums.TokenAdapter;
import com.tianli.exception.ErrorCodeEnum;
import lombok.Getter;

/**
 * 链类型
 */
@Getter
public enum ChainType {
    BSC,
    ETH,
    TRON;


    public static TokenAdapter getTokenAdapter(ChainType chainType){
        if(ChainType.BSC.equals(chainType)) return TokenAdapter.bnb;
        if(ChainType.ETH.equals(chainType)) return TokenAdapter.eth;
        throw ErrorCodeEnum.NOT_OPEN.generalException();
    }



}
