package com.tianli.fund.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.charge.service.OrderService;
import com.tianli.common.PageQuery;
import com.tianli.fund.convert.FundRecordConvert;
import com.tianli.fund.dao.FundIncomeRecordMapper;
import com.tianli.fund.entity.FundIncomeRecord;
import com.tianli.fund.query.FundIncomeQuery;
import com.tianli.fund.service.IFundIncomeRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.fund.vo.FundIncomeRecordVO;
import com.tianli.management.dto.AmountDto;
import com.tianli.management.vo.FundIncomeAmountVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 基金收益记录 服务实现类
 * </p>
 *
 * @author xianeng
 * @since 2022-08-30
 */
@Service
@Transactional
public class FundIncomeRecordServiceImpl extends ServiceImpl<FundIncomeRecordMapper, FundIncomeRecord> implements IFundIncomeRecordService {

    @Autowired
    private FundIncomeRecordMapper fundIncomeRecordMapper;

    @Autowired
    private FundRecordConvert fundRecordConvert;

    @Autowired
    private OrderService orderService;

    @Override
    public List<AmountDto> amountSumByUid(Long uid, Integer status) {
        FundIncomeQuery query = new FundIncomeQuery();
        query.setUid(uid);
        query.setStatus(status);
        return fundIncomeRecordMapper.selectAmount(query);
    }

    @Override
    public IPage<FundIncomeRecordVO> getPage(PageQuery<FundIncomeRecord> page, FundIncomeQuery query) {

        LambdaQueryWrapper<FundIncomeRecord> lambda = new QueryWrapper<FundIncomeRecord>().lambda();
        if(Objects.nonNull(query)) {
            if (Objects.nonNull(query.getFundId())) {
                lambda.eq(FundIncomeRecord::getFundId, query.getFundId());
            }
            if (Objects.nonNull(query.getUid())) {
                lambda.eq(FundIncomeRecord::getFundId, query.getUid());
            }
            if(StrUtil.isNotBlank(query.getQueryUid())){
                lambda.like(FundIncomeRecord::getUid,query.getQueryUid());
            }
            if(StrUtil.isNotBlank(query.getQueryProductId())){
                lambda.like(FundIncomeRecord::getProductId,query.getQueryProductId());
            }
            if (Objects.nonNull(query.getStartTime())) {
                lambda.ge(FundIncomeRecord::getCreateTime, query.getStartTime());
            }
            if (Objects.nonNull(query.getEndTime())) {
                lambda.le(FundIncomeRecord::getCreateTime, query.getEndTime());
            }
        }
        lambda .orderByDesc(FundIncomeRecord::getCreateTime);
        return fundIncomeRecordMapper.selectPage(page.page(),lambda)
                .convert(fundRecordConvert::toFundIncomeVO);
    }

    @Override
    public FundIncomeAmountVO getAmount(FundIncomeQuery query) {
        List<AmountDto> amountDtos = fundIncomeRecordMapper.selectAmount(query);
        BigDecimal interestAmount = orderService.calDollarAmount(amountDtos);
        return FundIncomeAmountVO.builder()
                .interestAmount(interestAmount).build();
    }
}
