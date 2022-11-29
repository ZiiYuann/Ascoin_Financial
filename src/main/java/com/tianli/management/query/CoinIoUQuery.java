package com.tianli.management.query;

import com.tianli.chain.enums.ChainType;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.common.query.IoUQuery;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


/**
 * @author chenb
 * @apiNote
 * @since 2022-11-22
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CoinIoUQuery extends IoUQuery {

    private Long id;

    @NotBlank(message = "币别名称不允许为空")
    private String name;

    private String contract;

    @NotBlank(message = "logo不允许为空")
    private String logo;

    @NotNull(message = "链不允许为null")
    private ChainType chain;

    @NotNull(message = "网络不允许为null")
    private NetworkType network;

    private int weight;

    private String rateUrl;

    private String rateField;
}
