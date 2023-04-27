ALTER TABLE `account_user_transfer` ADD COLUMN `transfer_chat_id` bigint NULL DEFAULT NULL AFTER `transfer_uid`;

ALTER TABLE `account_user_transfer` ADD COLUMN `receive_chat_id` bigint NULL DEFAULT NULL AFTER `receive_uid`;

ALTER TABLE `coin_base` ADD COLUMN `withdraw_decimals` tinyint NULL DEFAULT 8 COMMENT 'assure转账小数点位数' AFTER `display`;

ALTER TABLE `coin_base` ADD COLUMN `withdraw_min` decimal(38, 8) NULL DEFAULT 0.00000000 COMMENT 'assure转账最小金额' AFTER `withdraw_decimals`;

ALTER TABLE `financial_board_product` ADD PRIMARY KEY (`create_time`) USING BTREE;

ALTER TABLE `order` ADD COLUMN `related_remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联资源附录信息' AFTER `related_id`;

ALTER TABLE `order_charge_info` ADD COLUMN `chain` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL AFTER `network`;

ALTER TABLE `red_envelope_config` MODIFY COLUMN `coin` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '币别' FIRST;

ALTER TABLE `red_envelope_config` MODIFY COLUMN `channel` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '渠道' AFTER `coin`;

ALTER TABLE `red_envelope_config` MODIFY COLUMN `create_by` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL AFTER `min_amount`;

ALTER TABLE `red_envelope_config` MODIFY COLUMN `update_by` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL AFTER `create_time`;

insert into `config`(name,value) values('wallet_news_server_url','https://wallet-news.giantdt.com');

update coin_base set withdraw_decimals = 8 ,withdraw_min = 1 where name in ('usdt','usdc','busd','tusd');
update coin_base set withdraw_decimals = 8 ,withdraw_min = 0.001 where name in ('bnb','eth');
update coin_base set withdraw_decimals = 8 ,withdraw_min = 10 where name in ('matic','doge','jasmy');
update coin_base set withdraw_decimals = 6 ,withdraw_min = 10 where name in ('trx');
update coin_base set withdraw_decimals = 8 ,withdraw_min = 100 where name in ('shib');
update coin_base set withdraw_decimals = 8 ,withdraw_min = 320000 where name in ('btt');
update coin_base set withdraw_decimals = 2 ,withdraw_min = 1 where name in ('fb');
update coin_base set withdraw_decimals = 2 ,withdraw_min = 1000000000 where name in ('aidoge');
update coin_base set withdraw_decimals = 8 ,withdraw_min = 0 where name in ('hook');
