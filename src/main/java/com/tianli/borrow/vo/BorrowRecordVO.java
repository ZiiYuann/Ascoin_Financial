package com.tianli.borrow.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BorrowRecordVO {
    private Long borrowDuration;

    private List<Record> records;

    @Data
    public static class Record{

        private String record;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        private LocalDateTime time;

    }
}
