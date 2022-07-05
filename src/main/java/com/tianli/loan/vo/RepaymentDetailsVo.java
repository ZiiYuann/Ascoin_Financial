package com.tianli.loan.vo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.tianli.currency_token.mapper.ChainType;
import com.tianli.loan.entity.Loan;
import com.tianli.loan.entity.LoanAddress;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lzy
 * @date 2022/6/6 15:44
 */
@Data
public class RepaymentDetailsVo {

    private List<RepaymentAddressVo> addressList;
    /**
     * 实际借款金额
     */
    private BigDecimal actual_amount;

    /**
     * 利息
     */
    private BigDecimal interest;

    /**
     * 滞纳金
     */
    private BigDecimal forfeit_penalty;

    /**
     * 还款金额
     */
    private BigDecimal repayment_amount;
    /**
     * 剩余应还金额
     */
    private BigDecimal remaining_amount_due;

    public static RepaymentDetailsVo convert(Loan loan, LoanAddress loanAddress) {
        RepaymentDetailsVo repaymentDetailsVo = BeanUtil.copyProperties(loan, RepaymentDetailsVo.class);
        BigDecimal repayment_amount = ObjectUtil.isNull(loan.getRepayment_amount()) ? BigDecimal.ZERO : loan.getRepayment_amount();
        repaymentDetailsVo.setRemaining_amount_due((loan.getActual_amount().add(loan.getInterest()).add(loan.getForfeit_penalty())).subtract(repayment_amount));
        List<RepaymentAddressVo> addressList = new ArrayList<>();
        addressList.add(RepaymentAddressVo.convert(ChainType.bep20, loanAddress.getBsc()));
        addressList.add(RepaymentAddressVo.convert(ChainType.erc20, loanAddress.getEth()));
        addressList.add(RepaymentAddressVo.convert(ChainType.trc20, loanAddress.getTron()));
        repaymentDetailsVo.setAddressList(addressList);
        return repaymentDetailsVo;
    }
}
