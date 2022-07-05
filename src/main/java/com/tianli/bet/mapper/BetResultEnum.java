package com.tianli.bet.mapper;

import com.tianli.bet.KlineDirectionEnum;

import java.util.Objects;

public enum BetResultEnum {
    win, lose, wait;

    public static BetResultEnum result(KlineDirectionEnum finalDirection, KlineDirectionEnum expectDirection){
        if(Objects.equals(expectDirection, finalDirection)
                || (Objects.equals(KlineDirectionEnum.flat, finalDirection) && Objects.equals(KlineDirectionEnum.rise, expectDirection))){
            return win;
        }
        return lose;
    }
}
