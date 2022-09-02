package com.tianli.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-09-02
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionMsg {

    private Long id;

    private String msg;

    private LocalDateTime createTime;
}
