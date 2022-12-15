package com.tianli.management.vo;

import com.tianli.chain.enums.ChainType;
import com.tianli.common.blockchain.NetworkType;
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
public class ServiceFeeVO {

    private LocalDate createTime;

    private BigDecimal amount;

    private BigDecimal rate;

    private String coin;

    private ChainType chainType;

    private List<ServiceFeeVO> fees;

    private List<ServiceFeeVO> summaryFees;

    public static ServiceFeeVO getDefault(LocalDate createTime) {
        ServiceFeeVO withdrawServiceFeeVO = new ServiceFeeVO();
        withdrawServiceFeeVO.setCreateTime(createTime);
        withdrawServiceFeeVO.setAmount(BigDecimal.ZERO);
        return withdrawServiceFeeVO;
    }
}
