package com.tianli.account.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.account.entity.AccountUserTransfer;
import com.tianli.openapi.query.UserTransferQuery;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-07
 **/
public interface AccountUserTransferService extends IService<AccountUserTransfer> {

    AccountUserTransfer transfer(UserTransferQuery query);
}
