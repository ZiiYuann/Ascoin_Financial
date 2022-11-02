ALTER TABLE `financial`.`financial_product`
    ADD COLUMN `recommend` tinyint(1) NULL DEFAULT 0 COMMENT '是否推荐' AFTER `min_rate`;

ALTER TABLE `financial`.`financial_record`
    ADD INDEX `uid_index` (`uid`);


