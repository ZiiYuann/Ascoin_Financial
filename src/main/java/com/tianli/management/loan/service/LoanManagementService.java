package com.tianli.management.loan.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.admin.AdminAndRoles;
import com.tianli.admin.AdminService;
import com.tianli.currency_token.CurrencyTokenService;
import com.tianli.loan.dao.LoanMapper;
import com.tianli.loan.entity.Loan;
import com.tianli.loan.enums.LoanStatusEnum;
import com.tianli.loan.service.ILoanAddressService;
import com.tianli.loan.service.ILoanCurrencyService;
import com.tianli.loan.service.ILoanService;
import com.tianli.management.loan.dto.LoanAuditDTO;
import com.tianli.management.loan.vo.LoanAuditDetailsVo;
import com.tianli.management.loan.vo.LoanListVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author lzy
 * @date 2022/5/26 15:41
 */
@Service
public class LoanManagementService {

    @Resource
    LoanMapper loanMapper;

    @Resource
    ILoanService loanService;

    @Resource
    AdminService adminService;

    @Resource
    ILoanCurrencyService loanCurrencyService;

    @Resource
    CurrencyTokenService currencyTokenService;

    @Resource
    ILoanAddressService loanAddressService;

    public IPage<LoanListVo> queryList(String username, String status, String reviewer, String startTime, String endTime, Integer page, Integer size) {
        Long count = loanMapper.count(username, status, reviewer, startTime, endTime);
        if (ObjectUtil.isNull(count) || count <= 0L) {
            return new Page<>();
        }
        List<LoanListVo> loanListVoList = loanMapper.queryList(username, status, reviewer, startTime, endTime, (page - 1) * size, size);
        return new Page<LoanListVo>(page, size).setTotal(count).setRecords(loanListVoList);
    }

    @Transactional(rollbackFor = Exception.class)
    public void audit(LoanAuditDTO loanAuditDTO) {
        Loan loan = loanService.getById(loanAuditDTO.getId());
        if (ObjectUtil.isNull(loan) || !loan.getStatus().equals(LoanStatusEnum.PENDING_REVIEW)) {
            return;
        }
        AdminAndRoles my = adminService.my();
        loan.setActual_amount(loanAuditDTO.getActual_amount());
        loan.setStatus(loanAuditDTO.getStatus());
        loan.setReason(loanAuditDTO.getReason());
        loan.setReason_en(loanAuditDTO.getReason_en());
        loan.setUpdate_time(LocalDateTime.now());
        loan.setReview_time(System.currentTimeMillis());
        loan.setReviewer_id(my.getId());
        loan.setReviewer(my.getUsername());
        //打钱
        if (loanAuditDTO.getStatus().equals(LoanStatusEnum.USING)) {
            loanCurrencyService.increase(loan);
            loan.setCurrent_day(1);
            loan.setNext_update_time(System.currentTimeMillis() + 86400000);
            loan.setInterest(loan.getActual_amount().multiply(loan.getRate()).multiply(new BigDecimal(loan.getCurrent_day())));
            setLoanAddress(loan.getUid());
        }
        loanService.updateById(loan);
    }

    private void setLoanAddress(Long uid) {
        loanAddressService.findByUid(uid);
    }

    public LoanAuditDetailsVo auditDetails(Long id) {
        Loan loan = loanService.getById(id);
        BigDecimal totalAmount = currencyTokenService.getCurrencyAllUsdt(loan.getUid());
        LoanAuditDetailsVo loanAuditDetailsVo = BeanUtil.copyProperties(loan, LoanAuditDetailsVo.class);
        loanAuditDetailsVo.setTotal_amount(totalAmount);
        loanAuditDetailsVo.setExpected_interest(loanAuditDetailsVo.getExpect_amount().multiply(loanAuditDetailsVo.getRate()).multiply(Convert.toBigDecimal(loanAuditDetailsVo.getRepayment_cycle())));
        loanAuditDetailsVo.setRepayment_required(loanAuditDetailsVo.getExpect_amount().add(loanAuditDetailsVo.getExpected_interest()));
        return loanAuditDetailsVo;
    }

    public BigDecimal total(String username, String status, String reviewer, String startTime, String endTime) {
        BigDecimal total = loanMapper.total(username, status, reviewer, startTime, endTime);
        return total;
    }
}
