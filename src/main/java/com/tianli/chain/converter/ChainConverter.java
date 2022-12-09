package com.tianli.chain.converter;

import com.tianli.chain.entity.*;
import com.tianli.chain.query.CoinReviewConfigIoUQuery;
import com.tianli.chain.vo.CoinVO;
import com.tianli.chain.vo.WalletImputationLogAppendixVO;
import com.tianli.chain.vo.WalletImputationLogVO;
import com.tianli.chain.vo.WalletImputationVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChainConverter {

    WalletImputationVO toWalletImputationVO(WalletImputation walletImputation);

    WalletImputationLogVO toWalletImputationLogVO(WalletImputationLog walletImputationLog);

    WalletImputationLogAppendixVO toWalletImputationLogAppendixVO(WalletImputationLogAppendix walletImputationAppendix);

    CoinVO toCoinVO(Coin coin);

    CoinReviewConfig toDO(CoinReviewConfigIoUQuery query);
}
