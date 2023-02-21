package com.tianli.product.aborrow.vo;

import com.tianli.management.dto.AmountDto;
import com.tianli.product.aborrow.dto.PledgeRateDto;
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

    private List<AmountDto> coinRates;

    private List<HoldBorrowingVO> holdBorrowingVOS;

    private List<HoldPledgingVO> holdPledgingVOS;

    private PledgeRateDto pledgeRateDto;

    private BorrowRecordVO borrowRecordVO;
}
