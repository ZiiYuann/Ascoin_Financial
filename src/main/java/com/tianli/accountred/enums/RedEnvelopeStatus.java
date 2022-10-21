package com.tianli.accountred.enums;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-19
 **/
public enum RedEnvelopeStatus {
    // WAIT:等待发送（上链前的状态） PROCESS:发送中 fAIL:发送失败(上链失败) FINISH:已经完成（领取完毕） OVERDUE:过期（未领取完，时间到了）
    WAIT,
    PROCESS,
    FAIL,
    FINISH,
    OVERDUE,

    // RECEIVED:已经领取 SUCCESS:领取成功
    RECEIVED,
    SUCCESS,

}
