ALTER TABLE `order_review`
    ADD COLUMN `type` tinyint(1)  NULL DEFAULT 0  COMMENT '审核方式' AFTER `create_time`,
    ADD COLUMN `review_by` varchar(30)  NULL COMMENT '审核人' AFTER `type`;