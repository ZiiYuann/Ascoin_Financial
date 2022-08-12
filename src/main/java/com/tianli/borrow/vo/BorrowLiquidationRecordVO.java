package com.tianli.borrow.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data

@NoArgsConstructor
@Builder
@AllArgsConstructor
public class BorrowLiquidationRecordVO implements Serializable {

    private static final long serialVersionUID=1L;

    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime time;

}
