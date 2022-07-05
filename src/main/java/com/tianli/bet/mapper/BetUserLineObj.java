package com.tianli.bet.mapper;

import com.google.common.collect.Lists;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 押注表
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class BetUserLineObj {

    private static final long serialVersionUID = -5519581448378660715L;

    private LinkedList<UserLineStat> oneMin = new LinkedList<>();
    private LinkedList<UserLineStat> fiveMin = new LinkedList<>();
    private LinkedList<UserLineStat> fifteenMin = new LinkedList<>();
    private LinkedList<UserLineStat> thirtyMin = new LinkedList<>();
    private LinkedList<UserLineStat> sixtyMin = new LinkedList<>();
    private LinkedList<UserLineStat> oneDay = new LinkedList<>();

    public static BetUserLineObj build() {
        return new BetUserLineObj();
    }

    public void update(double final_start_exchange_rate, LocalDateTime expectedResultTime) {
        int year = expectedResultTime.getYear();
        Month month = expectedResultTime.getMonth();
        int dayOfMonth = expectedResultTime.getDayOfMonth();
        int hour = expectedResultTime.getHour();
        int minute = expectedResultTime.getMinute();
        long oneMinStartTime = LocalDateTime.of(year, month, dayOfMonth, hour, minute, 0).atZone(ZoneId.of("+8")).toInstant().getEpochSecond();
        doCompare(final_start_exchange_rate, oneMinStartTime, oneMin);
        int five = minute - minute % 5;
        long fiveMinStartTime = LocalDateTime.of(year, month, dayOfMonth, hour, five, 0).atZone(ZoneId.of("+8")).toInstant().getEpochSecond();
        doCompare(final_start_exchange_rate, fiveMinStartTime, fiveMin);
        int fifteen = minute - minute % 15;
        long fifteenMinStartTime = LocalDateTime.of(year, month, dayOfMonth, hour, fifteen, 0).atZone(ZoneId.of("+8")).toInstant().getEpochSecond();
        doCompare(final_start_exchange_rate, fifteenMinStartTime, fifteenMin);
        int thirty = minute - minute % 30;
        long thirtyMinStartTime = LocalDateTime.of(year, month, dayOfMonth, hour, thirty, 0).atZone(ZoneId.of("+8")).toInstant().getEpochSecond();
        doCompare(final_start_exchange_rate, thirtyMinStartTime, thirtyMin);
        long oneHourStartTime = LocalDateTime.of(year, month, dayOfMonth, hour, 0, 0).atZone(ZoneId.of("+8")).toInstant().getEpochSecond();
        doCompare(final_start_exchange_rate, oneHourStartTime, sixtyMin);
        long oneDayStartTime = LocalDateTime.of(year, month, dayOfMonth, 0, 0, 0).atZone(ZoneId.of("+8")).toInstant().getEpochSecond();
        doCompare(final_start_exchange_rate, oneDayStartTime, oneDay);
    }

    private void doCompare(double final_start_exchange_rate, long fiveMinStartTime, LinkedList<UserLineStat> sheet) {
        UserLineStat userLineStat2 = sheet.stream().filter(e -> Objects.equals(e.getId(), fiveMinStartTime)).findFirst().orElse(null);
        if (Objects.nonNull(userLineStat2)) {
            userLineStat2.update(final_start_exchange_rate);
        } else {
            userLineStat2 = UserLineStat.builder()
                    .id(fiveMinStartTime)
                    .max(final_start_exchange_rate)
                    .min(final_start_exchange_rate)
                    .build();
            sheet.addLast(userLineStat2);
        }
        if(sheet.size() >= 300){
            sheet.removeFirst();
        }
    }

    @Data
    @Builder
    static class UserLineStat {
        private Long id;
        private Double min;
        private Double max;

        public void update(double d) {
            if (min > d) {
                min = d;
            }
            if (max < d) {
                max = d;
            }
        }
    }

    public List<UserLineStat> getUserLine(String sheed){
        switch (sheed){
            case "oneMin": return oneMin;
            case "fiveMin": return fiveMin;
            case "fifteenMin": return fifteenMin;
            case "thirtyMin": return thirtyMin;
            case "sixtyMin": return sixtyMin;
            case "oneDay": return oneDay;
        }
        return Lists.newArrayList();
    }
}
