package com.tianli.bet.mapper;

import com.tianli.bet.BetDividendsService;
import com.tianli.tool.ApplicationContextTool;

public enum BetTypeEnum {
    normal, steady;

    public BetDividendsService getExeService() {
        String name = this.name();
        char[] arr = name.toCharArray();
        arr[0] = Character.toUpperCase(arr[0]);
        String name_ = "bet" + new String(arr) + "Service";
        return ApplicationContextTool.getBean(name_, BetDividendsService.class);
    }
}
