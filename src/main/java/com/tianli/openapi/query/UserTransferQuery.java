package com.tianli.openapi.query;

import com.google.common.base.MoreObjects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-06
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTransferQuery {

    private Long transferUid;

    private Long receiveUid;

    private String coin;

    private BigDecimal amount;

    private Long relatedId;
}
