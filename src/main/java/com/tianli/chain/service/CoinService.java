package com.tianli.chain.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.chain.entity.Coin;
import com.tianli.management.query.CoinIoUQuery;
import com.tianli.management.query.CoinStatusQuery;
import com.tianli.management.query.CoinsQuery;
import com.tianli.management.vo.MCoinListVO;

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
     * 币别上架
     *
     * @param uid   操作人id
     * @param query 请求
     */
    void push(Long uid, CoinStatusQuery query);

    /**
     * 分页列表
     * @param page  分页参数
     * @param query 请求参数
     * @return 列表
     */
    IPage<MCoinListVO> list(Page<Coin> page, CoinsQuery query);

}
