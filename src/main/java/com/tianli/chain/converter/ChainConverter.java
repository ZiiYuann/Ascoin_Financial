package com.tianli.chain.converter;

import com.tianli.chain.entity.WalletImputation;
import com.tianli.chain.entity.WalletImputationTemporary;
import com.tianli.chain.vo.WalletImputationVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChainConverter {

    WalletImputationVO toWalletImputationVO(WalletImputation walletImputation);

    WalletImputationTemporary toTemporary(WalletImputation walletImputation);
}
