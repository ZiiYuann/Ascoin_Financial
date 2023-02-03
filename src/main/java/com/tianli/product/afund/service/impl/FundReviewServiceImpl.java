package com.tianli.product.afund.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tianli.product.afund.entity.FundReview;
import com.tianli.product.afund.dao.FundReviewMapper;
import com.tianli.product.afund.service.IFundReviewService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xianeng
 * @since 2022-09-07
 */
@Service
public class FundReviewServiceImpl extends ServiceImpl<FundReviewMapper, FundReview> implements IFundReviewService {

    @Override
    public List<FundReview> getListByRid(Long id) {
        return this.list(new QueryWrapper<FundReview>().lambda().eq(FundReview::getRId,id));
    }
}
