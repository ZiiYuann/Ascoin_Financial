ALTER TABLE `financial`.`order`
    ADD UNIQUE INDEX `unqieOrderNo`(`order_no`);

CREATE TABLE `red_envelope` (
                                `id` bigint NOT NULL,
                                `uid` bigint NOT NULL,
                                `short_uid` bigint NOT NULL COMMENT '用户id短码',
                                `flag` varchar(255) NOT NULL COMMENT '红包唯一标示符号 群号、或者hash啥的',
                                `coin` varchar(10) NOT NULL COMMENT '币别',
                                `amount` decimal(20,8) NOT NULL COMMENT 'NORMAL：单个红包金额 RANDOM：红包总金额',
                                `total_amount` decimal(20,8) NOT NULL COMMENT '红包总金额',
                                `num` int NOT NULL COMMENT '红包个数',
                                `receive_num` int NOT NULL DEFAULT '0' COMMENT '已经领取红包个数',
                                `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '红包文案',
                                `type` varchar(10) NOT NULL COMMENT '红包类型 NORMAL：普通 RANDOM：手气 PRIVATE：私聊',
                                `way` varchar(10) NOT NULL COMMENT '红包方式 WALLET：钱包 CHAIN：链上',
                                `create_time` datetime NOT NULL COMMENT '红包创建时间',
                                `status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '红包状态',
                                `txid` varchar(255) DEFAULT NULL COMMENT '交易hash',
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='红包主表';

CREATE TABLE `red_envelope_spilt` (
                                      `rid` bigint NOT NULL COMMENT '红包id',
                                      `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '拆分红包uuid',
                                      `amount` decimal(20,8) NOT NULL COMMENT '单个红包金额',
                                      `receive` tinyint NOT NULL COMMENT '是否领取',
                                      `receive_time` datetime DEFAULT NULL COMMENT '领取时间',
                                      KEY `default` (`rid`,`id`) COMMENT '默认索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='红包拆分表';

CREATE TABLE `red_envelope_spilt_get_record` (
                                                 `id` bigint NOT NULL,
                                                 `rid` bigint NOT NULL COMMENT '红包id',
                                                 `s_rid` varbinary(64) NOT NULL COMMENT '子红包uuid',
                                                 `uid` bigint NOT NULL COMMENT '用户id',
                                                 `short_uid` bigint NOT NULL COMMENT '用户id短码',
                                                 `coin` varchar(20) NOT NULL COMMENT '币种',
                                                 `amount` decimal(20,8) NOT NULL COMMENT '金额',
                                                 `receive_time` datetime NOT NULL COMMENT '领取时间',
                                                 `type` varchar(20) NOT NULL COMMENT '红包类型',
                                                 PRIMARY KEY (`id`),
                                                 UNIQUE KEY `uniqe` (`rid`,`s_rid`) COMMENT '领取记录唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='红包领取记录表';