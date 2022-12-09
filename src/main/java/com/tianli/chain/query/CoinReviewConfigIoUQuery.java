package com.tianli.chain.query;

import com.tianli.common.query.IoUQuery;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;


/**
 * @author chenb
 * @apiNote
 * @since 2022-12-08
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoinReviewConfigIoUQuery extends IoUQuery {

    /**
     * 系统自动审核，自动打币
     */
    @Min(value = 10)
    private int autoReviewAutoTransfer;

    /**
     * 人工审核，人工打币
     */
    @Min(value = 5000)
    private int manualReviewManualTransfer;

    /**
     * 时间限制
     */
    private int hourLimit;

    /**
     * 次数限制
     */
    private int timesLimit;

}

