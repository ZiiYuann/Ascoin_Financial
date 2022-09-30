-- 缺失字段添加
ALTER TABLE `financial`.`wallet_imputation_log`
    ADD COLUMN `finish_time` bigint NULL COMMENT '结束时间' AFTER `create_time`;