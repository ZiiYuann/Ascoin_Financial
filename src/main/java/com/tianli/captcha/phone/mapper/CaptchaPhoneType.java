package com.tianli.captcha.phone.mapper;

/**
 * @Author wangqiyun
 * @Date 2018/12/5 5:49 PM
 */
public enum CaptchaPhoneType {
    registration, //注册
    resetLoginPassword,//重置登录密码
    modificationLoginPassword,//更新登录密码
    resetPayPassword,//重置支付密码
    registrationAgent,//增加顶级代理商
    withdraw,//提现
    kyc,//kyc认证
    newIp//新ip登录
}
