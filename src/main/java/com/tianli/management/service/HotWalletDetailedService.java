package com.tianli.management.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.management.entity.HotWalletDetailed;
import com.tianli.management.query.HotWalletDetailedIoUQuery;
import com.tianli.management.query.HotWalletDetailedPQuery;
import com.tianli.management.vo.HotWalletBalanceVO;
import com.tianli.management.vo.HotWalletDetailedSummaryDataVO;
import com.tianli.management.vo.HotWalletDetailedVO;

import java.math.BigDecimal;
import java.util.List;

public interface HotWalletDetailedService extends IService<HotWalletDetailed> {

    /**
     * 热钱包数据
     *
     * @param query 请求参数
     * @return vo
     */
    HotWalletDetailedSummaryDataVO summaryData(HotWalletDetailedPQuery query);

    /**
     * 热钱包余额
     *
     * @return 余额列表
     */
    List<HotWalletBalanceVO> balance();

    /**
     * 热钱包余额 u
     */
    BigDecimal balanceFee();

    /**
     * 明细列表
     *
     * @param page  分页参数
     * @param query 请求参数
     * @return 分页列表
     */
    IPage<HotWalletDetailedVO> pageByQuery(Page<HotWalletDetailed> page, HotWalletDetailedPQuery query);

    /**
     * 插入或者更新
     *
     * @param ioUQuery query
     */
    void insertOrUpdate(HotWalletDetailedIoUQuery ioUQuery);

    /**
     * 插入数据
     *
     * @param hotWalletDetailed do
     */
    void insert(HotWalletDetailed hotWalletDetailed);

    /**
     * 删除数据
     *
     * @param id 主键
     */
    void delete(Long id);

    /**
     * 主币余额
     * @return
     */
    List<HotWalletBalanceVO> mainBalance();
}
