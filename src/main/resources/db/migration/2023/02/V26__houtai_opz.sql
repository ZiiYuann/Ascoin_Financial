
ALTER TABLE `financial_product`
    ADD COLUMN `recommend_weight` int NULL DEFAULT 0 COMMENT '推荐权重' AFTER `recommend`;