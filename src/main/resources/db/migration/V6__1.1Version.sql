ALTER TABLE `financial`.`financial_product`
    ADD COLUMN `rate_type` tinyint(1) NULL DEFAULT 0 COMMENT '利率类型' AFTER `deleted`,
    ADD COLUMN `use_quota` decimal(20, 8) NULL DEFAULT 0  COMMENT '已经使用申购金额' AFTER `deleted`;

ALTER TABLE `financial`.`financial_record`
    ADD COLUMN `wait_amount` decimal(20, 8) NULL DEFAULT 0 COMMENT '待记利息金额' AFTER `update_time`,
    ADD COLUMN `income_amount` decimal(20, 8) NULL DEFAULT 0 COMMENT '记录利息金额' AFTER `update_time`;

UPDATE financial_record a, (SELECT id,hold_amount FROM financial_record )
    b set a.income_amount = b.hold_amount WHERE a.id = b.id;

UPDATE financial_product a ,
    (SELECT product_id,sum(hold_amount) as amount FROM financial_record WHERE `status` = 'PROCESS' GROUP BY product_id) b
SET a.use_quota = b.amount WHERE a.id = b.product_id;

CREATE TABLE `financial_product_ladder_rate` (
                                                 `id` bigint DEFAULT NULL,
                                                 `product_id` bigint DEFAULT NULL,
                                                 `start_point` decimal(20,8) DEFAULT NULL,
                                                 `end_point` decimal(20,8) DEFAULT NULL,
                                                 `rate` decimal(3,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='产品阶梯配置表';

ALTER TABLE `financial_product_ladder_rate`
    ADD INDEX `default`(`product_id`, `start_point` ASC);

CREATE TABLE `financial`.`order_advance`  (
                                              `id` bigint NOT NULL,
                                              `uid` bigint NOT NULL COMMENT '用户id',
                                              `product_id` bigint NOT NULL COMMENT '产品id',
                                              `txid` varchar(255) NULL COMMENT '交易hash',
                                              `amount` decimal(20, 8) NOT NULL COMMENT '申购数额',
                                              `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                              PRIMARY KEY (`id`)
) COMMENT = '预订单表';



