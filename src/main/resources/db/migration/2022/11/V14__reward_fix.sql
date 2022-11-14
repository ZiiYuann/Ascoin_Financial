drop table `order_reward_record`;

CREATE TABLE `order_reward_record` (
                                       `id` bigint NOT NULL,
                                       `uid` bigint DEFAULT NULL,
                                       `amount` decimal(20,8) DEFAULT NULL,
                                       `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
                                       `coin` varchar(10) DEFAULT NULL,
                                       `give_time` datetime NOT NULL COMMENT '订单的创建时间',
                                       `order_id` bigint NOT NULL COMMENT '奖励订单唯一主键',
                                       PRIMARY KEY (`id`),
                                       UNIQUE KEY `order_reward_record_order_id_uindex` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;