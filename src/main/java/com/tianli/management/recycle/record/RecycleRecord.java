package com.tianli.management.recycle.record;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tianli.currency_token.mapper.ChainType;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author lzy
 * @date 2022/4/26 15:34
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("recycle_record")
public class RecycleRecord extends Model<RecycleRecord> {

    private Long id;

    private String tx_hash;

    private CurrencyCoinEnum token;

    private ChainType chain_type;

    private LocalDateTime create_time;

    private Boolean process;

    private Boolean main_currency;
}
