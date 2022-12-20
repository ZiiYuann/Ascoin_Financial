package com.tianli.other.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.CommonFunction;
import com.tianli.other.convert.OtherConvert;
import com.tianli.other.entity.Banner;
import com.tianli.other.mapper.BannerMapper;
import com.tianli.other.query.BannerIoUQuery;
import com.tianli.other.query.MBannerListQuery;
import com.tianli.other.service.BannerService;
import com.tianli.other.vo.BannerVO;
import com.tianli.other.vo.MBannerVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author chenb
 * @apiNote
 * @since 2022-12-12
 **/
@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

    @Resource
    private OtherConvert otherConvert;
    @Resource
    private BannerMapper bannerMapper;

    @Override
    @Transactional
    public void saveOrUpdate(BannerIoUQuery query) {
        Banner banner = otherConvert.toDO(query);
        // 插入
        if (Objects.isNull(query.getId())) {

            banner.setId(CommonFunction.generalId());
            bannerMapper.insert(banner);
        }

        // 修改
        if (Objects.nonNull(query.getId())) {
            bannerMapper.updateById(banner);
        }

    }

    @Override
    public IPage<MBannerVO> MList(Page<Banner> page, MBannerListQuery query) {
        LambdaQueryWrapper<Banner> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(query.getName())) {
            queryWrapper = queryWrapper.like(Banner::getName, query.getName());
        }

        if (StringUtils.isNotBlank(query.getJumpUrl())) {
            queryWrapper = queryWrapper.like(Banner::getJumpUrl, query.getJumpUrl());
        }

        if (Objects.nonNull(query.getStatus())) {
            //  0：未开始 1：进行中 2：过期
            LocalDateTime now = LocalDateTime.now();
            switch (query.getStatus()) {
                case 0:
                    queryWrapper = queryWrapper.lt(Banner::getStartTime, now);
                    break;
                case 1:
                    queryWrapper = queryWrapper.ge(Banner::getStartTime, now);
                    queryWrapper = queryWrapper.lt(Banner::getEndTime, now);
                    break;
                case 3:
                    queryWrapper = queryWrapper.ge(Banner::getEndTime, now);
                    break;
            }
        }

        if (Objects.nonNull(query.getStartTime())) {
            queryWrapper = queryWrapper.ge(Banner::getStartTime, query.getStartTime());
        }

        if (Objects.nonNull(query.getEndTime())) {
            queryWrapper = queryWrapper.lt(Banner::getEndTime, query.getStartTime());
        }

        queryWrapper.orderByDesc(Banner::getWeight);

        return this.page(page, queryWrapper).convert(otherConvert::toMBannerVO);
    }

    @Override
    public List<BannerVO> processList() {
        LocalDateTime now = LocalDateTime.now();
        var queryWrapper = new LambdaQueryWrapper<Banner>()
                .lt(Banner::getStartTime, now)
                .ge(Banner::getEndTime, now)
                .orderByDesc(Banner :: getWeight);
        return this.list(queryWrapper).stream().map(otherConvert::toBannerVO).collect(Collectors.toList());
    }

    @Override
    public void delete(List<Long> ids) {
        bannerMapper.deleteBatchIds(ids);
    }


}
