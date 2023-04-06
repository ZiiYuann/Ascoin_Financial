package com.tianli.charge.enums;

public enum ChargeStatus {
    //        created 新创建/待审核 review_fail 审核失败 chaining  chain_fail  chain_success
    // 申购                                             申购中     申购失败      申购成功（已完成）
    // 赎回                                             赎回中     赎回失败      赎回成功（已完成）
    // 转存                                             转存中     转存失败      转存成功（已完成）
    // 充值                                                       充值成功      充值失败
    // 提币      待审核             审核成功                         提币成功      提币失败

    created, // 新创建/待审核
    review_fail, // 审核失败
    chaining, // 进行中
    chain_fail, // 失败
    chain_success; // 成功


}
