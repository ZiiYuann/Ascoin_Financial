package com.tianli.accountred.enums;

import lombok.Getter;

/**
 * @author chenb
 * @apiNote
 * @since 2022-12-21
 **/
public enum RedEnvelopeChannel {
    //EXTERN：站外 CHAT：聊天
    EXTERN(30),
    CHAT(1);

    @Getter
    private int expireDays;

    RedEnvelopeChannel(int expireDays) {
        this.expireDays = expireDays;
    }
}
