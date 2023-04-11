package com.tianli.management.query;

import com.tianli.chain.enums.ChainType;
import com.tianli.management.enums.HotWalletOperationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-18
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotWalletDetailedPQuery {

    private String hash;

    private String coin;

    private HotWalletOperationType type;

    private ChainType chain;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
