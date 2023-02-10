package com.tianli.product.aborrow.vo;

import com.tianli.product.aborrow.dto.PledgeRateDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-10
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalPledgeVO {

    private PledgeRateDto pledgeRateDto;
}
