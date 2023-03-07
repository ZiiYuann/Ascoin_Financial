package com.tianli.account.service;

import com.tianli.account.entity.AccountBalance;
import com.tianli.account.vo.AccountBalanceMainPageVO;
import com.tianli.account.vo.AccountBalanceSimpleVO;
import com.tianli.account.vo.AccountBalanceVO;
import com.tianli.account.vo.UserAssetsVO;
import com.tianli.charge.enums.ChargeType;
import com.tianli.common.blockchain.NetworkType;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-12-08
 **/
public interface AccountBalanceService {

    /**
     * 减少余额
     */
    void decrease(long uid, ChargeType type, String coin, BigDecimal amount, String orderNo);

    void decrease(long uid, ChargeType type, String coin, BigDecimal amount, String orderNo, NetworkType networkType);

    /**
     * 增加余额
     */
    void increase(long uid, ChargeType type, String coin, BigDecimal amount, String orderNo);

    void increase(long uid, ChargeType type, String coin, BigDecimal amount, String orderNo, NetworkType networkType);

    /**
     * 冻结余额
     */
    void freeze(long uid, ChargeType type, String coin, BigDecimal amount, String orderNo);

    void freeze(long uid, ChargeType type, String coin, BigDecimal amount, String orderNo, NetworkType networkType);

    /**
     * 减少冻结余额
     */
    void reduce(long uid, ChargeType type, String coin, BigDecimal amount, String orderNo);

    void reduce(long uid, ChargeType type, String coin, BigDecimal amount, String orderNo, NetworkType networkType);

    /**
     * 解冻余额
     */
    void unfreeze(long uid, ChargeType type, String coin, BigDecimal amount, String orderNo);

    void unfreeze(long uid, ChargeType type, String coin, BigDecimal amount, String orderNo, NetworkType networkType);

    /**
     * 质押冻结
     */
    void pledgeFreeze(long uid, ChargeType type, String coin, BigDecimal amount, String orderNo);

    void pledgeFreeze(long uid, ChargeType type, String coin, BigDecimal amount, String sn, NetworkType networkType);

    void pledgeReduce(long uid, ChargeType type, String coin, BigDecimal amount, String orderNo);

    void pledgeReduce(long uid, ChargeType type, String coin, BigDecimal amount, String orderNo, NetworkType networkType);

    /**
     * 质押解冻
     */
    void pledgeUnfreeze(long uid, ChargeType type, String coin, BigDecimal amount, String orderNo);


    /**
     * 获取并且不存在的话会初始化
     */
    AccountBalance getAndInit(long uid, String coinName);

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
    UserAssetsVO getAllUserAssetsVO(Long uid);

    /**
     * 获取用户集合的总资产信息（总余额 + 理财持有 + 基金持有）
     *
     * @param uids 用户id集合
     * @return 资产信息
     */
    UserAssetsVO getAllUserAssetsVO(List<Long> uids);

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

    /**
     * 获取账户余额总数据
     *
     * @return 总余额
     */
    List<AccountBalanceSimpleVO> accountBalanceSimpleVOs();


}
