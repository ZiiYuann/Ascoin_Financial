package com.tianli.management.logs;

import lombok.Data;

@Data
public class LoginLogsPageReq {
    private String username;
    private String ip;
    private String equipment_type;
    private String equipment;
    private Boolean grc_result;
    private String startTime;
    private String endTime;
    private int page = 1;
    private int size = 10;
}
