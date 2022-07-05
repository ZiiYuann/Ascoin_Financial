package com.tianli.management.kyc;

import lombok.Data;

@Data
public class KycMgtPageReq {
    private String username;
    private String certificate_type;
    private String certificate_no;
    private String real_name;
    private Integer status;
    private int page = 1;
    private int size = 10;
}
