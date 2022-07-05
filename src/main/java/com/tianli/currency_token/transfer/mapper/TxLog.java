package com.tianli.currency_token.transfer.mapper;

import com.tianli.currency_token.mapper.ChainType;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.math.BigDecimal;

/**
 * <p>
 * 交易记录
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class TxLog {

    private Long id;

    private BigDecimal value;

    private String from_address;

    private String to_address;

    private Long block;

    private String tx;

    private LocalDateTime create_time;

    private ChainType chain;

    private CurrencyCoinEnum token;

    private String contract_address;
}
