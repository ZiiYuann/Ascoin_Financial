ALTER TABLE `order`
    ADD COLUMN `update_time` datetime NULL AFTER `complete_time`;

UPDATE `order`
SET `update_time` = `complete_time`
WHERE `update_time` is null;

CREATE TABLE `service_fee`
(
    `id`          bigint         NOT NULL,
    `create_time` datetime       NOT NULL COMMENT '时间',
    `coin`        varchar(20)    NOT NULL COMMENT '币别',
    `amount`      decimal(20, 8) NOT NULL COMMENT '金额',
    `type`        tinyint        NOT NULL COMMENT '类型',
    `network`     varchar(20)    NOT NULL COMMENT '网络',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_index` (`create_time` DESC, `coin`, `type`) USING BTREE
) ENGINE = InnoDB;

CREATE TABLE `coin_review_config`
(
    `id`                            bigint      NOT NULL,
    `auto_review_auto_transfer`     int         NOT NULL COMMENT '自动审核，自动打币',
    `manual_review_manual_transfer` int         NOT NULL COMMENT '人工审核，人工打币',
    `hour_limit`                    tinyint     NOT NULL COMMENT '时间限制',
    `times_limit`                   tinyint     NOT NULL COMMENT '次数限制',
    `create_by`                     varchar(20) NOT NULL,
    `create_time`                   datetime    NOT NULL,
    `deleted`                       tinyint     NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB;

CREATE TABLE `banner`
(
    `id`          bigint       NOT NULL,
    `name`        varchar(30)  NOT NULL COMMENT '名称',
    `name_en`     varchar(50)  NOT NULL,
    `url_zh`      varchar(255) NOT NULL COMMENT '图中文',
    `url_en`      varchar(255) NOT NULL COMMENT '图英文',
    `jump_type`   tinyint      NOT NULL COMMENT '跳转类型(1、聊天群2、普通链接3、内部页面)',
    `jump_url`    varchar(255) NOT NULL COMMENT '跳转地址',
    `start_time`  datetime     NOT NULL,
    `end_time`    datetime     NOT NULL,
    `device_type` tinyint      NOT NULL COMMENT '设备类型(0、全部 1、安卓 2、ios)',
    `weight`      tinyint      NOT NULL COMMENT '权重',
    `create_time` datetime     NOT NULL,
    `update_time` datetime     NOT NULL,
    `create_by`   varchar(10)  NOT NULL,
    `update_by`   varchar(10)  NOT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB;

DROP TABLE IF EXISTS `withdraw_service_fee`;

ALTER TABLE `wallet_imputation`
    ADD INDEX `normal_index` (`uid` ASC, `network` ASC, `coin` ASC) USING BTREE;

ALTER TABLE `financial_record`
    ADD INDEX `uid_index` (`uid` ASC) USING BTREE;

ALTER TABLE `financial_product`
    MODIFY COLUMN `rate_type` tinyint(1) NULL DEFAULT 0 COMMENT '利率类型 0 正常 1阶梯' AFTER `use_quota`;

ALTER TABLE `financial_board_product`
    DROP PRIMARY KEY;

ALTER TABLE `financial_board_product`
    MODIFY COLUMN `create_time` datetime NOT NULL COMMENT '创建时间' FIRST;

ALTER TABLE `financial_board_product`
    ADD PRIMARY KEY (`create_time`) USING BTREE;