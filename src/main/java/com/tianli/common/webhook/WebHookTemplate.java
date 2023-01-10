package com.tianli.common.webhook;


import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * @author chenb
 * @apiNote
 * @since 2022-09-27
 **/
public class WebHookTemplate {

    public static final HashMap<String, String> COIN_ALIAS = new HashMap<>();

    static {
        COIN_ALIAS.put("usdt", "积分");
        COIN_ALIAS.put("eth", "积分e");
        COIN_ALIAS.put("bnb", "积分b");
        COIN_ALIAS.put("usdc", "积分uc");
    }

    public static String withdrawApply(double amount, String coin) {
        String msg = "监测到用户提现申请,请管理员尽快处理，金额：" +
                new BigDecimal(amount).toPlainString() +
                " " + COIN_ALIAS.getOrDefault(coin, coin) +
                "，时间：" +
                LocalDateTime.now();
        return msg;
    }

    public static String fundPurchase(Long uid, String productName, BigDecimal amount, String coin) {
        // 发送消息
        String fundPurchaseTemplate = WebHookTemplate.FUND_PURCHASE;

        String[] searchList = new String[5];
        searchList[0] = "#{uid}";
        searchList[1] = "#{productName}";
        searchList[2] = "#{amount}";
        searchList[3] = "#{coin}";
        searchList[4] = "#{time}";

        String[] replacementList = new String[5];
        replacementList[0] = uid + "";
        replacementList[1] = productName;
        replacementList[2] = amount.toPlainString();
        replacementList[3] = COIN_ALIAS.getOrDefault(coin, coin);
        replacementList[4] = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return StringUtils.replaceEach(fundPurchaseTemplate, searchList, replacementList);
    }

    public static String fundRedeem(Long uid, String productName, BigDecimal amount, String coin) {
        String fundPurchaseTemplate = WebHookTemplate.FUND_REDEEM;
        String[] searchList = new String[5];
        searchList[0] = "#{uid}";
        searchList[1] = "#{productName}";
        searchList[2] = "#{amount}";
        searchList[3] = "#{coin}";
        searchList[4] = "#{time}";
        String[] replacementList = new String[5];
        replacementList[0] = uid + "";
        replacementList[1] = productName;
        replacementList[2] = amount.toPlainString();
        replacementList[3] = COIN_ALIAS.getOrDefault(coin, coin);
        replacementList[4] = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return StringUtils.replaceEach(fundPurchaseTemplate, searchList, replacementList);
    }


    public static String fundIncome(Long uid, BigDecimal holdAmount, BigDecimal waitIncomeAmount, String coin) {
        String fundPurchaseTemplate = WebHookTemplate.FUND_INCOME;
        String[] searchList = new String[5];
        searchList[0] = "#{uid}";
        searchList[1] = "#{holdAmount}";
        searchList[2] = "#{incomeAmount}";
        searchList[3] = "#{coin}";
        searchList[4] = "#{time}";
        String[] replacementList = new String[5];
        replacementList[0] = uid + "";
        replacementList[1] = holdAmount.toPlainString();
        replacementList[2] = waitIncomeAmount.toPlainString();
        replacementList[3] = COIN_ALIAS.getOrDefault(coin, coin);
        replacementList[4] = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return StringUtils.replaceEach(fundPurchaseTemplate, searchList, replacementList);
    }

    public static String fundExamine(Long uid, BigDecimal amount, String coin) {
        // 发送消息
        String fundPurchaseTemplate = WebHookTemplate.FUND_EXAMINE;
        String[] searchList = new String[4];
        searchList[0] = "#{uid}";
        searchList[1] = "#{amount}";
        searchList[2] = "#{coin}";
        searchList[3] = "#{time}";
        String[] replacementList = new String[4];
        replacementList[0] = uid + "";
        replacementList[1] = amount.toPlainString();
        replacementList[2] = COIN_ALIAS.getOrDefault(coin, coin);
        replacementList[3] = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return StringUtils.replaceEach(fundPurchaseTemplate, searchList, replacementList);
    }

    public static String fundIncomePush(BigDecimal amount, String coin) {
        String fundPurchaseTemplate = WebHookTemplate.FUND_INCOME_PUSH;
        String[] searchList = new String[3];
        searchList[0] = "#{time}";
        searchList[1] = "#{amount}";
        searchList[2] = "#{coin}";
        String[] replacementList = new String[3];
        replacementList[0] = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        replacementList[1] = amount.toPlainString();
        replacementList[2] = COIN_ALIAS.getOrDefault(coin, coin);
        return StringUtils.replaceEach(fundPurchaseTemplate, searchList, replacementList);
    }

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
