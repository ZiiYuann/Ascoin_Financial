package com.tianli.loan.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.currency_token.transfer.dto.TransferDTO;
import com.tianli.currency_token.transfer.mapper.TokenContract;
import com.tianli.loan.dto.ApplyLoanDTO;
import com.tianli.loan.entity.Loan;
import com.tianli.loan.entity.LoanAddress;
import com.tianli.loan.enums.LoanStatusEnum;
import com.tianli.loan.vo.LoanListVo;
import com.tianli.loan.vo.LoanQueryVo;
import com.tianli.loan.vo.RepaymentDetailsVo;
import com.tianli.loan.vo.RepaymentRecordVo;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author lzy
 * @since 2022-05-26
 */
public interface ILoanService extends IService<Loan> {
    /**
     * 申请贷款
     *
     * @param applyLoanDTO
     */
    void apply(ApplyLoanDTO applyLoanDTO);

    /**
     * 根据订单状态查询
     *
     * @param statusEnums
     * @return
     */
    List<Loan> findByStatus(Long uid, List<LoanStatusEnum> statusEnums);

    Loan findById(Long id, Long uid);

    /**
     * 查询用户最后一次申请的单子
     *
     * @param uid
     * @return
     */
    Loan queryLastLoan(Long uid);

    LoanQueryVo details(Long id);

    IPage<LoanListVo> queryList(String status, Integer page, Integer size);

    /**
     * 还款
     */
    void repayment();

    /**
     * 还款
     */
    void repayment(LoanAddress loanAddress, TransferDTO item, TokenContract usdtContract);

    /**
     * 贷款时间任务
     */
    void loanTime();

    /**
     * 还款页面
     *
     * @return
     */
    RepaymentDetailsVo repaymentDetails();

    /**
     * 还款记录
     * @param page
     * @param size
     * @return
     */
    IPage<RepaymentRecordVo> repaymentRecord(Integer page, Integer size);
}
