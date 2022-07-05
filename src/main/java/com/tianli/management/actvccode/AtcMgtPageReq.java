package com.tianli.management.actvccode;

import lombok.Data;

@Data
public class AtcMgtPageReq {
    private String username;
    private String activation_code;
    private Integer status;
    private int page = 1;
    private int size = 10;
}
