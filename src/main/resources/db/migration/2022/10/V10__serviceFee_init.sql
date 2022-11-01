CREATE TABLE `withdraw_service_fee` (
                                        `id` bigint NOT NULL,
                                        `create_time` datetime NOT NULL,
                                        `bnb` decimal(20,9) DEFAULT '0.000000000',
                                        `eth` decimal(20,9) DEFAULT '0.000000000',
                                        `trx` decimal(20,9) DEFAULT '0.000000000',
                                        PRIMARY KEY (`id`) USING BTREE,
                                        KEY `create_time_asc` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;