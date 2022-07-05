package com.tianli.common.blockchain;

import lombok.Builder;
import lombok.Data;
import org.web3j.protocol.core.methods.response.Log;

import java.math.BigInteger;
import java.util.List;

@Data
@Builder
public class TransactionResult {
    //  true:成功  false:失败  null:未知
    private Boolean success;
    // 实际的手续费
    private BigInteger fee;
    // log日志
    private List<Log> logs;
}
