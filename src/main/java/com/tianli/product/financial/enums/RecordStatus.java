package com.tianli.product.financial.enums;

import lombok.Getter;

@Getter
public enum RecordStatus {

    PROCESS("进行中"),
    SUCCESS("完成"),;

    RecordStatus(String desc) {
        this.desc = desc;
    }

    private final String desc;
}
