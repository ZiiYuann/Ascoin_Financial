ALTER TABLE `financial_record`
    ADD COLUMN `pledge` tinyint NULL DEFAULT 0 COMMENT '是否被质押' AFTER `local_purchase`;

ALTER TABLE `order_advance` MODIFY COLUMN `uid` bigint NULL DEFAULT NULL COMMENT '用户id' AFTER `id`;
ALTER TABLE `order_advance` MODIFY COLUMN `product_id` bigint NULL DEFAULT NULL COMMENT '产品id' AFTER `uid`;
ALTER TABLE `order_advance` MODIFY COLUMN `coin` varchar (20) NULL DEFAULT NULL COMMENT '币别' AFTER `product_id`;
ALTER TABLE `order_advance` MODIFY COLUMN `amount` decimal (20, 8) NULL DEFAULT NULL COMMENT '申购数额' AFTER `txid`;
ALTER TABLE `order_advance` MODIFY COLUMN `auto_current` tinyint(1) NULL DEFAULT NULL COMMENT '收否自动续期' AFTER `amount`;
ALTER TABLE `order_advance`
    ADD COLUMN `query` text NULL AFTER `network`;
ALTER TABLE `order_advance`
    ADD COLUMN `advance_type` varchar(20) NOT NULL AFTER `query`;

DROP TABLE IF EXISTS `borrow_coin_config`;
DROP TABLE IF EXISTS `borrow_coin_order`;
DROP TABLE IF EXISTS `borrow_interest_record`;
DROP TABLE IF EXISTS `borrow_order_num_daily`;
DROP TABLE IF EXISTS `borrow_pledge_coin_config`;
DROP TABLE IF EXISTS `borrow_pledge_record`;
DROP TABLE IF EXISTS `borrow_repay_record`;

CREATE TABLE `borrow_config_coin`
(
    `coin`        varchar(20)    NOT NULL,
    `min_amount`  decimal(20, 8) NOT NULL COMMENT '最小可借',
    `max_amount`  decimal(20, 8) NULL DEFAULT NULL COMMENT '最大可借',
    `hour_rate`   decimal(10, 8) NOT NULL COMMENT '小孩利率',
    `weight`      double         NOT NULL DEFAULT 0 COMMENT '权重',
    `status`      varchar(255)   NOT NULL DEFAULT '0' COMMENT '状态',
    `create_time` datetime       NOT NULL,
    `update_time` datetime       NOT NULL,
    `create_by`   varchar(20)    NOT NULL,
    `update_by`   varchar(20)    NOT NULL,
    PRIMARY KEY (`coin`) USING BTREE
) ENGINE = InnoDB COMMENT = '借币配置表' ROW_FORMAT = Dynamic;

CREATE TABLE `borrow_config_pledge`
(
    `coin`                  varchar(20)    NOT NULL,
    `init_pledge_rate`      decimal(20, 8) NOT NULL COMMENT '初始质押率',
    `warn_pledge_rate`      decimal(20, 8) NULL DEFAULT NULL COMMENT '预警质押',
    `lq_pledge_rate`        decimal(20, 8) NULL DEFAULT NULL COMMENT '强制质押',
    `assure_lq_pledge_rate` decimal(20, 8) NULL DEFAULT NULL COMMENT 'assure强制质押',
    `weight`                double         NOT NULL DEFAULT 0 COMMENT '权重',
    `status`                varchar(255)   NOT NULL DEFAULT '0' COMMENT '状态',
    `create_time`           datetime       NOT NULL,
    `update_time`           datetime       NOT NULL,
    `create_by`             varchar(20)    NOT NULL,
    `update_by`             varchar(20)    NOT NULL,
    PRIMARY KEY (`coin`) USING BTREE
) ENGINE = InnoDB  COMMENT = '质押配置表' ROW_FORMAT = Dynamic;

CREATE TABLE `borrow_hedge_entrust`
(
    `id`               bigint         NOT NULL,
    `bid`              bigint         NOT NULL,
    `br_id`            bigint         NOT NULL,
    `coin`             varchar(20)    NOT NULL,
    `hedge_coin`       varchar(20)    NOT NULL,
    `hedge_type`       varchar(10)    NOT NULL,
    `hedge_status`     varchar(10)    NOT NULL,
    `amount`           decimal(20, 8) NOT NULL,
    `translate_amount` decimal(20, 8) NULL DEFAULT 0.00000000,
    `create_rate`      decimal(20, 8) NOT NULL,
    `entrust_rate`     decimal(20, 8) NOT NULL,
    `translate_rate`   decimal(20, 8) NULL DEFAULT 0.00000000,
    `liquidate_id`     varchar(50) NULL DEFAULT NULL,
    `create_by`        varchar(20)    NOT NULL,
    `create_time`      datetime       NOT NULL,
    `update_by`        varchar(20)    NOT NULL,
    `update_time`      datetime       NOT NULL
) ENGINE = InnoDB  COMMENT = '对冲委托表' ROW_FORMAT = Dynamic;

CREATE TABLE `borrow_interest`
(
    `id`     bigint         NOT NULL,
    `uid`    bigint         NOT NULL,
    `bid`    bigint         NOT NULL,
    `coin`   varchar(20)    NOT NULL,
    `amount` decimal(20, 8) NOT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB  COMMENT = '借贷利息表' ROW_FORMAT = Dynamic;

CREATE TABLE `borrow_interest_log`
(
    `id`            bigint         NOT NULL,
    `uid`           bigint         NOT NULL,
    `bid`           bigint         NOT NULL,
    `coin`          varchar(20)    NOT NULL,
    `amount`        decimal(20, 8) NOT NULL,
    `interest_type` varchar(20)    NOT NULL COMMENT '计息类型',
    `interest_time` datetime       NOT NULL COMMENT '计息整点时间',
    `create_time`   datetime       NOT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uq_index`(`bid` DESC, `coin` ASC, `interest_time` ASC, `interest_type` ASC) USING BTREE COMMENT '唯一索引'
) ENGINE = InnoDB  COMMENT = '借贷利息表日志表' ROW_FORMAT = Dynamic;

CREATE TABLE `borrow_operation_log`
(
    `id`                bigint         NOT NULL,
    `uid`               bigint         NOT NULL,
    `bid`               bigint         NOT NULL COMMENT 'borrow_record 主键',
    `coin`              varchar(20)    NOT NULL,
    `rate`              decimal(20, 8) NOT NULL DEFAULT 0.00000000,
    `amount`            decimal(20, 8) NOT NULL,
    `charge_type`       varchar(20)    NOT NULL COMMENT '交易类型',
    `create_time`       datetime       NOT NULL,
    `display`           tinyint        NOT NULL DEFAULT 0,
    `pre_pledge_rate`   decimal(10, 8) NOT NULL DEFAULT 0.00000000,
    `after_pledge_rate` decimal(10, 8) NOT NULL DEFAULT 0.00000000,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX               `uid_index`(`uid` ASC) USING BTREE,
    INDEX               `type_index`(`charge_type` ASC) USING BTREE,
    INDEX               `normal_index`(`bid` ASC, `charge_type` ASC) USING BTREE
) ENGINE = InnoDB  COMMENT = '借贷操作记录表' ROW_FORMAT = Dynamic;

CREATE TABLE `borrow_record`
(
    `id`                    bigint         NOT NULL,
    `uid`                   bigint         NOT NULL,
    `borrow_fee`            decimal(20, 8) NOT NULL DEFAULT 0.00000000,
    `pledge_fee`            decimal(20, 8) NOT NULL DEFAULT 0.00000000,
    `interest_fee`          decimal(20, 8) NOT NULL DEFAULT 0.00000000,
    `pledge_status`         varchar(20)    NOT NULL COMMENT '质押状态',
    `borrow_coins`          varchar(255)   NOT NULL DEFAULT '',
    `pledge_coins`          varchar(255)   NOT NULL DEFAULT '',
    `currency_pledge_rate`  decimal(10, 8) NOT NULL DEFAULT 0.00000000 COMMENT '当质押',
    `warn_pledge_rate`      varchar(10)    NOT NULL DEFAULT '0' COMMENT '警告质押',
    `lq_pledge_rate`        decimal(10, 8) NOT NULL DEFAULT 0.00000000 COMMENT '强平质押',
    `assure_lq_pledge_rate` varchar(10)    NOT NULL DEFAULT '0' COMMENT 'assure强平质押',
    `auto_replenishment`    tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否自动补仓',
    `parent_id`             bigint NULL DEFAULT 0 COMMENT '关联父订单id',
    `finish`                tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否结束',
    `create_time`           datetime       NOT NULL,
    `update_time`           datetime       NOT NULL,
    `finish_time`           datetime NULL DEFAULT NULL,
    `newest_snapshot_id`    bigint         NOT NULL DEFAULT 0 COMMENT '快照id',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX                   `normal_index`(`finish` ASC, `uid` ASC) USING BTREE
) ENGINE = InnoDB  COMMENT = '借贷开仓记录表' ROW_FORMAT = Dynamic;

CREATE TABLE `borrow_record_coin`
(
    `id`          bigint         NOT NULL,
    `uid`         bigint         NOT NULL,
    `bid`         bigint         NOT NULL,
    `coin`        varchar(20)    NOT NULL,
    `amount`      decimal(20, 8) NOT NULL,
    `create_time` datetime       NOT NULL,
    `update_time` datetime NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX         `uid_index`(`uid` ASC) USING BTREE
) ENGINE = InnoDB  COMMENT = '借币记录表（借币池）' ROW_FORMAT = Dynamic;

CREATE TABLE `borrow_record_pledge`
(
    `id`          bigint         NOT NULL,
    `uid`         bigint         NOT NULL,
    `bid`         bigint         NOT NULL,
    `pledge_type` varchar(20)    NOT NULL COMMENT '质押类型',
    `record_id`   bigint NULL DEFAULT NULL COMMENT '活期质押为 financial_record 主键',
    `coin`        varchar(20)    NOT NULL,
    `amount`      decimal(20, 8) NOT NULL,
    `create_time` datetime       NOT NULL,
    `update_time` datetime NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX         `uid_index`(`uid` ASC) USING BTREE
) ENGINE = InnoDB  COMMENT = '质押记录表（存款池）' ROW_FORMAT = Dynamic;

CREATE TABLE `borrow_record_snapshot`
(
    `id`          bigint   NOT NULL,
    `bid`         bigint   NOT NULL,
    `uid`         bigint   NOT NULL,
    `data`        text     NOT NULL,
    `create_time` datetime NOT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB  COMMENT = '借贷快照表' ROW_FORMAT = Dynamic;