SET FOREIGN_KEY_CHECKS=0;

ALTER TABLE `account_balance` MODIFY COLUMN `balance` decimal(38, 8) NOT NULL COMMENT '余额' AFTER `coin`;

ALTER TABLE `account_balance` MODIFY COLUMN `freeze` decimal(38, 8) NOT NULL COMMENT '冻结' AFTER `balance`;

ALTER TABLE `account_balance` MODIFY COLUMN `remain` decimal(38, 8) NOT NULL COMMENT '剩余' AFTER `freeze`;

ALTER TABLE `account_balance` MODIFY COLUMN `pledge_freeze` decimal(38, 8) NOT NULL DEFAULT 0.00000000 COMMENT '质押冻结' AFTER `remain`;

ALTER TABLE `account_balance_operation_log` MODIFY COLUMN `amount` decimal(38, 8) NULL DEFAULT NULL AFTER `order_no`;

ALTER TABLE `account_balance_operation_log` MODIFY COLUMN `balance` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL AFTER `create_time`;

ALTER TABLE `account_balance_operation_log` MODIFY COLUMN `freeze` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL AFTER `balance`;

ALTER TABLE `account_balance_operation_log` MODIFY COLUMN `remain` decimal(50, 8) NULL DEFAULT NULL AFTER `freeze`;

ALTER TABLE `account_user_transfer` MODIFY COLUMN `amount` decimal(38, 8) NOT NULL COMMENT '金额' AFTER `coin`;

ALTER TABLE `borrow_config_coin` MODIFY COLUMN `min_amount` decimal(38, 8) NOT NULL COMMENT '最小可借' AFTER `coin`;

ALTER TABLE `borrow_config_coin` MODIFY COLUMN `max_amount` decimal(38, 8) NULL DEFAULT NULL COMMENT '最大可借' AFTER `min_amount`;

ALTER TABLE `borrow_hedge_entrust` MODIFY COLUMN `amount` decimal(38, 8) NOT NULL AFTER `hedge_status`;

ALTER TABLE `borrow_hedge_entrust` MODIFY COLUMN `translate_amount` decimal(38, 8) NULL DEFAULT 0.00000000 AFTER `amount`;

ALTER TABLE `borrow_interest` MODIFY COLUMN `amount` decimal(38, 8) NOT NULL AFTER `coin`;

ALTER TABLE `borrow_interest_log` MODIFY COLUMN `amount` decimal(38, 8) NOT NULL AFTER `coin`;

ALTER TABLE `borrow_operation_log` MODIFY COLUMN `amount` decimal(38, 8) NOT NULL AFTER `rate`;

ALTER TABLE `borrow_record` MODIFY COLUMN `borrow_fee` decimal(38, 8) NOT NULL DEFAULT 0.00000000 AFTER `uid`;

ALTER TABLE `borrow_record` MODIFY COLUMN `pledge_fee` decimal(38, 8) NOT NULL DEFAULT 0.00000000 AFTER `borrow_fee`;

ALTER TABLE `borrow_record` MODIFY COLUMN `interest_fee` decimal(38, 8) NOT NULL DEFAULT 0.00000000 AFTER `pledge_fee`;

ALTER TABLE `borrow_record_coin` MODIFY COLUMN `amount` decimal(38, 8) NOT NULL AFTER `coin`;

ALTER TABLE `borrow_record_pledge` MODIFY COLUMN `amount` decimal(38, 8) NOT NULL AFTER `coin`;

ALTER TABLE `coin` MODIFY COLUMN `withdraw_min` decimal(38, 8) NOT NULL DEFAULT 0.00000000 COMMENT '最小提现金额' AFTER `withdraw_decimals`;

ALTER TABLE `coin` MODIFY COLUMN `withdraw_fixed_amount` decimal(38, 8) NOT NULL DEFAULT 0.00000000 COMMENT '固定手续费' AFTER `withdraw_min`;

ALTER TABLE `financial_board_product` MODIFY COLUMN `redeem_amount` decimal(38, 8) NOT NULL COMMENT '赎回金额' AFTER `id`;

ALTER TABLE `financial_board_product` MODIFY COLUMN `settle_amount` decimal(38, 8) NOT NULL COMMENT '结算金额' AFTER `redeem_amount`;

ALTER TABLE `financial_board_product` MODIFY COLUMN `purchase_amount` decimal(38, 8) NOT NULL COMMENT '申购金额' AFTER `settle_amount`;

ALTER TABLE `financial_board_product` MODIFY COLUMN `transfer_amount` decimal(38, 8) NOT NULL COMMENT '转存金额' AFTER `purchase_amount`;

ALTER TABLE `financial_board_product` MODIFY COLUMN `income` decimal(38, 8) NOT NULL COMMENT '累计收益' AFTER `transfer_amount`;

ALTER TABLE `financial_board_wallet` MODIFY COLUMN `recharge_amount` decimal(38, 8) NOT NULL COMMENT '充值金额' AFTER `id`;

ALTER TABLE `financial_board_wallet` MODIFY COLUMN `withdraw_amount` decimal(38, 8) NOT NULL COMMENT '提币金额' AFTER `recharge_amount`;

ALTER TABLE `financial_board_wallet` MODIFY COLUMN `total_service_amount` decimal(38, 8) NOT NULL AFTER `create_time`;

ALTER TABLE `financial_board_wallet` MODIFY COLUMN `usdt_service_amount` decimal(38, 8) NOT NULL AFTER `total_service_amount`;

ALTER TABLE `financial_income_accrue` MODIFY COLUMN `income_amount` decimal(38, 8) NULL DEFAULT NULL COMMENT '收益' AFTER `coin`;

ALTER TABLE `financial_income_daily` MODIFY COLUMN `income_amount` decimal(38, 8) NULL DEFAULT NULL COMMENT '收益' AFTER `record_id`;

ALTER TABLE `financial_income_daily` MODIFY COLUMN `amount` decimal(38, 8) NULL DEFAULT NULL COMMENT '计算时的金额' AFTER `rate`;

ALTER TABLE `financial_product` MODIFY COLUMN `person_quota` decimal(38, 8) NULL DEFAULT NULL COMMENT '个人额度' AFTER `business_type`;

ALTER TABLE `financial_product` MODIFY COLUMN `total_quota` decimal(38, 8) NULL DEFAULT NULL COMMENT '总额度' AFTER `person_quota`;

ALTER TABLE `financial_product` MODIFY COLUMN `limit_purchase_quota` decimal(38, 8) NULL DEFAULT NULL COMMENT '最低申购额度' AFTER `update_time`;

ALTER TABLE `financial_product` MODIFY COLUMN `use_quota` decimal(38, 8) NULL DEFAULT 0.00000000 COMMENT '已经使用申购金额' AFTER `deleted`;

ALTER TABLE `financial_record` MODIFY COLUMN `hold_amount` decimal(38, 8) NOT NULL COMMENT '持有金额' AFTER `status`;

ALTER TABLE `financial_record` MODIFY COLUMN `wait_amount` decimal(38, 8) NULL DEFAULT 0.00000000 COMMENT '待记利息金额' AFTER `update_time`;

ALTER TABLE `financial_record` MODIFY COLUMN `income_amount` decimal(38, 8) NULL DEFAULT 0.00000000 COMMENT '记录利息金额' AFTER `wait_amount`;

ALTER TABLE `fund_income_record` MODIFY COLUMN `hold_amount` decimal(38, 8) NULL DEFAULT NULL COMMENT '持有数额' AFTER `rate`;

ALTER TABLE `fund_income_record` MODIFY COLUMN `interest_amount` decimal(38, 8) NULL DEFAULT NULL COMMENT '利息数额' AFTER `hold_amount`;

ALTER TABLE `fund_record` MODIFY COLUMN `hold_amount` decimal(38, 8) NULL DEFAULT NULL COMMENT '持有金额' AFTER `logo`;

ALTER TABLE `fund_record` MODIFY COLUMN `cumulative_income_amount` decimal(38, 8) NULL DEFAULT 0.00000000 COMMENT '累计收益' AFTER `hold_amount`;

ALTER TABLE `fund_record` MODIFY COLUMN `income_amount` decimal(38, 8) NULL DEFAULT 0.00000000 COMMENT '已发收益' AFTER `cumulative_income_amount`;

ALTER TABLE `fund_record` MODIFY COLUMN `wait_income_amount` decimal(38, 8) NULL DEFAULT 0.00000000 COMMENT '待发收益' AFTER `income_amount`;

ALTER TABLE `fund_transaction_record` MODIFY COLUMN `transaction_amount` decimal(38, 8) NULL DEFAULT NULL COMMENT '交易金额' AFTER `product_name`;

ALTER TABLE `hot_wallet_detailed` MODIFY COLUMN `amount` decimal(38, 8) NOT NULL AFTER `uid`;

ALTER TABLE `order` MODIFY COLUMN `service_amount` decimal(38, 8) UNSIGNED NULL DEFAULT 0.00000000 COMMENT '手续费' AFTER `coin`;

ALTER TABLE `order` MODIFY COLUMN `amount` decimal(38, 8) NOT NULL COMMENT '金额' AFTER `service_amount`;

ALTER TABLE `order_advance` MODIFY COLUMN `amount` decimal(38, 8) NULL DEFAULT NULL COMMENT '申购数额' AFTER `txid`;

ALTER TABLE `order_charge_info` MODIFY COLUMN `fee` decimal(38, 8) NOT NULL COMMENT '总金额' AFTER `coin`;

ALTER TABLE `order_charge_info` MODIFY COLUMN `service_fee` decimal(38, 8) NULL DEFAULT NULL COMMENT '手续费' AFTER `fee`;

ALTER TABLE `order_charge_info` MODIFY COLUMN `real_fee` decimal(46, 0) NOT NULL COMMENT '真实金额' AFTER `service_fee`;

ALTER TABLE `order_charge_info` MODIFY COLUMN `miner_fee` decimal(38, 8) NULL DEFAULT NULL COMMENT '矿工费' AFTER `real_fee`;

ALTER TABLE `order_reward_record` MODIFY COLUMN `amount` decimal(38, 8) NULL DEFAULT NULL AFTER `uid`;

ALTER TABLE `red_envelope` MODIFY COLUMN `amount` decimal(38, 8) NOT NULL COMMENT 'NORMAL：单个红包金额 RANDOM：红包总金额' AFTER `channel`;

ALTER TABLE `red_envelope` MODIFY COLUMN `total_amount` decimal(38, 8) NOT NULL COMMENT '红包总金额' AFTER `amount`;

ALTER TABLE `red_envelope` MODIFY COLUMN `receive_amount` decimal(38, 8) NOT NULL DEFAULT 0.00000000 COMMENT '已经领取金额' AFTER `receive_num`;

ALTER TABLE `red_envelope_config` MODIFY COLUMN `limit_amount` decimal(38, 8) NOT NULL COMMENT '限制总金额' AFTER `num`;

ALTER TABLE `red_envelope_config` MODIFY COLUMN `min_amount` decimal(38, 8) NOT NULL COMMENT '最小金额' AFTER `limit_amount`;

ALTER TABLE `red_envelope_spilt` MODIFY COLUMN `amount` decimal(38, 8) NOT NULL COMMENT '单个红包金额' AFTER `id`;

ALTER TABLE `red_envelope_spilt_get_record` MODIFY COLUMN `amount` decimal(38, 8) NOT NULL COMMENT '金额' AFTER `coin`;

ALTER TABLE `service_fee` MODIFY COLUMN `amount` decimal(38, 8) NOT NULL COMMENT '金额' AFTER `coin`;

ALTER TABLE `wallet_imputation` MODIFY COLUMN `amount` decimal(38, 8) NOT NULL COMMENT '归集金额' AFTER `address_id`;

ALTER TABLE `wallet_imputation_log` MODIFY COLUMN `amount` decimal(38, 8) NOT NULL COMMENT '归集金额' AFTER `txid`;

ALTER TABLE `wallet_imputation_log_appendix` MODIFY COLUMN `amount` decimal(38, 8) NOT NULL COMMENT '归集金额' AFTER `txid`;

ALTER TABLE `wallet_imputation_temporary` MODIFY COLUMN `amount` decimal(38, 8) NOT NULL COMMENT '归集金额' AFTER `uid`;

