package com.tianli.fund.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.charge.service.OrderService;
import com.tianli.common.PageQuery;
import com.tianli.fund.convert.FundRecordConvert;
import com.tianli.fund.dao.FundTransactionRecordMapper;
import com.tianli.fund.entity.FundTransactionRecord;
import com.tianli.fund.enums.FundTransactionType;
import com.tianli.fund.query.FundTransactionQuery;
import com.tianli.fund.service.IFundTransactionRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.fund.vo.FundTransactionRecordVO;
import com.tianli.management.dto.AmountDto;
import com.tianli.management.vo.FundTransactionAmountVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 基金交易记录 服务实现类
 * </p>
 *
 * @author xianeng
 * @since 2022-08-30
 */
@Service
@Transactional
public class FundTransactionRecordServiceImpl extends ServiceImpl<FundTransactionRecordMapper, FundTransactionRecord> implements IFundTransactionRecordService {

    @Autowired
    private FundTransactionRecordMapper fundTransactionRecordMapper;

    @Autowired
    private FundRecordConvert fundRecordConvert;

    @Autowired
    private OrderService orderService;

    @Override
    public IPage<FundTransactionRecordVO> getTransactionPage(PageQuery<FundTransactionRecord> page, FundTransactionQuery query) {
        LambdaQueryWrapper<FundTransactionRecord> lambda = new QueryWrapper<FundTransactionRecord>().lambda();
        if(Objects.nonNull(query)) {
            if (Objects.nonNull(query.getFundId())) {
                lambda.eq(FundTransactionRecord::getFundId, query.getFundId());
            }
            if (Objects.nonNull(query.getUid())) {
                lambda.eq(FundTransactionRecord::getFundId, query.getUid());
            }
            if(StrUtil.isNotBlank(query.getQueryUid())){
                lambda.like(FundTransactionRecord::getUid,query.getQueryFundId());
            }
            if(StrUtil.isNotBlank(query.getQueryFundId())){
                lambda.like(FundTransactionRecord::getFundId,query.getQueryFundId());
            }
            if(StrUtil.isNotBlank(query.getQueryProductId())){
                lambda.like(FundTransactionRecord::getProductId,query.getQueryProductId());
            }
            if((Objects.nonNull(query.getType()))){
                lambda.eq(FundTransactionRecord::getType,query.getType());
            }
            if(Objects.nonNull(query.getStatus())){
                lambda.eq(FundTransactionRecord::getStatus,query.getStatus());
            }
            if(Objects.nonNull(query.getStartTime())){
                lambda.ge(FundTransactionRecord::getCreateTime,query.getStartTime());
            }
            if(Objects.nonNull(query.getEndTime())){
                lambda.le(FundTransactionRecord::getCreateTime,query.getEndTime());
            }
        }
        lambda.orderByDesc(FundTransactionRecord::getCreateTime);
        return fundTransactionRecordMapper.selectPage(page.page(),lambda)
                .convert(fundRecordConvert::toFundTransactionVO);
    }

    @Override
    public FundTransactionAmountVO getTransactionAmount(FundTransactionQuery query) {
        query.setType(FundTransactionType.purchase);
        List<AmountDto> purchaseAmount = fundTransactionRecordMapper.getTransactionAmount(query);

        query.setType(FundTransactionType.redemption);
        List<AmountDto> redemptionAmount = fundTransactionRecordMapper.getTransactionAmount(query);

        return FundTransactionAmountVO.builder()
                .purchaseAmount(orderService.calDollarAmount(purchaseAmount))
                .redemptionAmount(orderService.calDollarAmount(redemptionAmount))
                .build();
    }
}
