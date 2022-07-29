-- 固定手续费
INSERT INTO `financial`.`config` (`name`, `value`) VALUES ('usdt_trc20_withdraw_fix_amount', '1');
INSERT INTO `financial`.`config` (`name`, `value`) VALUES ('usdt_bep20_withdraw_fix_amount', '1');
INSERT INTO `financial`.`config` (`name`, `value`) VALUES ('usdt_erc20_withdraw_fix_amount', '1');

-- 最小提币金额
INSERT INTO `financial`.`config` (`name`, `value`) VALUES ('usdt_erc20_withdraw_min_amount', '10');
INSERT INTO `financial`.`config` (`name`, `value`) VALUES ('usdt_trc20_withdraw_min_amount', '10');
INSERT INTO `financial`.`config` (`name`, `value`) VALUES ('usdt_bep20_withdraw_min_amount', '10');

-- 手续费率
INSERT INTO `financial`.`config` (`name`, `value`) VALUES ('usdt_trc20_withdraw_rate', '0');
INSERT INTO `financial`.`config` (`name`, `value`) VALUES ('usdt_erc20_withdraw_rate', '0');
INSERT INTO `financial`.`config` (`name`, `value`) VALUES ('usdt_bep20_withdraw_rate', '0');