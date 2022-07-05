package com.tianli.currency.mapper;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.currency.log.CurrencyLogType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.time.LocalDateTime;


@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class DiscountCurrencyLog extends Model<DiscountCurrencyLog> {

    private static final long serialVersionUID = -3594138211014049362L;

    /**
     * 主键
     */
    private Long id;
    private LocalDateTime create_time;
    private Long uid;
    private CurrencyLogType type;
    private CurrencyTokenEnum token;
    private BigInteger amount;
    private BigInteger to_balance;
    private Long relate_id;
    private String node;
}
