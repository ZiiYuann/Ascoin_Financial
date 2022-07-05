package com.tianli.common.blockchain;

import lombok.Builder;
import lombok.Data;
import org.tron.protos.Protocol;

/**
 * @Author cs
 * @Date 2022-01-07 6:49 下午
 */
@Data
@Builder
public class SignTransactionResult {
    private Protocol.Transaction txn;
    private String txid;
}
