package com.tianli.product.aborrow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.product.aborrow.convert.BorrowConvert;
import com.tianli.product.aborrow.dto.BorrowRecordPledgeDto;
import com.tianli.product.aborrow.entity.BorrowOperationLog;
import com.tianli.product.aborrow.entity.BorrowRecordPledge;
import com.tianli.product.aborrow.enums.PledgeType;
import com.tianli.product.aborrow.mapper.BorrowRecordPledgeMapper;
import com.tianli.product.aborrow.query.PledgeContextQuery;
import com.tianli.product.aborrow.service.BorrowOperationLogService;
import com.tianli.product.aborrow.service.BorrowRecordPledgeService;
import com.tianli.product.afinancial.entity.FinancialRecord;
import com.tianli.product.afinancial.service.FinancialRecordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-09
 **/
@Service
public class BorrowRecordPledgeServiceImpl extends ServiceImpl<BorrowRecordPledgeMapper, BorrowRecordPledge>
        implements BorrowRecordPledgeService {

    @Resource
    private BorrowConvert borrowConvert;
    @Resource
    private FinancialRecordService financialRecordService;
    @Resource
    private OrderService orderService;
    @Resource
    private BorrowOperationLogService borrowOperationLogService;

    @Override
    public void save(Long uid, Long bid, PledgeContextQuery query) {
        if (PledgeType.WALLET.equals(query.getPledgeType())) {
            BorrowRecordPledge borrowRecordPledge = getAndInit(uid, query.getCoin(), query.getPledgeType(), null);
            this.casIncrease(uid, query.getCoin(), query.getPledgeAmount(), borrowRecordPledge.getAmount(), query.getPledgeType());

            Order order = Order.success(uid, ChargeType.pledge, query.getCoin(), query.getPledgeAmount(), bid);
            orderService.save(order);

            BorrowOperationLog operationLog = BorrowOperationLog.log(ChargeType.pledge, bid, uid, query.getCoin(), query.getPledgeAmount());
            borrowOperationLogService.save(operationLog);
        }

        if (PledgeType.FINANCIAL.equals(query.getPledgeType())) {
            query.getRecordIds().forEach(recordId -> {

                BigDecimal amount = financialRecordService.selectById(recordId, uid).getHoldAmount();
                financialRecordService.updatePledge(uid, recordId, amount, true);

                BorrowRecordPledge borrowRecordPledge = getAndInit(uid, query.getCoin(), query.getPledgeType(), recordId);
                borrowRecordPledge.setAmount(amount);

                Order order = Order.success(uid, ChargeType.pledge, query.getCoin(), amount, bid);
                orderService.save(order);

                BorrowOperationLog operationLog = BorrowOperationLog.log(ChargeType.pledge, bid, uid, query.getCoin(), amount);
                borrowOperationLogService.save(operationLog);
            });
        }

    }


    @Override
    public List<BorrowRecordPledgeDto> dtoListByUid(Long uid) {
        return this.list(new LambdaQueryWrapper<BorrowRecordPledge>()
                        .eq(BorrowRecordPledge::getUid, uid))
                .stream().map(index -> {
                    BorrowRecordPledgeDto borrowRecordPledgeDto = borrowConvert.toBorrowRecordPledgeDto(index);

                    if (PledgeType.FINANCIAL.equals(index.getPledgeType())) {
                        FinancialRecord financialRecord = financialRecordService.selectById(index.getRecordId(), uid);
                        borrowRecordPledgeDto.setFinancialRecord(financialRecord);
                    }
                    return borrowRecordPledgeDto;
                }).collect(Collectors.toList());
    }

    private void casIncrease(Long uid, String coin, BigDecimal increaseAmount, BigDecimal originalAmount, PledgeType pledgeType) {
        int i = baseMapper.casIncrease(uid, coin, increaseAmount, originalAmount, pledgeType);
        if (i == 1) {
            throw ErrorCodeEnum.BORROW_RECORD_PLEDGE_ERROR.generalException();
        }
    }

    private void casDecrease(Long uid, String coin, BigDecimal decreaseAmount, BigDecimal originalAmount, PledgeType pledgeType) {
        int i = baseMapper.casDecrease(uid, coin, decreaseAmount, originalAmount, pledgeType);
        if (i == 1) {
            throw ErrorCodeEnum.BORROW_RECORD_PLEDGE_ERROR.generalException();
        }
    }

    private BorrowRecordPledge getAndInit(Long uid, String coin, PledgeType pledgeType, Long recordId) {
        BorrowRecordPledge borrowRecordPledge = this.getOne(new LambdaQueryWrapper<BorrowRecordPledge>()
                .eq(BorrowRecordPledge::getPledgeType, pledgeType)
                .eq(BorrowRecordPledge::getUid, uid)
                .eq(BorrowRecordPledge::getCoin, coin)
                .eq(BorrowRecordPledge::getRecordId, recordId));

        if (Objects.isNull(borrowRecordPledge)) {
            borrowRecordPledge = BorrowRecordPledge.builder()
                    .uid(uid)
                    .coin(coin)
                    .pledgeType(pledgeType)
                    .recordId(recordId)
                    .amount(BigDecimal.ZERO)
                    .build();
            this.save(borrowRecordPledge);
        }
        return borrowRecordPledge;
    }
}
