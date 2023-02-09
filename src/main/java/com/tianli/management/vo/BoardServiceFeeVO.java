package com.tianli.management.vo;

import com.tianli.chain.enums.ChainType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
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
public class BoardServiceFeeVO {

    private LocalDate createTime;

    private BigDecimal amount;

    private BigDecimal rate;

    private String coin;

    private ChainType chainType;

    private List<BoardServiceFeeVO> fees;

    private List<BoardServiceFeeVO> summaryFees;

    public static BoardServiceFeeVO getDefault(LocalDate createTime) {
        BoardServiceFeeVO withdrawBoardServiceFeeVO = new BoardServiceFeeVO();
        withdrawBoardServiceFeeVO.setCreateTime(createTime);
        withdrawBoardServiceFeeVO.setAmount(BigDecimal.ZERO);
        return withdrawBoardServiceFeeVO;
    }

    public static HashMap<ChainType, BoardServiceFeeVO> getDefaultChainMap() {
        HashMap<ChainType, BoardServiceFeeVO> result = new HashMap<>();
        for (ChainType chainType : ChainType.values()) {
            BoardServiceFeeVO aDefault = getDefault(null);
            aDefault.setCoin(chainType.getMainToken());
            aDefault.setRate(BigDecimal.ONE);
            aDefault.setChainType(chainType);
            result.put(chainType, aDefault);
        }
        return result;
    }
}
