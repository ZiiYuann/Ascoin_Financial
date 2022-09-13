ALTER TABLE `financial`.`financial_product`
    ADD COLUMN `rate_type` tinyint(1) NULL DEFAULT 0 COMMENT '利率类型' AFTER `deleted`,
    ADD COLUMN `use_quota` decimal(20, 8) NULL DEFAULT 0  COMMENT '已经使用申购金额' AFTER `deleted`,
    ADD COLUMN `max_rate` decimal(10, 2) NOT NULL DEFAULT 0 COMMENT '最大利率' AFTER `rate_type`,
    ADD COLUMN `min_rate` decimal(10, 2) NOT NULL DEFAULT 0 COMMENT '最小利率' AFTER `max_rate`;

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

CREATE TABLE `order_advance` (
                                 `id` bigint NOT NULL,
                                 `uid` bigint NOT NULL COMMENT '用户id',
                                 `product_id` bigint NOT NULL COMMENT '产品id',
                                 `coin` varchar(20) NOT NULL COMMENT '币别',
                                 `term` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '限期',
                                 `txid` varchar(255) DEFAULT NULL COMMENT '交易hash',
                                 `amount` decimal(20,8) NOT NULL COMMENT '申购数额',
                                 `auto_current` tinyint(1) NOT NULL COMMENT '收否自动续期',
                                 `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预订单表';

CREATE TABLE `exception_msg` (
                                 `id` bigint DEFAULT NULL,
                                 `msg` text,
                                 `create_time` datetime DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='异常信息表';



CREATE TABLE `wallet_agent` (
                                `id` bigint NOT NULL COMMENT 'ID',
                                `uid` bigint DEFAULT NULL COMMENT '代理人ID',
                                `agent_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '代理人名称',
                                `login_password` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '登陆密码',
                                `remark` varchar(256) DEFAULT NULL COMMENT '备注',
                                `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                `deleted` bit(1) DEFAULT b'0' COMMENT '删除标记',
                                PRIMARY KEY (`id`),
                                KEY `idx_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='云钱包代理人';

CREATE TABLE `wallet_agent_product` (
                                        `id` bigint DEFAULT NULL COMMENT 'ID',
                                        `agent_id` bigint DEFAULT NULL COMMENT '代理人ID',
                                        `uid` bigint DEFAULT NULL COMMENT '用户ID',
                                        `product_id` bigint DEFAULT NULL COMMENT '产品ID',
                                        `product_name` varchar(20) DEFAULT NULL COMMENT '产品名称',
                                        `referral_code` varchar(20) DEFAULT NULL COMMENT '推荐码',
                                        KEY `idx_agent_id` (`agent_id`),
                                        KEY `idx_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='云钱包代理人和产品关联';

CREATE TABLE `fund_transaction_record` (
                                           `id` bigint NOT NULL COMMENT 'id',
                                           `uid` bigint DEFAULT NULL COMMENT '用户ID',
                                           `fund_id` bigint DEFAULT NULL COMMENT '基金ID',
                                           `product_id` bigint DEFAULT NULL COMMENT '产品ID',
                                           `product_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '产品名称',
                                           `transaction_amount` decimal(20,8) DEFAULT NULL COMMENT '交易金额',
                                           `coin` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '币别',
                                           `rate` decimal(20,8) DEFAULT NULL COMMENT '年化率',
                                           `type` varchar(20) DEFAULT NULL COMMENT '类型',
                                           `status` int DEFAULT NULL COMMENT '状态',
                                           `create_time` datetime DEFAULT NULL COMMENT '创建日期',
                                           PRIMARY KEY (`id`),
                                           KEY `idx_uid` (`uid`),
                                           KEY `idx_fund_id` (`fund_id`),
                                           KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基金交易记录';

CREATE TABLE `fund_review` (
                               `id` bigint NOT NULL COMMENT 'id',
                               `type` varchar(20) DEFAULT NULL COMMENT '类型',
                               `r_id` bigint DEFAULT NULL COMMENT '关联ID',
                               `remark` varchar(256) DEFAULT NULL COMMENT '备注',
                               `status` varchar(20) DEFAULT NULL COMMENT '状态',
                               `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                               PRIMARY KEY (`id`),
                               KEY `idx_r_id` (`r_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基金审核记录';

CREATE TABLE `fund_record` (
                               `id` bigint NOT NULL COMMENT 'ID',
                               `uid` bigint DEFAULT NULL COMMENT '用户ID',
                               `product_id` bigint DEFAULT NULL COMMENT '产品ID',
                               `product_name` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '产品名称',
                               `product_name_en` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '英文名称',
                               `coin` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '币种',
                               `logo` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'logo',
                               `hold_amount` decimal(20,8) DEFAULT NULL COMMENT '持有金额',
                               `cumulative_income_amount` decimal(20,8) DEFAULT '0.00000000' COMMENT '累计收益',
                               `income_amount` decimal(20,8) DEFAULT '0.00000000' COMMENT '已发收益',
                               `wait_income_amount` decimal(20,8) DEFAULT '0.00000000' COMMENT '待发收益',
                               `risk_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '风险类型',
                               `business_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '运营类型',
                               `rate` decimal(20,8) DEFAULT NULL COMMENT '年利率',
                               `status` varchar(20) DEFAULT NULL COMMENT '完成状态',
                               `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '交易类型',
                               `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                               PRIMARY KEY (`id`),
                               KEY `idx_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基金持有记录';

CREATE TABLE `fund_income_record` (
                                      `id` bigint NOT NULL COMMENT 'id',
                                      `uid` bigint DEFAULT NULL COMMENT '用户ID',
                                      `fund_id` bigint DEFAULT NULL COMMENT '基金ID',
                                      `product_id` bigint DEFAULT NULL COMMENT '产品ID',
                                      `product_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '产品名称',
                                      `coin` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '币种',
                                      `rate` decimal(20,8) DEFAULT NULL COMMENT '年利率',
                                      `hold_amount` decimal(20,8) DEFAULT NULL COMMENT '持有数额',
                                      `interest_amount` decimal(20,8) DEFAULT NULL COMMENT '利息数额',
                                      `status` int DEFAULT NULL COMMENT '状态',
                                      `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `uk_fund_id` (`fund_id`,`create_time`),
                                      KEY `idx_fund_id` (`fund_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基金收益记录';
