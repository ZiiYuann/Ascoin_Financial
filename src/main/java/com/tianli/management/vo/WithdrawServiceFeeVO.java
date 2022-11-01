package com.tianli.management.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-27
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawServiceFeeVO {

    private LocalDate createTime;

    private BigDecimal eth;

    private BigDecimal bnb;

    private BigDecimal trx;

    private BigDecimal ethUsdt;

    private BigDecimal bnbUsdt;

    private BigDecimal trxUsdt;

    private List<WithdrawServiceFeeVO> fees;

    public static WithdrawServiceFeeVO getDefault(LocalDate createTime) {
        WithdrawServiceFeeVO withdrawServiceFeeVO = new WithdrawServiceFeeVO();
        withdrawServiceFeeVO.setCreateTime(createTime);
        withdrawServiceFeeVO.setEth(BigDecimal.ZERO);
        withdrawServiceFeeVO.setBnb(BigDecimal.ZERO);
        withdrawServiceFeeVO.setTrx(BigDecimal.ZERO);
        withdrawServiceFeeVO.setEthUsdt(BigDecimal.ZERO);
        withdrawServiceFeeVO.setTrxUsdt(BigDecimal.ZERO);
        withdrawServiceFeeVO.setBnbUsdt(BigDecimal.ZERO);
        return withdrawServiceFeeVO;
    }
}
