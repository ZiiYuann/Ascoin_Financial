ALTER TABLE `financial`.`order_advance`
    ADD COLUMN `network` varchar(20) NULL COMMENT '网络' AFTER `try_times`;