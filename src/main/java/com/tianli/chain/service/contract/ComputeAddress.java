package com.tianli.chain.service.contract;

import com.tianli.chain.enums.ChainType;

import java.io.IOException;

/**
 * @Author cs
 * @Date 2022-12-26 17:45
 */
public interface ComputeAddress {
    boolean match(ChainType chain);
    String computeAddress(long uid) throws IOException;
}
