package com.tianli.borrow.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BorrowRecordVO implements Serializable {

    private static final long serialVersionUID=1L;

    private Long borrowDuration;

    private List<Record> records;

    @Data
    public static class Record{

        private String record;

        private String recordEn;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        private LocalDateTime time;

    }
}
