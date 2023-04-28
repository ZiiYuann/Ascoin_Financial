ALTER TABLE `account_user_transfer` ADD COLUMN `transfer_chat_id` bigint NULL DEFAULT NULL AFTER `transfer_uid`;

ALTER TABLE `account_user_transfer` ADD COLUMN `receive_chat_id` bigint NULL DEFAULT NULL AFTER `receive_uid`;

ALTER TABLE `coin_base` ADD COLUMN `withdraw_decimals` tinyint NULL DEFAULT 8 COMMENT 'assure转账小数点位数' AFTER `display`;

ALTER TABLE `coin_base` ADD COLUMN `withdraw_min` decimal(38, 8) NULL DEFAULT 0.00000000 COMMENT 'assure转账最小金额' AFTER `withdraw_decimals`;

ALTER TABLE `financial_board_product` ADD PRIMARY KEY (`create_time`) USING BTREE;

ALTER TABLE `order` ADD COLUMN `related_remarks` varchar(255)  NULL DEFAULT NULL COMMENT '关联资源附录信息' AFTER `related_id`;

insert into `config`(name,value) values('wallet_news_server_url','https://wallet-news.giantdt.com');

insert into order_charge_type(id,type,name,nameEn,operation_type,operation_group,visible_type,is_enable)values
(36,'assure_withdraw','提币成功',	'Successful Withdrawal','WITHDRAW','WITHDRAW',1,1),
(37,'assure_recharge','充值成功',	'Successful Recharge','RECHARGE','RECHARGE',1,1);

update coin_base set withdraw_decimals = 8 ,withdraw_min = 1 where name in ('usdt','usdc','busd','tusd');
update coin_base set withdraw_decimals = 8 ,withdraw_min = 0.001 where name in ('bnb','eth');
update coin_base set withdraw_decimals = 8 ,withdraw_min = 10 where name in ('matic','doge','jasmy');
update coin_base set withdraw_decimals = 6 ,withdraw_min = 10 where name in ('trx');
update coin_base set withdraw_decimals = 8 ,withdraw_min = 100 where name in ('shib');
update coin_base set withdraw_decimals = 8 ,withdraw_min = 320000 where name in ('btt');
update coin_base set withdraw_decimals = 2 ,withdraw_min = 1 where name in ('fb');
update coin_base set withdraw_decimals = 2 ,withdraw_min = 1000000000 where name in ('aidoge');
update coin_base set withdraw_decimals = 8 ,withdraw_min = 0 where name in ('hook');
