ALTER TABLE `financial_product`
    ADD COLUMN `recommend_weight` int NULL DEFAULT 0 COMMENT '推荐权重' AFTER `recommend`;

CREATE TABLE `product_hold_record`
(
    `id`           bigint      NOT NULL AUTO_INCREMENT,
    `uid`          bigint      NOT NULL COMMENT 'uid',
    `product_id`   bigint      NOT NULL COMMENT '产品id',
    `product_type` varchar(20) NOT NULL COMMENT '产品类型',
    `record_id`    bigint      NOT NULL,
    PRIMARY KEY (`id`),
    KEY `uid_index` (`uid`) USING BTREE COMMENT 'uid索引'
) ENGINE = InnoDB
  AUTO_INCREMENT = 0;

INSERT INTO `product_hold_record` (`uid`, `product_id`, `product_type`, `record_id`)
SELECT uid, product_id, product_type, id
from financial_record
where `status` = 'PROCESS';

INSERT INTO `product_hold_record` (`uid`, `product_id`, `product_type`, `record_id`)
SELECT uid, product_id, type, id
from fund_record
where `status` = 'PROCESS';


