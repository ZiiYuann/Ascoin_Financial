package com.tianli.other.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.other.entity.Banner;
import com.tianli.other.query.BannerIoUQuery;
import com.tianli.other.query.MBannerListQuery;
import com.tianli.other.vo.BannerVO;
import com.tianli.other.vo.MBannerVO;

import java.util.List;

public interface BannerService extends IService<Banner> {


    /**
     * 插入或者保存
     *
     * @param bannerIoUQuery 请求参数
     */
    void saveOrUpdate(BannerIoUQuery bannerIoUQuery);

    /**
     * 管理端列表
     *
     * @param page  分页参数
     * @param query 请求参数
     * @return 分页列表
     */
    IPage<MBannerVO> MList(Page<Banner> page, MBannerListQuery query);

    /**
     * 获取进行中的广告
     *
     * @return 进行中列表
     */
    List<BannerVO> processList();

    /**
     * 删除
     *
     * @param ids id集合
     */
    void delete(List<Long> ids);
}
