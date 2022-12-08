package com.tianli.chain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.chain.entity.Coin;
import com.tianli.chain.entity.CoinBase;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.management.query.CoinIoUQuery;
import com.tianli.management.query.CoinStatusQuery;
import com.tianli.management.query.CoinWithdrawQuery;

import java.util.List;
import java.util.Set;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-21
 **/
public interface CoinService extends IService<Coin> {

    /**
     * @return 集合列表
     */
    List<Coin> pushCoinsWithCache();

    /**
     * 保存或者更新
     *
     * @param query 请求
     */
    void saveOrUpdate(Long uid, CoinIoUQuery query);


    /**
     * 刷新缓存
     */
    List<CoinBase> flushCache();

    /**
     * 币别上架
     *
     * @param uid   操作人id
     * @param query 请求
     */
    void push(Long uid, CoinStatusQuery query);

    /**
     * @return 有效的币种列表
     */
    List<CoinBase> effectiveCoinsWithCache();


    /**
     * @return 有效的币种名称
     */
    Set<String> effectiveCoinNames();

    /**
     * 根据币别名称和网络获取币别
     *
     * @param name        币别名称
     * @param networkType 网络
     * @return 币信息
     */
    Coin getByNameAndNetwork(String name, NetworkType networkType);

    /**
     * 根据合约币别
     *
     * @param contract 合约
     * @return 币信息
     */
    Coin getByContract(String contract);

    /**
     * 根据合约币别
     *
     * @param name 币名
     * @return 币信息
     */
    Coin mainToken(String name);

    /**
     * 提现配置
     *
     * @param uid   操作人id
     * @param query 请求参数
     */
    void withdrawConfig(Long uid, CoinWithdrawQuery query);

}
