package com.tianli.fund;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class FundCycleTest {

    public static void main(String[] args) {
        LocalDate date1 = LocalDate.of(2022, 9, 5);
        LocalDate date2 = LocalDate.of(2022, 9, 1);
        long until = date1.until(date2, ChronoUnit.DAYS);
        System.out.println(until);
    }
}
