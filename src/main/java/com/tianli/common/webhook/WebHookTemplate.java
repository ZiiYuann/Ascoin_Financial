package com.tianli.common.webhook;


/**
 * @author chenb
 * @apiNote
 * @since 2022-09-27
 **/
public class WebHookTemplate {


    public static String FUND_PURCHASE =
            "【申购提醒】\n" +
                    "用户ID: #{uid}\n" +
                    "申购产品：#{productName}\n" +
                    "申购金额：#{amount} #{coin}\n" +
                    "申购时间：#{time}\n" +
                    "请及时查看哦！";

    public static String FUND_REDEEM =
            "【赎回提示】\n" +
                    "用户ID: #{uid}\n" +
                    "申购产品：#{productName}\n" +
                    "申购金额：#{amount} #{coin}\n" +
                    "申购时间：#{time}\n" +
                    "请及时查看哦！";

    public static String FUND_INCOME =
            "【收益发放提醒】\n" +
                    "用户ID: #{uid}\n" +
                    "持有数额：#{holdAmount} #{coin}\n" +
                    "利息数额：#{incomeAmount} #{coin}\n" +
                    "持有已满7天周期，到了利息发放时间。请前往后台为用户发放利息。";


    public static String FUND_EXAMINE =
            "你于#{time} 对用户ID#{uid}的赎回请求进行了审核，审核通过赎回金额：#{amount} #{coin}，" +
                    "赎回状态：成功。如不是本人操作，请及时前往后台修改密码。";

    public static String FUND_INCOME_PUSH =
            "你于#{time} 对收益进行了发放，发放利息：#{amount} #{coin}，" +
                    "发放状态：成功。";
}
