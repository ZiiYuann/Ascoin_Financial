package com.tianli.account.convert;

import com.tianli.account.entity.AccountBalance;
import com.tianli.account.vo.AccountBalanceVO;
import org.mapstruct.Mapper;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-11
 **/
@Mapper(componentModel = "spring")
public interface AccountConverter {

    AccountBalanceVO toVO(AccountBalance accountBalance);
}
