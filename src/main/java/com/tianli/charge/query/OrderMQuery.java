package com.tianli.charge.query;

import com.tianli.charge.enums.ChargeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-12-06
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderMQuery {

    private Long uid;

    private ChargeType type;

    private List<Long> uids;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private List<String> chargeTypes;

}
