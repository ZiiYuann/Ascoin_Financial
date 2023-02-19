package com.tianli.product.aborrow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.product.aborrow.convert.BorrowConvert;
import com.tianli.product.aborrow.dto.BorrowRecordPledgeDto;
import com.tianli.product.aborrow.entity.BorrowOperationLog;
import com.tianli.product.aborrow.entity.BorrowRecordPledge;
import com.tianli.product.aborrow.enums.ModifyPledgeContextType;
import com.tianli.product.aborrow.enums.PledgeType;
import com.tianli.product.aborrow.mapper.BorrowRecordPledgeMapper;
import com.tianli.product.aborrow.query.PledgeContextQuery;
import com.tianli.product.aborrow.service.BorrowOperationLogService;
import com.tianli.product.aborrow.service.BorrowRecordCoinService;
import com.tianli.product.aborrow.service.BorrowRecordPledgeService;
import com.tianli.product.afinancial.entity.FinancialRecord;
import com.tianli.product.afinancial.enums.ProductType;
import com.tianli.product.afinancial.service.FinancialRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Resource
    private AccountBalanceService accountBalanceService;
    @Resource
    private BorrowRecordCoinService borrowRecordCoinService;

    @Override
    @Transactional
    public void save(Long uid, Long bid, PledgeContextQuery query) {
        ChargeType pledge = ChargeType.pledge;
        if (PledgeType.WALLET.equals(query.getPledgeType())) {
            BorrowRecordPledge borrowRecordPledge = getAndInit(uid, bid, query.getCoin(), query.getPledgeType(), null);
            this.casIncrease(borrowRecordPledge.getId(), query.getCoin(), query.getPledgeAmount(), borrowRecordPledge.getAmount(), query.getPledgeType());
            Order order = Order.success(uid, pledge, query.getCoin(), query.getPledgeAmount(), bid);
            orderService.save(order);

            BorrowOperationLog operationLog = BorrowOperationLog.log(pledge, bid, uid, query.getCoin(), query.getPledgeAmount());
            borrowOperationLogService.saveOrUpdate(operationLog);

            accountBalanceService.pledgeFreeze(uid, pledge, order.getCoin(), query.getPledgeAmount(), order.getOrderNo(), pledge.getNameZn());
        }

        if (PledgeType.FINANCIAL.equals(query.getPledgeType())) {
            Long recordId = query.getRecordId();
            FinancialRecord financialRecord = financialRecordService.selectById(recordId, uid);
            if (ProductType.fixed.equals(financialRecord.getProductType())) {
                throw ErrorCodeEnum.BORROW_FIXED_PRODUCT_ERROR.generalException();
            }
            if (financialRecord.isPledge()) {
                throw ErrorCodeEnum.BORROW_PRODUCT_HAVE_PLEDGE.generalException();
            }

            BigDecimal amount = financialRecord.getHoldAmount();
            financialRecordService.updatePledge(uid, recordId, amount, true);

            BorrowRecordPledge borrowRecordPledge = getAndInit(uid, bid, financialRecord.getCoin(), query.getPledgeType(), recordId);
            borrowRecordPledge.setAmount(amount);

            Order order = Order.success(uid, pledge, financialRecord.getCoin(), amount, bid);
            orderService.save(order);

            // 此日志只做插入不做显示
            BorrowOperationLog operationLog = BorrowOperationLog.log(pledge, bid, uid, financialRecord.getCoin(), amount);
            borrowOperationLogService.save(operationLog);

            // 插入的query中可能不带币别
            query.setCoin(financialRecord.getCoin());
        }

    }

    @Override
    public void save(Long uid, Long bid, PledgeContextQuery query, ModifyPledgeContextType type) {

        if (ModifyPledgeContextType.ADD.equals(type)) {
            this.save(uid, bid, query);
            return;
        }

        PledgeType pledgeType = query.getPledgeType();
        ChargeType release = ChargeType.release;
        BorrowRecordPledge borrowRecordPledge = getAndInit(uid, bid, query.getCoin(), pledgeType, null);
        if (PledgeType.WALLET.equals(pledgeType)) {
            this.casDecrease(borrowRecordPledge.getId(), query.getCoin(), query.getPledgeAmount(), borrowRecordPledge.getAmount(), pledgeType);
            Order order = Order.success(uid, release, query.getCoin(), query.getPledgeAmount(), bid);
            orderService.save(order);

            accountBalanceService.pledgeUnfreeze(uid, release, order.getCoin(), query.getPledgeAmount()
                    , order.getOrderNo(), release.getNameZn());

            // 在外层保存操作日志，因为需要显示
        }

        if (PledgeType.FINANCIAL.equals(pledgeType)) {
            // 无法通过调整质押物来释放活期质押
            ErrorCodeEnum.BORROW_PRODUCT_CAN_NOT_REPAY.throwException();
        }

    }

    @Override
    public void release(Long uid, Long bid) {
        List<BorrowRecordPledge> borrowRecordPledges = this.list(new LambdaQueryWrapper<BorrowRecordPledge>()
                .eq(BorrowRecordPledge::getUid, uid)
                .eq(BorrowRecordPledge::getBid, bid));
        ChargeType release = ChargeType.release;
        borrowRecordPledges.forEach(recordPledge -> {
            PledgeType pledgeType = recordPledge.getPledgeType();
            BigDecimal amount = recordPledge.getAmount();
            String coin = recordPledge.getCoin();


            if (PledgeType.WALLET.equals(pledgeType)) {
                this.casDecrease(recordPledge.getId(), recordPledge.getCoin(), amount, amount, pledgeType);
                Order order = Order.success(uid, release, coin, amount, bid);
                orderService.save(order);

                accountBalanceService.pledgeUnfreeze(uid, release, coin, amount
                        , order.getOrderNo(), release.getNameZn());

                // 此日志只做插入不做显示
                BorrowOperationLog operationLog = BorrowOperationLog.log(release, bid, uid, coin, amount);
                borrowOperationLogService.save(operationLog);
                return;
            }

            if (PledgeType.FINANCIAL.equals(pledgeType) && !borrowRecordCoinService.payOff(uid, bid)) {
                ErrorCodeEnum.BORROW_PRODUCT_CAN_NOT_REPAY.throwException();
            }
            Long recordId = recordPledge.getRecordId();
            FinancialRecord financialRecord = financialRecordService.selectById(recordId, uid);
            financialRecordService.updatePledge(uid, recordId, financialRecord.getHoldAmount(), false);

            Order order = Order.success(uid, release, financialRecord.getCoin(), financialRecord.getHoldAmount(), bid);
            // 此日志只做插入不做显示
            BorrowOperationLog operationLog = BorrowOperationLog.log(release, bid, uid, financialRecord.getCoin()
                    , financialRecord.getHoldAmount());
            borrowOperationLogService.save(operationLog);
            orderService.save(order);

        });
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
                        borrowRecordPledgeDto.setAmount(financialRecord.getHoldAmount());
                    }
                    return borrowRecordPledgeDto;
                }).collect(Collectors.toList());
    }

    @Override
    public List<BorrowRecordPledge> listByUid(Long uid) {
        return this.list(new LambdaQueryWrapper<BorrowRecordPledge>()
                .eq(BorrowRecordPledge::getUid, uid));
    }

    @Override
    public boolean releaseCompleted(Long uid, Long bid) {
        List<BorrowRecordPledge> borrowRecordPledges = this.list(
                new LambdaQueryWrapper<BorrowRecordPledge>()
                        .eq(BorrowRecordPledge::getUid, uid)
                        .eq(BorrowRecordPledge::getBid, bid)
        );


        boolean empty = borrowRecordPledges.stream().filter(index -> index.getAmount().compareTo(BigDecimal.ZERO) > 0).findAny()
                .isEmpty();

        List<Long> recordIds = borrowRecordPledges.stream()
                .filter(index -> PledgeType.FINANCIAL.equals(index.getPledgeType()))
                .map(BorrowRecordPledge::getRecordId).collect(Collectors.toList());
        List<FinancialRecord> financialRecords = financialRecordService.listByIds(recordIds);
        boolean empty1 = financialRecords.stream().filter(FinancialRecord::isPledge).findAny().isEmpty();
        return empty1 && empty;
    }

    private void casIncrease(Long id, String coin, BigDecimal increaseAmount, BigDecimal originalAmount, PledgeType pledgeType) {
        int i = baseMapper.casIncrease(id, coin, increaseAmount, originalAmount, pledgeType);
        if (i != 1) {
            throw ErrorCodeEnum.BORROW_RECORD_PLEDGE_ERROR.generalException();
        }
    }

    private void casDecrease(Long id, String coin, BigDecimal decreaseAmount, BigDecimal originalAmount, PledgeType pledgeType) {
        int i = baseMapper.casDecrease(id, coin, decreaseAmount, originalAmount, pledgeType);
        if (i != 1) {
            throw ErrorCodeEnum.BORROW_RECORD_PLEDGE_ERROR.generalException();
        }
    }

    private BorrowRecordPledge getAndInit(Long uid, Long bid, String coin, PledgeType pledgeType, Long recordId) {

        LambdaQueryWrapper<BorrowRecordPledge> eq = new LambdaQueryWrapper<BorrowRecordPledge>()
                .eq(BorrowRecordPledge::getPledgeType, pledgeType)
                .eq(BorrowRecordPledge::getUid, uid)
                .eq(BorrowRecordPledge::getCoin, coin)
                .eq(BorrowRecordPledge::getBid, bid);

        if (Objects.nonNull(recordId)) {
            eq = eq.eq(BorrowRecordPledge::getRecordId, recordId);
        }

        BorrowRecordPledge borrowRecordPledge = this.getOne(eq);

        if (Objects.isNull(borrowRecordPledge)) {
            borrowRecordPledge = BorrowRecordPledge.builder()
                    .uid(uid)
                    .bid(bid)
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
