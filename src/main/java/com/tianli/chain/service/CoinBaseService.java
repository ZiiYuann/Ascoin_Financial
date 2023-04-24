package com.tianli.chain.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.chain.entity.Coin;
import com.tianli.chain.entity.CoinBase;
import com.tianli.management.query.CoinBaseWithdrawQuery;
import com.tianli.management.query.CoinIoUQuery;
import com.tianli.management.query.CoinWithdrawQuery;
import com.tianli.management.query.CoinsQuery;
import com.tianli.management.vo.CoinBaseVO;
import com.tianli.management.vo.MCoinListVO;

import java.util.List;
import java.util.Set;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-30
 **/
public interface CoinBaseService extends IService<CoinBase> {


    /**
     * @param nickName 昵称
     * @param query    请求参数
     * @return 币别配置信息
     */
    CoinBase saveOrUpdate(String nickName, CoinIoUQuery query);

    /**
     * 分页列表
     *
     * @param page  分页参数
     * @param query 请求参数
     * @return 列表
     */
    IPage<MCoinListVO> list(Page<Coin> page, CoinsQuery query);


    /**
     * 分页列表
     *
     * @param page  分页参数
     * @param query 请求参数
     * @return 列表
     */
    IPage<CoinBaseVO> baseList(Page<CoinBase> page, CoinsQuery query);


    void updateConfig(CoinBaseWithdrawQuery query);


    /**
     * 根据名称获取币
     *
     * @param name 币别名称
     * @return 币别
     */
    CoinBase getByName(String name);

    /**
     * 显示币
     *
     * @param name 币名称
     */
    void show(String name);

    void notShow(String name);

    /**
     * 删除缓存
     *
     * @param name 币别名称
     */
    void deleteCache(String name);

    /**
     * 刷新缓存
     */
    List<CoinBase> flushPushListCache();

    /**
     * 获取缓存
     */
    List<CoinBase> getPushListCache();

    /**
     * 删除缓存
     */
    void deletePushListCache();

    /**
     * @return 有效的币种名称
     */
    Set<String> pushCoinNames();

    Set<String> pushCoinNames(int version);

}
