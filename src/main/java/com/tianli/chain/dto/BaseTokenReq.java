package com.tianli.chain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-22
 **/
@Data
public class BaseTokenReq {

    private Long id;

    private String to;

    private String from;

    private String hash;

    private String block;

    private BigInteger value;

    private String contractAddress;

    private CreateTimeReq createTime;

    public LocalDateTime getCreateTime() {
        int day = createTime.getDate().getDay();
        int month = createTime.getDate().getMonth();
        int year = createTime.getDate().getYear();
        int hour = createTime.getTime().getHour();
        int minute = createTime.getTime().getMinute();
        int second = createTime.getTime().getSecond();
        String dateStr = new StringBuilder()
                .append(year)
                .append("-").append(String.format("%02d", month))
                .append("-").append(String.format("%02d", day))
                .append(" ").append(String.format("%02d", hour))
                .append(":").append(String.format("%02d", minute))
                .append(":").append(String.format("%02d", second)).toString();
        return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
