package com.tianli.product.aborrow.dto;

import com.tianli.management.dto.AmountDto;
import com.tianli.product.aborrow.entity.BorrowRecordCoin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-09
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRecordSnapshotDTO {

    private List<AmountDto> coinRates;

    private List<BorrowRecordCoin> borrowRecordCoins;

    private List<BorrowRecordPledgeDto> borrowRecordPledgeDtos;

    private PledgeRateDto pledgeRateDto;
}
