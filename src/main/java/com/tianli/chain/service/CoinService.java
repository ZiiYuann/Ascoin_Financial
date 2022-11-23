package com.tianli.chain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.chain.entity.Coin;
import com.tianli.management.query.CoinIoUQuery;
import com.tianli.management.query.CoinStatusQuery;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-21
 **/
public interface CoinService extends IService<Coin> {


    /**
     * 保存或者更新
     *
     * @param query 请求
     */
    void saveOrUpdate(Long uid, CoinIoUQuery query);


    /**
     * 刷新缓存
     */
    void flushCache();

    /**
     * 修改状态
     *
     * @param uid   操作人id
     * @param query 请求
     */
    void status(Long uid, CoinStatusQuery query);

}
