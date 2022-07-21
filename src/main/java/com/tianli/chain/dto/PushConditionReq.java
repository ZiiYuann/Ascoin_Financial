package com.tianli.chain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-21
 **/
@Data
@Builder
public class PushConditionReq {

    /**
     * 回调地址
     */
    private String callbackAddress;


    private List<TxConditionReq> txConditionReqs;
}
