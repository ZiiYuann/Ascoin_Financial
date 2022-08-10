ALTER TABLE `financial`.`wallet_imputation_log`
    MODIFY COLUMN `finish_time` datetime NULL DEFAULT NULL COMMENT '结束时间' AFTER `create_time`;