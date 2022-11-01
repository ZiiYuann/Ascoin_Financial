CREATE TABLE `order_reward_record` (
                                       `id` varchar(20) NOT NULL,
                                       `uid` bigint DEFAULT NULL,
                                       `amount` decimal(20,8) DEFAULT NULL,
                                       `type` varchar(20) DEFAULT NULL,
                                       `coin` varchar(10) DEFAULT NULL,
                                       PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;