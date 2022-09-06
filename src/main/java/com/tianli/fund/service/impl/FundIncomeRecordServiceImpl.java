package com.tianli.fund.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.charge.service.OrderService;
import com.tianli.common.PageQuery;
import com.tianli.fund.convert.FundRecordConvert;
import com.tianli.fund.dao.FundIncomeRecordMapper;
import com.tianli.fund.dto.FundIncomeAmountDTO;
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
import java.util.stream.Collectors;

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
    public List<AmountDto> getAmountByUidAndStatus(Long uid, Integer status) {
        FundIncomeQuery query = new FundIncomeQuery();
        query.setUid(uid);
        query.setStatus(status);
        List<FundIncomeAmountDTO> fundIncomeAmountDTOS = getAmount(query);
        List<AmountDto> amountDtos = fundIncomeAmountDTOS.stream().map(fundIncomeAmountDTO ->
                new AmountDto(fundIncomeAmountDTO.getTotalAmount(), fundIncomeAmountDTO.getCoin())).collect(Collectors.toList());
        return amountDtos;
    }

    @Override
    public IPage<FundIncomeRecordVO> getPage(PageQuery<FundIncomeRecord> page, FundIncomeQuery query) {
        return fundIncomeRecordMapper.selectIncomePage(page.page(),query);
    }

    @Override
    public List<FundIncomeAmountDTO> getAmount(FundIncomeQuery query) {
        return fundIncomeRecordMapper.selectAmount(query);
    }

    @Override
    public FundIncomeAmountVO getIncomeAmount(FundIncomeQuery query) {
        List<FundIncomeAmountDTO> amount = getAmount(query);
        List<AmountDto> amountDtos = amount.stream().map(fundIncomeAmountDTO -> new AmountDto(fundIncomeAmountDTO.getTotalAmount(), fundIncomeAmountDTO.getCoin())).collect(Collectors.toList());
        BigDecimal dollarAmount = orderService.calDollarAmount(amountDtos);
        return FundIncomeAmountVO.builder().interestAmount(dollarAmount).build();
    }

    @Override
    public boolean existWaitInterest(Long agentUid) {
        int count = fundIncomeRecordMapper.selectWaitInterestCount(agentUid);
        return count>0;
    }

}
