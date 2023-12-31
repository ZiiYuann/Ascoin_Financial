package com.tianli.product.afund.service;

import com.tianli.product.afund.entity.FundReview;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xianeng
 * @since 2022-09-07
 */
public interface IFundReviewService extends IService<FundReview> {

    List<FundReview> getListByRid(Long id);
}
