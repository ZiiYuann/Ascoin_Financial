package com.tianli.management.converter;

import com.tianli.management.vo.WalletAgentVO;
import com.tianli.management.bo.WalletAgentBO;
import com.tianli.management.entity.WalletAgent;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WalletAgentConverter {

    WalletAgent toDO(WalletAgentBO bo);

    WalletAgentVO toVO(WalletAgent walletAgent);

}
