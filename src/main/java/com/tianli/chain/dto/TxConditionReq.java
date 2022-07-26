package com.tianli.chain.dto;

import com.tianli.chain.enums.ChainType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-21
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TxConditionReq{

    private String from;

    private String to;

    /**
     * 合约地址
     */
    private String contractAddress;

    private ChainType chain;


}
