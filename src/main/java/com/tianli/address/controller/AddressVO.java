package com.tianli.address.controller;

import com.tianli.address.mapper.Address;
import com.tianli.currency.CurrencyTypeEnum;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户充值地址表
 * </p>
 *
 * @author hd
 * @since 2020-12-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class AddressVO {

    /**
     * 地址类型
     */
    private CurrencyTypeEnum type;

    /**
     * 比特币地址btc / usdt-omni
     */
    private String btc;

    /**
     * 以太坊地址eth / usdt-erc20
     */
    private String eth;

    /**
     * 波场地址 trc20
     */
    private String tron;

    /**
     * 波场地址 trc20
     */
    private String bsc;

    public static AddressVO trans(Address address){
        return AddressVO.builder()
                .type(address.getType())
                .btc(address.getBtc())
                .eth(address.getEth())
                .bsc(address.getBsc())
                .tron(address.getTron()).build();
    }

}
