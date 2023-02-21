package com.tianli.account.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.account.entity.AccountUserTransfer;
import com.tianli.account.vo.AccountUserTransferVO;
import com.tianli.charge.enums.ChargeType;
import com.tianli.openapi.query.UserTransferQuery;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-07
 **/
public interface AccountUserTransferService extends IService<AccountUserTransfer> {

    AccountUserTransfer getByExternalPk(Long externalPk);

    AccountUserTransfer transfer(UserTransferQuery query);

    AccountUserTransferVO getVO(Long transferId);
}
