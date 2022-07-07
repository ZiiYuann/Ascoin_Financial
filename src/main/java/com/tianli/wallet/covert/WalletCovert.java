package com.tianli.wallet.covert;

import com.tianli.wallet.vo.WalletActiveVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-06
 **/
@Mapper(componentModel = "spring")
public interface WalletCovert {

    @Mapping(target = "status", expression = "java(com.tianli.wallet.enums.WalletActiveStatus.getByType(walletActive.getStatus()))")
    WalletActiveVo toVo(AccountActive walletActive);
}
