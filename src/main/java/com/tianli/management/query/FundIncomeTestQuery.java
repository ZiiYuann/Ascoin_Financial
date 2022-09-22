package com.tianli.management.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-09-20
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FundIncomeTestQuery {

    private Long uid;

    private Long recordId;

    private LocalDateTime now;
}
