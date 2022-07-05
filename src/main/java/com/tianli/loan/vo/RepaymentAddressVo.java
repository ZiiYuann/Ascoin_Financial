package com.tianli.loan.vo;

import com.tianli.currency_token.dto.ChainInfoDTO;
import com.tianli.currency_token.mapper.ChainType;
import lombok.Builder;
import lombok.Data;

/**
 * @author lzy
 * @date 2022/6/7 09:46
 */
@Data
@Builder
public class RepaymentAddressVo {

    private ChainType chain;

    private String address;

    private Object chainInfo;


    public static RepaymentAddressVo convert(ChainType chain, String address) {
        return RepaymentAddressVo.builder()
                .chain(chain)
                .address(address)
                .chainInfo(ChainInfoDTO.chainInfos.get(chain.name()))
                .build();
    }
}
