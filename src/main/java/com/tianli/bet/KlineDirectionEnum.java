package com.tianli.bet;

/**
 * k线走势
 */
public enum KlineDirectionEnum {
    rise, //上升
    fall, //下降
    flat; //平

    public KlineDirectionEnum oppose(){
        switch (this){
            case rise: return fall;
            case fall: return rise;
            case flat: return flat;
        }
        return null;
    }
}
