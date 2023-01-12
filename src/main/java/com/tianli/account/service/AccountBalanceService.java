package com.tianli.account.service;

import com.tianli.account.vo.AccountBalanceMainPageVO;
import com.tianli.account.vo.AccountBalanceVO;
import com.tianli.account.vo.UserAssetsVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-12-08
 **/
public interface AccountBalanceService {

    /**
     * 获取用户云钱包汇总信息（总资产 + 各个币种资产）
     *
     * @param uid     用户id
     * @param version 版本
     * @return 汇总信息
     */
    AccountBalanceMainPageVO accountSummary(Long uid, int version);

    /**
     * 获取用户云钱包汇总信息（总资产 + 各个币种资产）
     *
     * @param uid       用户id
     * @param fixedCoin 固定币别
     * @param version   版本
     * @return 汇总信息
     */
    AccountBalanceMainPageVO accountSummary(Long uid, boolean fixedCoin, int version);

    /**
     * 获取用户所有币别列表
     * {@link AccountBalanceService#accountSingleCoin(Long, String)} 的列表形式
     *
     * @param uid 用户id
     * @return 账户信息
     */
    List<AccountBalanceVO> accountList(Long uid);

    /**
     * 获取用户单币种账户信息
     *
     * @param uid      用户id
     * @param coinName 币别
     * @return 账户信息
     */
    AccountBalanceVO accountSingleCoin(Long uid, String coinName);

    /**
     * 获取用户的总资产信息（总余额 + 理财持有 + 基金持有）
     *
     * @param uid 用户id
     * @return 资产信息
     */
    UserAssetsVO getUserAssetsVO(Long uid);

    /**
     * 获取用户集合的总资产信息（总余额 + 理财持有 + 基金持有）
     *
     * @param uids 用户id集合
     * @return 资产信息
     */
    UserAssetsVO getUserAssetsVO(List<Long> uids);

    /**
     * 获取用户集合的总资产信息（总余额 + 理财持有 + 基金持有） map
     *
     * @param uids 用户id集合
     * @return 资产信息集合
     */
    List<UserAssetsVO> getUserAssetsVOMap(List<Long> uids);

    /**
     * 获取用户的总余额
     *
     * @param uid 用户id
     * @return 总余额
     */
    BigDecimal dollarBalance(Long uid);
}
