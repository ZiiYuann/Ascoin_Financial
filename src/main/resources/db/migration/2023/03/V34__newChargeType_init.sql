
DROP TABLE IF EXISTS `order_charge_type`;
CREATE TABLE `order_charge_type`
(
    `id`              int NOT NULL,
    `type`            varchar(64) DEFAULT NULL COMMENT '交易类型',
    `name`            varchar(64) DEFAULT NULL COMMENT '名称',
    `nameEn`          varchar(64) DEFAULT NULL COMMENT '国际化名称',
    `operation_type`  varchar(64) DEFAULT NULL COMMENT '操作分类(暂时无用)',
    `operation_group` varchar(64) DEFAULT NULL COMMENT '分组名',
    `visible_type`    tinyint DEFAULT NULL COMMENT '可见类型 0:代理可见,1用户可见',
    `is_enable`       tinyint DEFAULT NULL COMMENT '是否启用 1:是; 0否',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB;

INSERT INTO `order_charge_type` (`id`, `type`, `name`, `nameEn`, `operation_type`, `operation_group`, `visible_type`,
                                 `is_enable`)
VALUES (1, 'recharge', '充值成功', 'Successful Recharge', 'RECHARGE', 'RECHARGE', 1, 1);
INSERT INTO `order_charge_type` (`id`, `type`, `name`, `nameEn`, `operation_type`, `operation_group`, `visible_type`,
                                 `is_enable`)
VALUES (2, 'withdraw_success', '提币成功', 'Successful Withdrawal', 'WITHDRAW', 'WITHDRAW', 1, 1);
INSERT INTO `order_charge_type` (`id`, `type`, `name`, `nameEn`, `operation_type`, `operation_group`, `visible_type`,
                                 `is_enable`)
VALUES (3, 'withdraw_failed', '提币失败', 'Failed Withdrawal', 'WITHDRAW', 'WITHDRAW', 1, 1);
INSERT INTO `order_charge_type` (`id`, `type`, `name`, `nameEn`, `operation_type`, `operation_group`, `visible_type`,
                                 `is_enable`)
VALUES (4, 'withdraw_freeze', '提币冻结', 'Freeze Withdrawal', 'WITHDRAW', 'WITHDRAW', 1, 1);
INSERT INTO `order_charge_type` (`id`, `type`, `name`, `nameEn`, `operation_type`, `operation_group`, `visible_type`,
                                 `is_enable`)
VALUES (5, 'redeem', '赎回', 'Redemption', 'FINANCIAL', 'IN', 1, 1);
INSERT INTO `order_charge_type` (`id`, `type`, `name`, `nameEn`, `operation_type`, `operation_group`, `visible_type`,
                                 `is_enable`)
VALUES (6, 'income', '理财收益', 'Earnings', 'FINANCIAL', 'IN', 1, 1);
INSERT INTO `order_charge_type` (`id`, `type`, `name`, `nameEn`, `operation_type`, `operation_group`, `visible_type`,
                                 `is_enable`)
VALUES (7, 'fund_interest', '基金利息', 'Fund Interest', 'FINANCIAL', 'IN', 1, 1);
INSERT INTO `order_charge_type` (`id`, `type`, `name`, `nameEn`, `operation_type`, `operation_group`, `visible_type`,
                                 `is_enable`)
VALUES (8, 'agent_fund_sale', '用户申购', 'User Subscription', 'FINANCIAL', 'IN', 0, 1);
INSERT INTO `order_charge_type` (`id`, `type`, `name`, `nameEn`, `operation_type`, `operation_group`, `visible_type`,
                                 `is_enable`)
VALUES (9, 'settle', '结算本金', 'Settlement', 'FINANCIAL', 'IN', 1, 1);
INSERT INTO `order_charge_type` (`id`, `type`, `name`, `nameEn`, `operation_type`, `operation_group`, `visible_type`,
                                 `is_enable`)
VALUES (10, 'release', '质押解冻', 'Release Pledge', 'BORROW', 'IN', 1, 0);
INSERT INTO `order_charge_type` (`id`, `type`, `name`, `nameEn`, `operation_type`, `operation_group`, `visible_type`,
                                 `is_enable`)
VALUES (11, 'borrow', '借币', 'Borrow', 'BORROW', 'IN', 1, 0);
INSERT INTO `order_charge_type` (`id`, `type`, `name`, `nameEn`, `operation_type`, `operation_group`, `visible_type`,
                                 `is_enable`)
VALUES (12, 'transfer_increase', '划转', 'Transfer ', 'EXCHANGE', 'IN', 1, 1);
INSERT INTO `order_charge_type` (`id`, `type`, `name`, `nameEn`, `operation_type`, `operation_group`, `visible_type`,
                                 `is_enable`)
VALUES (13, 'red_get', '红包领取', 'Red Packet Collection', 'CHAT', 'IN', 1, 1);
INSERT INTO `order_charge_type` (`id`, `type`, `name`, `nameEn`, `operation_type`, `operation_group`, `visible_type`,
                                 `is_enable`)
VALUES (14, 'red_back', '红包退款', 'Red Packet Refund', 'CHAT', 'IN', 1, 1);
INSERT INTO `order_charge_type` (`id`, `type`, `name`, `nameEn`, `operation_type`, `operation_group`, `visible_type`,
                                 `is_enable`)
VALUES (15, 'return_gas', '免Gas费', 'Return Gas', 'ACTIVITY', 'IN', 1, 1);
INSERT INTO `order_charge_type` (`id`, `type`, `name`, `nameEn`, `operation_type`, `operation_group`, `visible_type`,
                                 `is_enable`)
VALUES (16, 'airdrop', '空投', 'Airdrop', 'ACTIVITY', 'IN', 1, 1);
INSERT INTO `order_charge_type` (`id`, `type`, `name`, `nameEn`, `operation_type`, `operation_group`, `visible_type`,
                                 `is_enable`)
VALUES (17, 'transaction_reward', '交易奖励', 'Trading Bonus', 'ACTIVITY', 'IN', 1, 1);
INSERT INTO `order_charge_type` (`id`, `type`, `name`, `nameEn`, `operation_type`, `operation_group`, `visible_type`,
                                 `is_enable`)
VALUES (18, 'gold_exchange', '金币兑换', 'Gold Exchange', 'ACTIVITY', 'IN', 1, 0);
INSERT INTO `order_charge_type` (`id`, `type`, `name`, `nameEn`, `operation_type`, `operation_group`, `visible_type`,
                                 `is_enable`)
VALUES (19, 'swap_reward', '闪兑交易奖励', 'Swap reward', 'ACTIVITY', 'IN', 1, 1);
INSERT INTO `order_charge_type` (`id`, `type`, `name`, `nameEn`, `operation_type`, `operation_group`, `visible_type`,
                                 `is_enable`)
VALUES (20, 'user_credit_in', '用户上分划入', 'User credit in', 'GAME', 'IN', 1, 1);
INSERT INTO `order_charge_type` (`id`, `type`, `name`, `nameEn`, `operation_type`, `operation_group`, `visible_type`,
                                 `is_enable`)
VALUES (21, 'credit_in', '下分划入', 'Credit in', 'GAME', 'IN', 1, 1);
INSERT INTO `order_charge_type` (`id`, `type`, `name`, `nameEn`, `operation_type`, `operation_group`, `visible_type`,
                                 `is_enable`)
VALUES (22, 'purchase', '理财申购', 'Subscription', 'FINANCIAL', 'OUT', 1, 1);
INSERT INTO `order_charge_type` (`id`, `type`, `name`, `nameEn`, `operation_type`, `operation_group`, `visible_type`,
                                 `is_enable`)
VALUES (23, 'fund_purchase', '基金申购', 'Fund Subscription', 'FINANCIAL', 'OUT', 1, 1);
INSERT INTO `order_charge_type` (`id`, `type`, `name`, `nameEn`, `operation_type`, `operation_group`, `visible_type`,
                                 `is_enable`)
VALUES (24, 'agent_fund_redeem', '用户赎回', 'User Redemptions', 'FINANCIAL', 'OUT', 0, 1);
INSERT INTO `order_charge_type` (`id`, `type`, `name`, `nameEn`, `operation_type`, `operation_group`, `visible_type`,
                                 `is_enable`)
VALUES (25, 'agent_fund_interest', '利息支付', 'Interest payments', 'FINANCIAL', 'OUT', 0, 1);
INSERT INTO `order_charge_type` (`id`, `type`, `name`, `nameEn`, `operation_type`, `operation_group`, `visible_type`,
                                 `is_enable`)
VALUES (26, 'repay', '还币', 'Repay', 'BORROW', 'OUT', 1, 0);
INSERT INTO `order_charge_type` (`id`, `type`, `name`, `nameEn`, `operation_type`, `operation_group`, `visible_type`,
                                 `is_enable`)
VALUES (27, 'pledge', '质押', 'Collateral', 'BORROW', 'OUT', 1, 0);
INSERT INTO `order_charge_type` (`id`, `type`, `name`, `nameEn`, `operation_type`, `operation_group`, `visible_type`,
                                 `is_enable`)
VALUES (28, 'auto_re', '自动补仓', 'Automatic replenishment', 'BORROW', 'OUT', 1, 0);
INSERT INTO `order_charge_type` (`id`, `type`, `name`, `nameEn`, `operation_type`, `operation_group`, `visible_type`,
                                 `is_enable`)
VALUES (29, 'transfer_reduce', '划转', 'Transfer ', 'EXCHANGE', 'OUT', 1, 1);
INSERT INTO `order_charge_type` (`id`, `type`, `name`, `nameEn`, `operation_type`, `operation_group`, `visible_type`,
                                 `is_enable`)
VALUES (30, 'red_give', '红包发送', 'Red Packet Send', 'CHAT', 'OUT', 1, 1);
INSERT INTO `order_charge_type` (`id`, `type`, `name`, `nameEn`, `operation_type`, `operation_group`, `visible_type`,
                                 `is_enable`)
VALUES (31, 'credit_out', '上分划出', 'Credit out', 'GAME', 'OUT', 1, 1);
INSERT INTO `order_charge_type` (`id`, `type`, `name`, `nameEn`, `operation_type`, `operation_group`, `visible_type`,
                                 `is_enable`)
VALUES (32, 'user_credit_out', '用户下分划出', 'User credit out', 'GAME', 'OUT', 1, 1);
INSERT INTO `order_charge_type` (`id`, `type`, `name`, `nameEn`, `operation_type`, `operation_group`, `visible_type`,
                                 `is_enable`)
VALUES (33, 'fund_redeem', '基金赎回', 'Fund Redemptions', 'FINANCIAL', 'IN', 1, 1);


