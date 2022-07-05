package com.tianli.management.bul;

import lombok.Data;

@Data
public class BulMgtPageReq {
    private String username;
    private int page = 1;
    private int size = 10;
}
