package com.tianli.chain.converter;

import com.tianli.chain.entity.WalletImputation;
import com.tianli.chain.entity.WalletImputationLog;
import com.tianli.chain.entity.WalletImputationLogAppendix;
import com.tianli.chain.entity.WalletImputationTemporary;
import com.tianli.chain.vo.WalletImputationLogAppendixVO;
import com.tianli.chain.vo.WalletImputationLogVO;
import com.tianli.chain.vo.WalletImputationVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChainConverter {

    WalletImputationVO toWalletImputationVO(WalletImputation walletImputation);

    WalletImputationLogVO toWalletImputationLogVO(WalletImputationLog walletImputationLog);

    WalletImputationLogAppendixVO toWalletImputationLogAppendixVO(WalletImputationLogAppendix walletImputationAppendix);

    WalletImputationTemporary toTemporary(WalletImputation walletImputation);
}
