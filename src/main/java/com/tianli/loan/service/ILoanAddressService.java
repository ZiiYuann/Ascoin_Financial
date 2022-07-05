package com.tianli.loan.service;

import com.tianli.currency_token.mapper.ChainType;
import com.tianli.loan.entity.LoanAddress;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户还款地址表 服务类
 * </p>
 *
 * @author lzy
 * @since 2022-05-31
 */
public interface ILoanAddressService extends IService<LoanAddress> {

    /**
     * 查询用户还款地址
     * @param uid
     * @return
     */
    LoanAddress findByUid(Long uid);

    LoanAddress findByAddress(String address, ChainType chainType);

}
