ALTER TABLE `financial_product`
    MODIFY COLUMN `max_rate` decimal(10, 6) NOT NULL DEFAULT 0.00 COMMENT '最大利率' AFTER `rate_type`,
    MODIFY COLUMN `min_rate` decimal(10, 6) NOT NULL DEFAULT 0.00 COMMENT '最小利率' AFTER `max_rate`;

UPDATE `financial_product` SET `max_rate` = 0.0250 WHERE `id` = 1744574324585165639;