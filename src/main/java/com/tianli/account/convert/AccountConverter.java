package com.tianli.account.convert;

import com.tianli.account.entity.AccountBalance;
import com.tianli.account.entity.AccountUserTransfer;
import com.tianli.account.vo.AccountBalanceOperationLogVO;
import com.tianli.account.vo.AccountBalanceVO;
import com.tianli.account.vo.AccountUserTransferVO;
import com.tianli.charge.entity.Order;
import org.mapstruct.Mapper;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-11
 **/
@Mapper(componentModel = "spring")
public interface AccountConverter {

    AccountBalanceVO toVO(AccountBalance accountBalance);

    AccountUserTransferVO toAccountUserTransferVO(AccountUserTransfer accountUserTransfer);

    AccountBalanceOperationLogVO toAccountBalanceOperationLogVO(Order order);
}
