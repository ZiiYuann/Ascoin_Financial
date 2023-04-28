
DROP TABLE IF EXISTS `c2c_transfer_record`;

CREATE TABLE `c2c_transfer_record` (
                                       `id` bigint NOT NULL,
                                       `uid` bigint NOT NULL COMMENT 'uid',
                                       `amount` decimal(10,2) NOT NULL COMMENT '金额',
                                       `coin` varchar(64) NOT NULL COMMENT '币种',
                                       `charge_type` varchar(64) DEFAULT NULL COMMENT '划转类型',
                                       `c2c_order_no` varchar(255) NOT NULL COMMENT 'c2c订单id',
                                       `external_pk` bigint NOT NULL COMMENT '外键',
                                       `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                       PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;