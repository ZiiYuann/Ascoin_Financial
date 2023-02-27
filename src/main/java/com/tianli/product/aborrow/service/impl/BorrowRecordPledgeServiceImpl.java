package com.tianli.product.aborrow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.chain.service.CoinBaseService;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderService;
import com.tianli.currency.service.CurrencyService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.product.aborrow.convert.BorrowConvert;
import com.tianli.product.aborrow.dto.BorrowRecordPledgeDto;
import com.tianli.product.aborrow.entity.BorrowHedgeEntrust;
import com.tianli.product.aborrow.entity.BorrowOperationLog;
import com.tianli.product.aborrow.entity.BorrowRecord;
import com.tianli.product.aborrow.entity.BorrowRecordPledge;
import com.tianli.product.aborrow.enums.*;
import com.tianli.product.aborrow.mapper.BorrowRecordPledgeMapper;
import com.tianli.product.aborrow.query.PledgeContextQuery;
import com.tianli.product.aborrow.service.*;
import com.tianli.product.aborrow.vo.BorrowRecordPledgeVO;
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
    @Resource
    private CoinBaseService coinBaseService;
    @Resource
    private CurrencyService currencyService;
    @Resource
    private BorrowHedgeEntrustService borrowHedgeEntrustService;
    @Resource
    private BorrowRecordService borrowRecordService;
    @Resource
    private BorrowConfigPledgeService borrowConfigPledgeService;

    @Override
    @Transactional
    public void save(Long uid, Long bid, PledgeContextQuery query, boolean auto, boolean addLog) {
        PledgeType pledgeType = query.getPledgeType();

        boolean f = PledgeType.FINANCIAL.equals(pledgeType);
        FinancialRecord financialRecord = f ?
                financialRecordService.selectById(query.getRecordId(), uid) : FinancialRecord.builder().build();

        if (f && ProductType.fixed.equals(financialRecord.getProductType())) {
            throw ErrorCodeEnum.BORROW_FIXED_PRODUCT_ERROR.generalException();
        }
        if (f && financialRecord.isPledge()) {
            throw ErrorCodeEnum.BORROW_PRODUCT_HAVE_PLEDGE.generalException();
        }

        String coin = f ? financialRecord.getCoin() : query.getCoin();
        borrowConfigPledgeService.getById(coin);
        BigDecimal pledgeAmount = f ? financialRecord.getHoldAmount() : query.getPledgeAmount();

        BorrowRecordPledge borrowRecordPledge = getAndInit(uid, bid, coin, pledgeType, query.getRecordId());
        this.casIncrease(borrowRecordPledge.getId(), coin, pledgeAmount, borrowRecordPledge.getAmount(), pledgeType);

        ChargeType pledge = auto ? ChargeType.auto_re : ChargeType.pledge;
        Order order = Order.success(uid, pledge, coin, pledgeAmount, bid);
        orderService.save(order);

        if (addLog) {
            BorrowOperationLog operationLog = BorrowOperationLog.log(pledge, bid, uid, coin
                    , pledgeAmount, currencyService.getDollarRate(coin));
            borrowOperationLogService.saveOrUpdate(operationLog);
        }

        if (!f) {
            accountBalanceService.pledgeFreeze(uid, pledge, coin, pledgeAmount, order.getOrderNo(), pledge.getNameZn());
        }

        if (f) {
            financialRecordService.updatePledge(uid, financialRecord.getId(), pledgeAmount, true);
            query.setCoin(financialRecord.getCoin());
        }

    }

    @Override
    @Transactional
    public void modifyPledgeContext(Long uid, Long bid, PledgeContextQuery query, ModifyPledgeContextType type) {

        if (ModifyPledgeContextType.ADD.equals(type)) {
            this.save(uid, bid, query, false, false);
            return;
        }

        if (ModifyPledgeContextType.AUTO_ADD.equals(type)) {
            this.save(uid, bid, query, true, false);
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
                BorrowOperationLog operationLog = BorrowOperationLog.log(release, bid, uid, coin
                        , amount, currencyService.getDollarRate(coin));
                borrowOperationLogService.save(operationLog);
                return;
            }

            if (PledgeType.FINANCIAL.equals(pledgeType) && borrowRecordCoinService.payOff(uid, bid)) {
                ErrorCodeEnum.BORROW_PRODUCT_CAN_NOT_REPAY.throwException();
            }
            Long recordId = recordPledge.getRecordId();
            FinancialRecord financialRecord = financialRecordService.selectById(recordId, uid);
            financialRecordService.updatePledge(uid, recordId, financialRecord.getHoldAmount(), false);

            Order order = Order.success(uid, release, financialRecord.getCoin(), financialRecord.getHoldAmount(), bid);
            // 此日志只做插入不做显示
            BorrowOperationLog operationLog = BorrowOperationLog.log(release, bid, uid, financialRecord.getCoin()
                    , financialRecord.getHoldAmount(), currencyService.getDollarRate(financialRecord.getCoin()));
            borrowOperationLogService.save(operationLog);
            orderService.save(order);

        });
    }

    @Override
    public List<BorrowRecordPledgeDto> dtoListByUid(Long uid, Long bid) {
        return this.list(new LambdaQueryWrapper<BorrowRecordPledge>()
                        .eq(BorrowRecordPledge::getUid, uid)
                        .eq(BorrowRecordPledge::getBid, bid))
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
    public List<BorrowRecordPledgeVO> vos(Long uid, Long bid, PledgeType pledgeType) {
        List<BorrowRecordPledgeDto> recordPledgeDtos = this.dtoListByUid(uid, bid);

        if (Objects.nonNull(pledgeType)) {
            recordPledgeDtos = recordPledgeDtos.stream().filter(r -> pledgeType.equals(r.getPledgeType()))
                    .collect(Collectors.toList());
        }

        return recordPledgeDtos.stream().map(dto -> BorrowRecordPledgeVO.builder()
                .coin(dto.getCoin())
                .logo(coinBaseService.getByName(dto.getCoin()).getLogo())
                .amount(dto.getAmount())
                .recordId(dto.getRecordId())
                .build()).collect(Collectors.toList());
    }

    @Override
    public List<BorrowRecordPledge> listByUid(Long uid, Long bid) {
        return this.list(new LambdaQueryWrapper<BorrowRecordPledge>()
                .eq(BorrowRecordPledge::getUid, uid)
                .eq(BorrowRecordPledge::getBid, bid));
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

    @Override
    @Transactional
    public void reduce(Long uid, Long bid, PledgeContextQuery query, boolean forced) {
        // 强平所有
        if (Objects.isNull(query)) {
            var recordPledgeDtos = this.dtoListByUid(uid, bid);
            recordPledgeDtos.forEach(dto ->
            {
                PledgeContextQuery pledgeContextQuery = PledgeContextQuery.builder()
                        .recordId(dto.getRecordId())
                        .pledgeType(dto.getPledgeType())
                        .coin(dto.getCoin())
                        .pledgeAmount(dto.getAmount())
                        .build();
                this.reduce(uid, bid, pledgeContextQuery, forced);
            });

            return;
        }

        PledgeType pledgeType = query.getPledgeType();
        BorrowRecordPledge recordPledge = this.getAndInit(uid, bid, query.getCoin(), pledgeType, query.getRecordId());
        ChargeType chargeType = ChargeType.forced_closeout;
        BigDecimal originalAmount = recordPledge.getAmount();

        String coin = recordPledge.getCoin();
        BigDecimal rate = null;
        BigDecimal decreaseAmount = query.getPledgeAmount();


        BorrowOperationLog operationLog = null;
        Order order = null;
        if (PledgeType.WALLET.equals(pledgeType)) {

            if (!forced) {
                this.casDecrease(recordPledge.getId(), coin, decreaseAmount, originalAmount, pledgeType);
            }

            order = Order.success(uid, chargeType, recordPledge.getCoin(), decreaseAmount, bid);

            accountBalanceService.pledgeReduce(uid, chargeType, coin, decreaseAmount
                    , order.getOrderNo(), chargeType.getNameZn());
            rate = currencyService.getDollarRate(coin);
            // 此日志只做插入不做显示
            operationLog = BorrowOperationLog.log(chargeType, bid, uid, coin, decreaseAmount, rate);
        }

        if (PledgeType.FINANCIAL.equals(pledgeType)) {
            Long recordId = recordPledge.getRecordId();
            FinancialRecord financialRecord = financialRecordService.selectById(recordId, uid);

            if (forced) {
                BorrowRecordPledge borrowRecordPledge = getAndInit(uid, bid, coin, PledgeType.WALLET, null);
                this.casIncrease(borrowRecordPledge.getId(), coin, decreaseAmount, borrowRecordPledge.getAmount(), PledgeType.WALLET);
            }

            financialRecordService.updatePledgeAndReduce(uid, recordId, financialRecord.getHoldAmount(), !forced, decreaseAmount);
            rate = currencyService.getDollarRate(financialRecord.getCoin());
            order = Order.success(uid, chargeType, financialRecord.getCoin(), decreaseAmount, bid);
            // 此日志只做插入不做显示
            operationLog = BorrowOperationLog.log(chargeType, bid, uid, financialRecord.getCoin()
                    , decreaseAmount, rate);
        }

        // 如果不是强制平仓，则生成当前记录的副本，添加到强平记录中
        if (!forced) {
            BorrowRecord borrowRecord = borrowRecordService.copy(bid, PledgeStatus.WAIT);
            Long newBid = borrowRecord.getId();
            BorrowRecordPledge borrowRecordPledge = getAndInit(uid, newBid, coin, PledgeType.WALLET, null);
            this.casDecrease(borrowRecordPledge.getId(), coin, decreaseAmount, borrowRecordPledge.getAmount(), PledgeType.WALLET);
        }

        orderService.save(order);
        borrowOperationLogService.save(operationLog);
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
