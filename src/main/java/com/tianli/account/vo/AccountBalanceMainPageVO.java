package com.tianli.account.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-11
 **/
@Data
public class AccountBalanceMainPageVO {

    /**
     *  总余额
     */
    private BigDecimal totalAccountBalance;

    /**
     * 单个账户余额
     */
    private List<AccountBalanceVO> accountBalances;

}
