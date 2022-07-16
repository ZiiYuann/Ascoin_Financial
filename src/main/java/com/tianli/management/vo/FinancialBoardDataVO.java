package com.tianli.management.vo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-16
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialBoardDataVO {

    private BigDecimal amount;

    private LocalDateTime dateTime;

}
