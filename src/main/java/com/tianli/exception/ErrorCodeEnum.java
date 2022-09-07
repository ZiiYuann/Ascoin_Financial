package com.tianli.exception;

import com.google.gson.Gson;
import com.tianli.tool.MapTool;

public enum ErrorCodeEnum {

    /*** －－－－－－基础错误码－－－－－－*/
    SUCCESS(0, "成功"),
    SYSTEM_ERROR(99, "系统错误"),
    UNLOIGN(101, "请登录"),
    ARGUEMENT_ERROR(102, "参数错误"),
    ACCESS_DENY(103, "无权限"),
    OBJECT_NOT_FOUND(104, "找不到对象"),
    TOO_FREQUENT(105, "操作过于频繁"),
    CODE_ERROR(106, "验证码输入不正确"),
    SYSTEM_BUSY(107, "系统繁忙"),
    TOKEN_ERROR(108, "token错误"),
    PASSWORD_ERROR(109, "账号或密码输入错误"),
    NETWORK_ERROR(110, "网络错误"),
    TYPE_ERROR(111, "文件类型错误"),
    NOT_OPEN(112, "服务未开放"),
    ORIGINAL_PASSWORD_ERROR(113, "原密码错误"),
    LOGIN_AUTHORIZATION_ERROR(114, "第三方登录授权错误"),
    ERROR_GETTING_THIRD_PARTY_ACCESS_TOKEN(115, "获取第三方Access_token错误"),
    ERROR_GETTING_THIRD_PARTY_USER_INFO(116, "获取第三方user_info错误"),
    ACCESS_TOKEN_VERIFICATION_FAILED(117, "Access_token校验失败"),
    BET_AMOUNT_TOO_MUCH(118, "押注金额过大"),
    BET_AMOUNT_TOO_SMALL(119, "押注金额过小"),
    SALESMAN_NOT_FOUND(120,"业务员不存在"),
    WRONG_SETTINGS(121,"不能设置自己为组长"),
    OPERATION_PASSWORD_ERROR(122, "操作密码错误"),
    INSUFFICIENT_BALANCE(123,"余额不足"),
    PURCHASE_AMOUNT_TO_SMALL(124,"申购金额低于系统限制"),


    /*** －－－－－－业务错误码-账号相关－－－－－－*/
    CONTRACT_ADDRESS_ERROR(220, "请输入正确的合约地址"),
    ADDRESS_ERROR(220, "地址错误"),
    USER_NOT_EXIST(2001, "用户不存在"),
    USER_EXIST(2002, "用户已经存在"),
    ACCOUNT_BAND(2003, "账号被禁用"),
    USER_PHONE_USED_REPEAT(2004, "手机号码已被重复"),
    CUSTOM_ERROR(2005, "用户自定义错误"),
    INVALID_REFERRAL_CODE(2006, "无效的邀请码"),
    INSUFFICIENT_USER_RIGHTS(2007, "用户权限不足"),
    MSG_NOT_SUPPORT(2008,"该地区短信不支持"),
    APPLIED_FOR_LOAN(2009,"已存在申请中的贷款"),
    REPAYMENT_FAILED(2010,"还款失败,合约账户余额不足"),
    SERVICE_NOT_AVAILABLE(2011,"未到还款时间"),
    NEW_CURRENCY_NAME_EXIT(2012,"币种名称重复"),
    NEW_CURRENCY_SHORT_NAME_EXIT(2013,"币种简称重复"),
    NO_OPPONENT_PLATE(2014,"没有交易对象"),
    NO_CREDIT_SCORE(2015,"账户信用评分不足"),
    ACCOUNT_NOT_ACTIVE(2016,"云钱包未激活"),
    CURRENCY_NOT_SUPPORT(2017,"币别尚未支持"),
    /*** －－－－－－业务错误码-余额相关－－－－－－*/
    CREDIT_LACK(2100, "额度不足"),
    WITHDRAWAL_AMOUNT_LT_FEE_ERROR(2101, "提现数额必须大于手续费"),
    FEE_LT_ZERO_ERROR(2102, "手续费小于0"),
    WITHDRAWAL_AMOUNT_LT_MIN_AMOUNT_ERROR(2103, "提现金额小于最小提现金额"),

    ACCOUNT_FREEZE(2104, "账号被冻结"),
    /*** －－－－－－业务错误码-团队相关－－－－－－*/
    EXIST_LOW_AGENT(2200, "存在下级代理商"),
    REPEAT_SET_AGENT(2201, "重复设置代理商"),
    FORBID_SET_AGENT(2202, "无法设置该用户为代理商"),
    EXIST_LOW_SALESMAN(2203, "存在下级组员"),

    /*** －－－－－－业务错误码-其他相关－－－－－－*/
    SECRET_ERROR(3000, "密钥错误"),
    NOT_BET_STEADY_SECTION_ERROR(3100, "非稳赚场开启区间"),
    TIME_CONFLICT(3103, "存在时间冲突"),
    KYC_TRIGGER_ERROR(3333, "需要KYC认证"),
    SIGN_ERROR(3104,"验签失败"),
    UPLOAD_CHAIN_ERROR(3105,"上链失败, 请稍后重试"),
    UPLOAD_DATACENTER_ERROR(3106,"链接数据中心失败, 请稍后重试"),
    PRODUCT_USER_HOLD(3107,"用户持有中，不可删除"),
    PRODUCT_CAN_NOT_BUY(3108,"产品暂时不可购买"),
    PRODUCT_CAN_NOT_EDIT(3108,"产品暂时不可修改"),

    /*** －－－－－－业务错误码-现货交易－－－－－－*/
    TRADE_FAIL(3400, "交易失败"),
    CANCEL_FAIL(3401, "撤销失败"),

    /*** －－－－－－业务错误码-借币相关－－－－－－*/
    CURRENCY_COIN_ERROR(3501,"币别错误"),
    BORROW_CONFIG_NO_EXIST(3502,"币种配置为空"),
    BORROW_CONFIG_EXIST(3503,"币种配置已存在"),
    BORROW_CONFIG_USED(3504,"币种配置已被使用"),
    BORROW_CONFIG_RATE_ERROR(3505,"币种配置比率错误"),
    BORROW_RANGE_ERROR(3506,"单笔借币数量小于最小数量或大于最大数量"),
    BORROW_GT_AVAILABLE_ERROR(3507,"借币数量大于可用数量"),
    BORROW_ORDER_NO_EXIST(3508,"借币订单不存在"),
    BORROW_ORDER_STATUS_ERROR(3509,"借币订单状态错误"),
    REPAY_GT_CAPITAL_ERROR(3510,"还款金额大于需要还款金额"),
    ADJUST_GT_AVAILABLE_ERROR(3511,"调整金额大于可用金额"),
    PLEDGE_RATE_RANGE_ERROR(3512,"质押率范围错误"),
    PLEDGE_LT_LIQUIDATION_ERROR(3513,"质押率小于平仓率"),
    NO_OPERATION(3514,"整点计息，请稍后进行操作"),

    /*** －－－－－－业务错误码-基金相关－－－－－－*/
    AGENT_ALREADY_BIND(3601,"用户已有代理信息"),
    AGENT_PRODUCT_NOT_EXIST(3602,"产品不存在"),
    AGENT_PRODUCT_ALREADY_BIND(3603,"产品已经绑定"),
    AGENT_NOT_EXIST(3604,"代理信息不存在"),
    PURCHASE_GT_PERSON_QUOTA(3605,"申购金额大于个人限额"),
    PURCHASE_GT_TOTAL_QUOTA(3606,"申购金额大于总限额"),
    REFERRAL_CODE_ERROR(3604,"推荐码错误"),
    FUND_NOT_EXIST(3606,"基金不存在"),
    REDEMPTION_GT_HOLD(3607,"赎回金额大于持有金额"),
    TRANSACTION_NOT_EXIST(3608,"交易记录不存在或状态错误"),
    INCOME_NOT_EXIST(3609,"收益记录不存在"),
    INCOME_STATUS_ERROR(3609,"收益记录状态错误"),
    PRODUCT_NOT_CLOSE(3610,"产品未下线"),
    EXIST_WAIT_REDEMPTION(3611,"存在待赎回金额"),
    EXIST_WAIT_INTEREST(3612,"存在待发放利息"),
    REDEMPTION_CYCLE_ERROR(3613,"赎回周期错误"),


        ;
    private int errorNo;

    private String errorMsg;


    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }


    public int getErrorNo() {
        return errorNo;
    }

    public void setErrorNo(int errorNo) {
        this.errorNo = errorNo;
    }

    ErrorCodeEnum(int errorNo, String errorMsg) {
        this.errorNo = errorNo;
        this.errorMsg = errorMsg;
    }

    ErrorCodeEnum() {
        this.errorNo = 100;
        this.errorMsg = "未知错误";
    }

    public ErrCodeException generalException(String msg) {
        String newMsg = String.format("[%s]:%s", this.errorMsg, msg);
        return new ErrCodeException(this.errorNo, newMsg);
    }

    public ErrCodeException generalException() {
        return new ErrCodeException(this.errorNo, this.errorMsg);
    }

    public ErrCodeException generalException(Throwable throwable) {
        return new ErrCodeException(this.errorNo, this.errorMsg, throwable);
    }

    public void throwExtendMsgException(String msg) {
        throw this.generalException(msg);
    }

    public void throwException() {
        throw this.generalException();
    }

    public void throwException(Throwable throwable) {
        throw this.generalException(throwable);
    }

    public static void throwException(String msg) {
        throw new ErrCodeException(110, msg);
    }

    public static void throwException(Integer code, String msg) {
        throw new ErrCodeException(code, msg);
    }

    public String toJson() {
        return new Gson().toJson(MapTool.Map().put("code", errorNo).put("msg", errorMsg).put("time", System.currentTimeMillis()));
    }
}