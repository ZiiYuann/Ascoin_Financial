package com.tianli.account.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author:yangkang
 * @create: 2023-03-13 18:41
 * @Description: 云钱包流水记录新查询参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDetailsNewQuery implements Serializable {

    private String coin;

    private List<String> chargeType;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private String sepicalType;

    private String logType;

}
