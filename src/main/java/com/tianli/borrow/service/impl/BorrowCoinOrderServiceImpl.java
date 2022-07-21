package com.tianli.borrow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.borrow.contant.BorrowOrderStatus;
import com.tianli.borrow.contant.BorrowPledgeRate;
import com.tianli.borrow.convert.BorrowCoinConfigConverter;
import com.tianli.borrow.convert.BorrowConverter;
import com.tianli.borrow.dao.BorrowCoinConfigMapper;
import com.tianli.borrow.dto.BorrowCoinOrderDTO;
import com.tianli.borrow.entity.BorrowCoinConfig;
import com.tianli.borrow.entity.BorrowCoinOrder;
import com.tianli.borrow.dao.BorrowCoinOrderMapper;
import com.tianli.borrow.query.BorrowCoinOrderQuery;
import com.tianli.borrow.service.IBorrowCoinOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.borrow.vo.BorrowCoinConfigVO;
import com.tianli.borrow.vo.BorrowCoinMainPageVO;
import com.tianli.borrow.vo.BorrowCoinOrderVO;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderService;
import com.tianli.common.CommonFunction;
import com.tianli.common.PageQuery;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.currency.enums.CurrencyAdaptType;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.financial.entity.FinancialRecord;
import com.tianli.financial.mapper.FinancialRecordMapper;
import com.tianli.sso.init.RequestInitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 借币订单 服务实现类
 * </p>
 *
 * @author xn
 * @since 2022-07-20
 */
@Service
public class BorrowCoinOrderServiceImpl extends ServiceImpl<BorrowCoinOrderMapper, BorrowCoinOrder> implements IBorrowCoinOrderService {

    @Resource
    private BorrowConverter borrowConverter;

    @Resource
    private BorrowCoinConfigConverter borrowCoinConfigConverter;

    @Autowired
    private RequestInitService requestInitService;

    @Autowired
    private FinancialRecordMapper financialRecordMapper;

    @Autowired
    private BorrowCoinOrderMapper borrowCoinOrderMapper;

    @Autowired
    private BorrowCoinConfigMapper borrowCoinConfigMapper;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AccountBalanceService accountBalanceService;

    @Override
    public BorrowCoinMainPageVO mainPage() {
        Long uid = requestInitService.uid();

        BigDecimal totalHoldAmount = financialRecordMapper.selectTotalHoldAmount();
        BigDecimal totalBorrowAmount = borrowCoinOrderMapper.selectTotalBorrowAmount();
        BigDecimal holdAmount = financialRecordMapper.selectHoldAmountByUid(uid);
        BigDecimal borrowAmount = borrowCoinOrderMapper.selectBorrowAmountByUid(uid);
        BigDecimal pledgeAmount = borrowCoinOrderMapper.selectPledgeAmountByUid(uid);

        List<BorrowCoinMainPageVO.BorrowOrder> borrowCoinOrders = borrowCoinOrderMapper.selectList(new QueryWrapper<BorrowCoinOrder>().lambda()
                .eq(BorrowCoinOrder::getUid, uid)
                .eq(BorrowCoinOrder::getStatus, BorrowOrderStatus.INTEREST_ACCRUAL))
                .stream().map(borrowConverter::toMainVO).collect(Collectors.toList());

        //借款额度
        BigDecimal borrowQuota = holdAmount.multiply(BorrowPledgeRate.INITIAL_PLEDGE_RATE);

        return BorrowCoinMainPageVO.builder()
                .totalDepositAmount(totalHoldAmount)
                .totalBorrowAmount(totalBorrowAmount)
                .depositAmount(holdAmount)
                .borrowAmount(borrowAmount)
                .pledgeAmount(pledgeAmount)
                .borrowQuota(borrowQuota)
                .borrowOrders(borrowCoinOrders)
                .borrowRate(borrowAmount.divide(borrowQuota)).build();
    }

    @Override
    public IPage<BorrowCoinOrderVO> pageList(PageQuery<BorrowCoinOrder> pageQuery, BorrowCoinOrderQuery query) {
        Long uid = requestInitService.uid();
        Page<BorrowCoinOrder> borrowCoinOrderPage = borrowCoinOrderMapper.selectPage(pageQuery.page(), new QueryWrapper<BorrowCoinOrder>().lambda()
                .eq(BorrowCoinOrder::getUid, uid)
                .in(BorrowCoinOrder::getStatus, query.getOrderStatus()));
        return borrowCoinOrderPage.convert(borrowConverter::toVO);
    }

    @Override
    public BorrowCoinConfigVO config() {
        Long uid = requestInitService.uid();

        BorrowCoinConfig coinConfig = borrowCoinConfigMapper.selectOne(new QueryWrapper<BorrowCoinConfig>().lambda()
                .eq(BorrowCoinConfig::getCoin, CurrencyCoin.usdt.getName()));

        if(Objects.isNull(coinConfig)) ErrorCodeEnum.NO_COIN_CONFIG.throwException();

        BorrowCoinConfigVO borrowCoinConfigVO = borrowCoinConfigConverter.toVO(coinConfig);
        BigDecimal availableAmount = financialRecordMapper.selectAvailableAmountByUid(uid);
        borrowCoinConfigVO.setAvailableAmount(availableAmount);
        return borrowCoinConfigVO;
    }

    @Override
    public void borrowCoin(BorrowCoinOrderDTO borrowCoinOrderDTO) {
        Long uid = requestInitService.uid();
        BigDecimal borrowAmount = borrowCoinOrderDTO.getBorrowAmount();
        BorrowCoinConfig coinConfig = borrowCoinConfigMapper.selectOne(new QueryWrapper<BorrowCoinConfig>().lambda()
                .eq(BorrowCoinConfig::getCoin, CurrencyCoin.usdt.getName()));

        //校验单笔最大和最小数量
        BigDecimal minimumBorrow = coinConfig.getMinimumBorrow();
        BigDecimal maximumBorrow = coinConfig.getMaximumBorrow();
        if(borrowAmount.compareTo(minimumBorrow) < 0 || borrowAmount.compareTo(maximumBorrow)>0)ErrorCodeEnum.BORROW_GT_AVAILABLE_ERROR.throwException();

        //校验数量
        BigDecimal initialPledgeRate = coinConfig.getInitialPledgeRate();
        BigDecimal availableAmount = financialRecordMapper.selectAvailableAmountByUid(uid);
        if(borrowAmount.compareTo(availableAmount.multiply(initialPledgeRate)) > 0)ErrorCodeEnum.BORROW_GT_AVAILABLE_ERROR.throwException();

        //查询用户所有产品
        List<FinancialRecord> financialRecords = financialRecordMapper.selectList(new QueryWrapper<FinancialRecord>().lambda()
                .eq(FinancialRecord::getUid, uid).orderByAsc(FinancialRecord::getPurchaseTime));

        //锁定持有产品的数量
        BigDecimal remainAmount = borrowAmount.divide(initialPledgeRate);
        for(FinancialRecord financialRecord : financialRecords){
            BigDecimal holdAmount = financialRecord.getHoldAmount();
            BigDecimal lockAmount = financialRecord.getLockAmount();
            BigDecimal available = holdAmount.subtract(lockAmount);

            if(remainAmount.compareTo(available) <= 0){
                financialRecord.setLockAmount(lockAmount.add(remainAmount));
                financialRecordMapper.updateById(financialRecord);
                break;
            }else {
                remainAmount = remainAmount.subtract(available);
                financialRecord.setLockAmount(holdAmount);
                financialRecordMapper.updateById(financialRecord);
            }
        }

        //添加订单
        BorrowCoinOrder borrowCoinOrder = BorrowCoinOrder.builder()
                .uid(uid)
                .borrowCoin(CurrencyCoin.usdt.getName())
                .borrowCapital(borrowAmount)
                .waitRepayCapital(borrowAmount)
                .pledgeCoin(CurrencyCoin.usdt.getName())
                .pledgeAmount(borrowAmount.divide(initialPledgeRate))
                .currentPledgeRate(initialPledgeRate)
                .status(BorrowOrderStatus.INTEREST_ACCRUAL)
                .borrowTime(new Date())
                .createTime(new Date()).build();
        borrowCoinOrderMapper.insert(borrowCoinOrder);

        // 生成一笔订单记录(进行中)
        Order order = Order.builder()
                .uid(uid)
                .coin(CurrencyCoin.usdt)
                .relatedId(borrowCoinOrder.getId())
                .orderNo(AccountChangeType.borrow.getPrefix() + CommonFunction.generalSn(CommonFunction.generalId()))
                .amount(borrowAmount)
                .type(ChargeType.borrow)
                .status(ChargeStatus.created)
                .createTime(LocalDateTime.now())
                .build();
        orderService.saveOrder(order);
        //钱包增加余额
        accountBalanceService.increase(uid, ChargeType.recharge, CurrencyAdaptType.usdt_omni, borrowAmount, order.getOrderNo()
                , CurrencyLogDes.借币.name());



    }
}
