package com.tianli.loan.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.CommonFunction;
import com.tianli.common.init.RequestInitService;
import com.tianli.currency.CurrencyService;
import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.currency.mapper.Currency;
import com.tianli.currency_token.transfer.dto.TransferDTO;
import com.tianli.currency_token.transfer.mapper.TokenContract;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.loan.dao.LoanMapper;
import com.tianli.loan.dto.ApplyLoanDTO;
import com.tianli.loan.entity.Loan;
import com.tianli.loan.entity.LoanAddress;
import com.tianli.loan.entity.LoanCycle;
import com.tianli.loan.entity.LoanRepaymentRecord;
import com.tianli.loan.enums.LoanStatusEnum;
import com.tianli.loan.service.ILoanAddressService;
import com.tianli.loan.service.ILoanCycleService;
import com.tianli.loan.service.ILoanRepaymentRecordService;
import com.tianli.loan.service.ILoanService;
import com.tianli.loan.vo.LoanListVo;
import com.tianli.loan.vo.LoanQueryVo;
import com.tianli.loan.vo.RepaymentDetailsVo;
import com.tianli.loan.vo.RepaymentRecordVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author lzy
 * @since 2022-05-26
 */
@Service
public class LoanServiceImpl extends ServiceImpl<LoanMapper, Loan> implements ILoanService {

    @Resource
    RequestInitService requestInitService;

    @Resource
    ILoanCycleService loanCycleService;

    @Resource
    CurrencyService currencyService;

    @Resource
    ILoanAddressService loanAddressService;

    @Resource
    ILoanRepaymentRecordService loanRepaymentRecordService;

    @Override
    public void apply(ApplyLoanDTO applyLoanDTO) {
        List<Loan> byStatus = this.findByStatus(requestInitService.uid(), ListUtil.of(LoanStatusEnum.PENDING_REVIEW, LoanStatusEnum.USING, LoanStatusEnum.BE_EXPIRED, LoanStatusEnum.PAST_DUE));
        if (CollUtil.isNotEmpty(byStatus)) {
            throw ErrorCodeEnum.APPLIED_FOR_LOAN.generalException();
        }
        LoanCycle loanCycle = loanCycleService.getById(applyLoanDTO.getLoan_cycle_id());
        if (ObjectUtil.isNull(loanCycle)) {
            throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
        }
        Loan loan = applyLoanDTO.getLoan(requestInitService, loanCycle);
        this.save(loan);
    }


    @Override
    public List<Loan> findByStatus(Long uid, List<LoanStatusEnum> statusEnums) {
        return this.list(Wrappers.lambdaQuery(Loan.class)
                .eq(Loan::getUid, uid)
                .in(Loan::getStatus, statusEnums));
    }

    @Override
    public Loan findById(Long id, Long uid) {
        return this.getOne(Wrappers.lambdaQuery(Loan.class).eq(Loan::getId, id).eq(Loan::getUid, uid));
    }

    @Override
    public Loan queryLastLoan(Long uid) {
        return this.getOne(Wrappers.lambdaQuery(Loan.class)
                .eq(Loan::getUid, uid)
                .orderByDesc(Loan::getCreate_time_ms)
                .last("limit 1"));
    }


    @Override
    public LoanQueryVo details(Long id) {
        Long uid = requestInitService.uid();
        //如果没有指定单 则查询最后一笔单子
        Loan loan = ObjectUtil.isNotNull(id) ? this.findById(id, uid) : this.queryLastLoan(uid);
        if (ObjectUtil.isNull(loan) || loan.getStatus().equals(LoanStatusEnum.REPAID)) {
            return null;
        }
        return LoanQueryVo.convert(loan);
    }

    @Override
    public IPage<LoanListVo> queryList(String status, Integer page, Integer size) {
        Long uid = requestInitService.uid();
        Page<Loan> loanPage = this.page(new Page<>(page, size), Wrappers.lambdaQuery(Loan.class)
                .eq(Loan::getUid, uid)
                .orderByDesc(Loan::getId)
                .eq(StrUtil.isNotBlank(status), Loan::getStatus, status));
        List<Loan> records = loanPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return new Page<>();
        }
        List<LoanListVo> loanListVoList = records.stream().map(LoanListVo::convert).collect(Collectors.toList());
        return new Page<LoanListVo>(page, size)
                .setTotal(loanPage.getTotal())
                .setRecords(loanListVoList)
                .setPages(loanPage.getPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void repayment() {
        Long uid = requestInitService.uid();
        Loan loan = this.queryLastLoan(uid);
        if (!ListUtil.of(LoanStatusEnum.USING, LoanStatusEnum.BE_EXPIRED, LoanStatusEnum.PAST_DUE).contains(loan.getStatus())) {
            return;
        }
        BigDecimal repaymentAmount = loan.getActual_amount().add(loan.getInterest()).add(loan.getForfeit_penalty());
        Currency currency = currencyService.get(uid, CurrencyTypeEnum.normal);
        BigDecimal remain = TokenCurrencyType.usdt_omni._money(currency.getRemain());
        if (remain.compareTo(repaymentAmount) < 0) {
            throw ErrorCodeEnum.REPAYMENT_FAILED.generalException();
        }
        currencyService.withdrawPresumptuous(uid, CurrencyTypeEnum.normal, TokenCurrencyType.usdt_omni.amount(repaymentAmount), loan.getId().toString(), CurrencyLogDes.还款.name());
        loan.setStatus(LoanStatusEnum.REPAID);
        loan.setRepayment_time_ms(requestInitService.now_ms());
        loan.setRepayment_amount(repaymentAmount);
        boolean update = this.updateById(loan);
        if (!update) {
            throw ErrorCodeEnum.SYSTEM_BUSY.generalException();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void repayment(LoanAddress loanAddress, TransferDTO item, TokenContract usdtContract) {
        BigDecimal money = usdtContract.money(item.getValue());
        Loan loan = this.queryLastLoan(loanAddress.getUid());
        BigDecimal actualAmount = loan.getActual_amount();
        BigDecimal interest = loan.getInterest();
        BigDecimal forfeitPenalty = loan.getForfeit_penalty();
        BigDecimal repaymentPrincipal = loan.getRepayment_principal();
        BigDecimal repaymentInterest = loan.getRepayment_interest();
        BigDecimal repaymentForfeitPenalty = loan.getRepayment_forfeit_penalty();
        BigDecimal balance = money;
        //偿还的滞纳金金额
        BigDecimal paid_forfeit_penalty = getPaidForfeitPenalty(forfeitPenalty, repaymentForfeitPenalty, balance);
        balance = balance.subtract(paid_forfeit_penalty);
        //偿还的利息金额
        BigDecimal paid_interest = getPaidInterest(interest, repaymentInterest, balance);
        balance = balance.subtract(paid_interest);
        //偿还的本金金额
        BigDecimal paid_principal = getPaidPrincipal(actualAmount, repaymentPrincipal, balance);
        balance = balance.subtract(paid_principal);
        //如果还有剩下的钱 全部加到还款本金上
        if (balance.compareTo(BigDecimal.ZERO) > 0) {
            paid_principal = paid_principal.add(balance);
        }
        loan.setRepayment_amount(loan.getRepayment_amount().add(money));
        loan.setRepayment_principal(loan.getRepayment_principal().add(paid_principal));
        loan.setRepayment_interest(loan.getRepayment_interest().add(paid_interest));
        loan.setRepayment_forfeit_penalty(loan.getRepayment_forfeit_penalty().add(paid_forfeit_penalty));
        loan.setUpdate_time(LocalDateTime.now());
        if (loan.getRepayment_amount().compareTo(actualAmount.add(interest).add(forfeitPenalty)) >= 0) {
            loan.setStatus(LoanStatusEnum.REPAID);
            loan.setRepayment_time_ms(System.currentTimeMillis());
        }
        LoanRepaymentRecord repaymentRecord = LoanRepaymentRecord.builder()
                .id(CommonFunction.generalId())
                .uid(loan.getUid())
                .loan_id(loan.getId())
                .amount(money)
                .from(item.getFrom())
                .to(item.getTo())
                .hash(item.getHash())
                .create_time(LocalDateTime.now())
                .tr_time(item.getCreateTime())
                .token(usdtContract.getToken().name())
                .chain_type(usdtContract.getChain().name())
                .paid_principal(paid_principal)
                .paid_interest(paid_interest)
                .paid_forfeit_penalty(paid_forfeit_penalty)
                .remaining_principal(actualAmount.subtract(loan.getRepayment_principal()))
                .remaining_interest(interest.subtract(loan.getRepayment_interest()))
                .remaining_forfeit_penalty(forfeitPenalty.subtract(loan.getRepayment_forfeit_penalty()))
                .build();
        loanRepaymentRecordService.save(repaymentRecord);
        this.updateById(loan);
    }

    private BigDecimal getPaidPrincipal(BigDecimal principal, BigDecimal repaymentPrincipal, BigDecimal balance) {
        return getPaidAmount(principal, repaymentPrincipal, balance);
    }

    private BigDecimal getPaidInterest(BigDecimal interest, BigDecimal repaymentInterest, BigDecimal balance) {
        return getPaidAmount(interest, repaymentInterest, balance);
    }

    private BigDecimal getPaidForfeitPenalty(BigDecimal forfeitPenalty, BigDecimal repaymentForfeitPenalty, BigDecimal balance) {
        return getPaidAmount(forfeitPenalty, repaymentForfeitPenalty, balance);
    }

    private BigDecimal getPaidAmount(BigDecimal amountDue, BigDecimal repaymentAmount, BigDecimal balance) {
        BigDecimal paid_interest = BigDecimal.ZERO;
        if (amountDue.compareTo(repaymentAmount) > 0) {
            if (balance.compareTo(amountDue.subtract(repaymentAmount)) <= 0) {
                paid_interest = balance;
            } else {
                paid_interest = amountDue.subtract(repaymentAmount);
            }
        }
        return paid_interest;
    }


    @Override
    public void loanTime() {
        List<Loan> loanList = this.list(Wrappers.lambdaQuery(Loan.class)
                .in(Loan::getStatus, ListUtil.of(LoanStatusEnum.USING, LoanStatusEnum.BE_EXPIRED, LoanStatusEnum.PAST_DUE))
                .le(Loan::getNext_update_time, System.currentTimeMillis()));
        if (CollUtil.isEmpty(loanList)) {
            return;
        }
        for (Loan loan : loanList) {
            loan.setCurrent_day(loan.getCurrent_day() + 1);
            loan.setNext_update_time(loan.getNext_update_time() + 86400000);
            loan.setInterest(loan.getActual_amount().multiply(loan.getRate()).multiply(Convert.toBigDecimal(loan.getCurrent_day())));
            loan.setUpdate_time(LocalDateTime.now());
            if (ObjectUtil.equal(loan.getCurrent_day(), loan.getRepayment_cycle())) {
                loan.setStatus(LoanStatusEnum.BE_EXPIRED);
            } else if (loan.getCurrent_day() > loan.getRepayment_cycle() && !ObjectUtil.equal(LoanStatusEnum.PAST_DUE, loan.getStatus())) {
                loan.setStatus(LoanStatusEnum.PAST_DUE);
            }
            if (loan.getCurrent_day() > loan.getRepayment_cycle() && ObjectUtil.isNotNull(loan.getLate_fee_rate())) {
                loan.setForfeit_penalty(loan.getActual_amount().multiply(loan.getLate_fee_rate()).multiply(Convert.toBigDecimal(loan.getCurrent_day() - loan.getRepayment_cycle())));
            }
            this.update(loan, Wrappers.lambdaUpdate(Loan.class)
                    .eq(Loan::getId, loan.getId())
                    .ne(Loan::getStatus, LoanStatusEnum.REPAID));
        }
    }

    @Override
    public RepaymentDetailsVo repaymentDetails() {
        Long uid = requestInitService.uid();
        Loan loan = this.queryLastLoan(uid);
        LoanAddress loanAddress = loanAddressService.findByUid(uid);
        return RepaymentDetailsVo.convert(loan, loanAddress);
    }

    @Override
    public IPage<RepaymentRecordVo> repaymentRecord(Integer page, Integer size) {
        Long uid = requestInitService.uid();
        Page<LoanRepaymentRecord> recordPage = loanRepaymentRecordService.page(new Page<>(page, size), Wrappers.lambdaQuery(LoanRepaymentRecord.class)
                .eq(LoanRepaymentRecord::getUid, uid)
                .orderByDesc(LoanRepaymentRecord::getCreate_time));
        List<LoanRepaymentRecord> records = recordPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return new Page<>();
        }
        List<RepaymentRecordVo> repaymentRecordVos = BeanUtil.copyToList(records, RepaymentRecordVo.class);
        return new Page<RepaymentRecordVo>(page, size)
                .setTotal(recordPage.getTotal())
                .setRecords(repaymentRecordVos)
                .setPages(recordPage.getPages());
    }


}
