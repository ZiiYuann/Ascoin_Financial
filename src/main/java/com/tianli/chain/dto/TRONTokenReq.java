package com.tianli.chain.dto;

import java.math.BigInteger;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-22
 **/
public class TRONTokenReq extends BaseTokenReq{


    private byte status;

    /**
     * 本次合约因Bandwidth不足消耗的TRX
     */
    private BigInteger netFee;

    /**
     * 本次合约消耗的Bandwidth(不包含NetFee对应的)
     */
    private BigInteger netUsage;

    /**
     * 本次合约调用者消耗TRX数量（SUN）
     */
    private BigInteger energyFee;

    /**
     * 本次合约调用者消耗的Energy数量
     */
    private BigInteger energyUsage;

    /**
     * 本次合约总共消耗的Energy数量
     */
    private BigInteger energyUsageTotal;

    /**
     * 本次合约创建者提供的Energy数量
     */
    private BigInteger originEnergyUsage;

}
