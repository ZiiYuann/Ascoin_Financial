package com.tianli.management.query;

import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.common.blockchain.NetworkType;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-19
 **/
@Data
public class FinancialChargeQuery {

    private String txid;

    private String coin;

    private String uid;

    private NetworkType networkType;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private ChargeType chargeType;

    private ChargeStatus chargeStatus;

    private boolean noReview;

    private Byte reviewType;


}
