package com.tianli.management.query;

import com.tianli.chain.enums.ChainType;
import com.tianli.common.query.PQuery;
import com.tianli.management.enums.HotWalletOperationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-18
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class HotWalletDetailedPQuery extends PQuery {

    private String hash;

    private String coin;

    private HotWalletOperationType type;

    private ChainType chain;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
