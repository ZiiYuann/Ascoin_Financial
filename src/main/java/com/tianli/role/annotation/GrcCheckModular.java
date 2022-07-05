package com.tianli.role.annotation;

public enum GrcCheckModular {
    验证码登录, 密码登录, 领取新人福利, 领取每日福利, 下注, 提现, 邀请绑定,
    ;

    public String desc(){
        return this.name();
    }
}
