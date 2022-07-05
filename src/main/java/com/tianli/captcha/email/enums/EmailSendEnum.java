package com.tianli.captcha.email.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author lzy
 * @date 2022/5/6 14:28
 */
@Getter
@AllArgsConstructor
public enum EmailSendEnum {


    IP_WARN("ipWarnService",
            "【Financial】Login Attempted from new IP address  {}  {}(UTC)",
            "Did You Login From a New Device or Loaction\n\n" + "We noticed your BF.Space account {}  was accessed from a new IP address.\n\n" +
                    "When:{}(UTC)\n" + "IP Address :{}\n\n" +
                    "Don’t recognize this activity? Please reset your password and contact customer support immediately. \n\n" +
                    "This is an automated message, please do not reply.",
            "ip不一致提醒邮件"),
    WITHDRAWAL_FAILED("emailWithdrawalFailedService",
            "【Financial】Withdrawal Failed  {}(UTC)",
            "Withdrawal Failed\n" +
                    "You’ve failed withdrawn {} WAVES from your account. \n\n" +
                    "Reason : {}\n\n" +
                    "Don’t recognize this activity? Please reset your password and contact customer support immediately. \n\n" +
                    "This is an automated message, please do not reply.",
            "提款失败提醒邮件"),
    WITHDRAWAL_SUCCESS("emailWithdrawalSuccessService",
            "【Financial】Withdrawal Successful + {}(UTC)",
            "Withdrawal Successful\n" +
                    "You’ve successfully withdrawn {} WAVES from your account. \n\n" +
                    "Withdrawal Address : \n" +
                    "{}\n" +
                    "Transaction ID : \n" +
                    "{}\n\n" +
                    "Don’t recognize this activity? Please reset your password and contact customer support immediately. \n\n" +
                    "This is an automated message, please do not reply.",
            "提现成功提醒邮件");

    private String serviceName;

    private String msgTitle;

    private String msgContext;

    private String desc;
}
