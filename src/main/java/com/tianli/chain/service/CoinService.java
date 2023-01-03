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
     * 已经发布币别列表缓存
     *
     * @return 集合列表
     */
    List<Coin> pushCoinsWithCache();

    /**
     * 已经发布币别列表缓存
     *
     * @param name 币种名称
     * @return 集合列表
     */
    List<Coin> pushCoinsWithCache(String name);

    /**
     * 保存或者更新
     *
     * @param nickname 操作人
     * @param query    请求
     */
    void saveOrUpdate(String nickname, CoinIoUQuery query);

    /**
     * 币别上架
     *
     * @param nickname 操作人
     * @param query    请求
     */
    void push(String nickname, CoinStatusQuery query);

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
     * @param nickname 操作人
     * @param query    请求参数
     */
    void withdrawConfig(String nickname, CoinWithdrawQuery query);

    /**
     * 删除缓存
     */
    void deletePushListCache();

    /**
     * 删除币别（慎重使用）
     */
    void delete(Long id);

}
