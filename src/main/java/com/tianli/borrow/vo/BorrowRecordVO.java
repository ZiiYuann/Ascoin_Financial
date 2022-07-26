package com.tianli.borrow.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class BorrowRecordVO {
    private Long borrowDuration;

    private List<Record> records;

    @Data
    public static class Record{

        private String record;

        private Date time;

    }
}