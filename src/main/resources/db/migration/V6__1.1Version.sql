ALTER TABLE `financial`.`financial_product`
    ADD COLUMN `use_quota` decimal(20, 8) NULL DEFAULT 0  COMMENT '已经使用申购金额' AFTER `deleted`;

ALTER TABLE `financial`.`financial_record`
    ADD COLUMN `wait_amount` decimal(20, 8) NULL DEFAULT 0 COMMENT '待记利息金额' AFTER `update_time`,
    ADD COLUMN `income_amount` decimal(20, 8) NULL DEFAULT 0 COMMENT '记录利息金额' AFTER `update_time`;

UPDATE financial_record a, (SELECT id,hold_amount FROM financial_record )
    b set a.income_amount = b.hold_amount WHERE a.id = b.id;

UPDATE financial_product a ,
    (SELECT product_id,sum(hold_amount) as amount FROM financial_record WHERE `status` = 'PROCESS' GROUP BY product_id) b
SET a.use_quota = b.amount WHERE a.id = b.product_id;