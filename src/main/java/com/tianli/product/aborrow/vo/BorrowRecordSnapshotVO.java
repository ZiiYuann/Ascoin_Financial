package com.tianli.product.aborrow.vo;

import com.tianli.product.aborrow.dto.PledgeRateDto;
import com.tianli.product.vo.RateVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-20
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRecordSnapshotVO {

    private List<RateVo> coinRates;

    private List<HoldBorrowingVO> holdBorrowingVOS;

    private List<HoldPledgingVO> holdPledgingVOS;

    private PledgeRateDto pledgeRateDto;

    private BorrowRecordVO borrowRecordVO;
}
