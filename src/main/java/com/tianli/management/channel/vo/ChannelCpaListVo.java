package com.tianli.management.channel.vo;

import lombok.Builder;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.time.LocalDateTime;

/**
 * @author lzy
 * @date 2022/5/7 16:44
 */
@Builder
@Data
public class ChannelCpaListVo {

    @Excel(name = "编号", orderNum = "0")
    private Long id;

    @Excel(name = "邮箱", orderNum = "1")
    private String username;

    @Excel(name = "注册时间", orderNum = "2", databaseFormat = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime register_time;

    @Excel(name = "渠道名", orderNum = "3")
    private String channel_name;

    @Excel(name = "kyc状态", orderNum = "4")
    private Integer kyc_status;

    @Excel(name = "认证时间", orderNum = "5", databaseFormat = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime kyc_certification_time;
}
