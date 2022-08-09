-- 缺失字段添加
ALTER TABLE `financial`.`wallet_imputation`
    ADD COLUMN `log_id` bigint NULL COMMENT '日志表id' AFTER `update_time`;