package com.tianli.chain.dto;

import lombok.Data;

import java.math.BigDecimal;
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

    private BigDecimal value;

    private String contractAddress;

    private DateReq date;

    private TimeReq time;

    public LocalDateTime getCreateTime() {
        int day = date.getDay();
        int month = date.getMonth();
        int year = date.getYear();
        int hour = time.getHour();
        int minute = time.getMinute();
        int second = time.getSecond();
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
