CREATE TABLE `coin`
(
    `id`                    bigint                  DEFAULT NULL,
    `name`                  varchar(10)    NOT NULL COMMENT '币别',
    `contract`              varchar(100)   NOT NULL COMMENT '合约地址',
    `chain`                 varchar(10)    NOT NULL COMMENT '公链',
    `network`               varchar(255)   NOT NULL COMMENT '网络',
    `rate_url`              varchar(255)            DEFAULT NULL COMMENT '汇率配置url(用于获取不到汇率时使用）',
    `rate_field`            varchar(20)             DEFAULT NULL COMMENT '汇率url解析汇率的字段',
    `status`                tinyint unsigned        DEFAULT '0' COMMENT '状态：0未上架  1上架中 2 上架完成 3下架',
    `update_time`           datetime       NOT NULL COMMENT '更新时间',
    `create_time`           datetime       NOT NULL COMMENT '创建时间',
    `create_by`             varchar(30)             DEFAULT NULL COMMENT '创建人',
    `update_by`             varchar(30)             DEFAULT NULL COMMENT '修改人',
    `main_token`            tinyint(1)     NOT NULL DEFAULT '0' COMMENT '是否是主币',
    `decimals`              tinyint        NOT NULL COMMENT '小数点位数（链）',
    `withdraw_decimals`     tinyint        NOT NULL DEFAULT '8' COMMENT '小数点位数',
    `withdraw_min`          decimal(20, 8) NOT NULL DEFAULT '0.00000000' COMMENT '最小提现金额',
    `withdraw_fixed_amount` decimal(20, 8) NOT NULL DEFAULT '0.00000000' COMMENT '固定手续费',
    UNIQUE KEY `uq_normal` (`name`, `chain`, `network`) USING BTREE COMMENT '币种唯一索引'
) ENGINE = InnoDB COMMENT ='币别配置表';

CREATE TABLE `coin_base`
(
    `name`        varchar(20)  NOT NULL COMMENT '币别名称',
    `weight`      tinyint      NOT NULL COMMENT '权重(0-255)',
    `logo`        varchar(255) NOT NULL COMMENT 'logo',
    `create_time` datetime              DEFAULT NULL,
    `create_by`   varchar(30)           DEFAULT NULL,
    `update_time` datetime              DEFAULT NULL,
    `update_by`   varchar(30)           DEFAULT NULL,
    `rate_url`    varchar(255)          DEFAULT NULL COMMENT '汇率配置url(用于获取不到汇率时使用）',
    `rate_field`  varchar(255)          DEFAULT NULL COMMENT '汇率url解析汇率的字段',
    `main_token`  tinyint      NOT NULL DEFAULT '0' COMMENT '是否是主币',
    `display`     tinyint      NOT NULL DEFAULT '0' COMMENT '是否显示',
    PRIMARY KEY (`name`),
    UNIQUE KEY `uq_name` (`name`)
) ENGINE = InnoDB COMMENT ='币别基础表';


INSERT INTO `financial`.`coin_base` (`name`, `weight`, `logo`, `create_time`, `create_by`, `update_time`, `update_by`,
                                     `rate_url`, `rate_field`, `main_token`, `display`)
VALUES ('bnb', 97,
        'https://assure-financial-jp.s3-accelerate.amazonaws.com/file/49807aaa-e661-4afe-b2d7-2072b157f775.png',
        '2022-12-01 14:54:11', NULL, '2022-12-01 14:54:13', NULL, NULL, NULL, 1, 1);
INSERT INTO `financial`.`coin_base` (`name`, `weight`, `logo`, `create_time`, `create_by`, `update_time`, `update_by`,
                                     `rate_url`, `rate_field`, `main_token`, `display`)
VALUES ('eth', 98,
        'https://assure-financial-jp.s3-accelerate.amazonaws.com/file/5ac3ddbd-7a99-4fd1-8cda-0bc2fcf7e775.png',
        '2022-12-01 14:53:47', NULL, '2022-12-01 14:53:50', NULL, NULL, NULL, 1, 1);
INSERT INTO `financial`.`coin_base` (`name`, `weight`, `logo`, `create_time`, `create_by`, `update_time`, `update_by`,
                                     `rate_url`, `rate_field`, `main_token`, `display`)
VALUES ('usdc', 98,
        'https://assure-financial-jp.s3-accelerate.amazonaws.com/file/10c3e027-278c-4251-8133-a057eb4ecd1c.png',
        '2022-12-01 14:51:15', NULL, '2022-12-01 14:50:35', NULL, NULL, NULL, 0, 1);
INSERT INTO `financial`.`coin_base` (`name`, `weight`, `logo`, `create_time`, `create_by`, `update_time`, `update_by`,
                                     `rate_url`, `rate_field`, `main_token`, `display`)
VALUES ('usdt', 100,
        'https://assure-financial-jp.s3-accelerate.amazonaws.com/file/a4ffebf3-ddf4-4e3f-ae7e-1ab09f808e0f.png',
        '2022-11-30 16:27:46', NULL, '2022-12-06 20:42:35', NULL, NULL, NULL, 0, 1);

INSERT INTO `financial`.`coin` (`id`, `name`, `contract`, `chain`, `network`, `rate_url`, `rate_field`, `status`, `update_time`, `create_time`, `create_by`, `update_by`, `main_token`, `decimals`, `withdraw_decimals`, `withdraw_min`, `withdraw_fixed_amount`) VALUES (1597528554238193664, 'bnb', '', 'BSC', 'bep20', NULL, NULL, 2, '2022-12-01 14:56:50', '2022-12-01 14:56:52', NULL, NULL, 1, 18, 8, 0.01, 0.0008);
INSERT INTO `financial`.`coin` (`id`, `name`, `contract`, `chain`, `network`, `rate_url`, `rate_field`, `status`, `update_time`, `create_time`, `create_by`, `update_by`, `main_token`, `decimals`, `withdraw_decimals`, `withdraw_min`, `withdraw_fixed_amount`) VALUES (1597528554238193666, 'eth', '', 'ETH', 'erc20', NULL, NULL, 2, '2022-12-01 14:59:52', '2022-12-06 16:42:27', NULL, NULL, 1, 18, 8, 0.01, 0.0015);
INSERT INTO `financial`.`coin` (`id`, `name`, `contract`, `chain`, `network`, `rate_url`, `rate_field`, `status`, `update_time`, `create_time`, `create_by`, `update_by`, `main_token`, `decimals`, `withdraw_decimals`, `withdraw_min`, `withdraw_fixed_amount`) VALUES (1598206628236673026, 'usdc', '0x8AC76a51cc950d9822D68b83fE1Ad97B32Cd580d', 'BSC', 'bep20', NULL, NULL, 2, '2022-12-01 14:45:28', '2022-12-01 14:45:28', '88888888', '88888888', 0, 18, 6, 10, 1);
INSERT INTO `financial`.`coin` (`id`, `name`, `contract`, `chain`, `network`, `rate_url`, `rate_field`, `status`, `update_time`, `create_time`, `create_by`, `update_by`, `main_token`, `decimals`, `withdraw_decimals`, `withdraw_min`, `withdraw_fixed_amount`) VALUES (1598207913737568257, 'usdc', '0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48', 'ETH', 'erc20', NULL, NULL, 2, '2022-12-01 14:50:35', '2022-12-01 14:50:35', '88888888', '88888888', 0, 6, 6, 10, 1);
INSERT INTO `financial`.`coin` (`id`, `name`, `contract`, `chain`, `network`, `rate_url`, `rate_field`, `status`, `update_time`, `create_time`, `create_by`, `update_by`, `main_token`, `decimals`, `withdraw_decimals`, `withdraw_min`, `withdraw_fixed_amount`) VALUES (1598208081622974466, 'usdc', 'TEkxiTehnzSmSe2XqrBj4w32RUN966rdz8', 'TRON', 'trc20', NULL, NULL, 2, '2022-12-01 14:51:15', '2022-12-01 14:51:15', '88888888', '88888888', 0, 6, 6, 10, 1);
INSERT INTO `financial`.`coin` (`id`, `name`, `contract`, `chain`, `network`, `rate_url`, `rate_field`, `status`, `update_time`, `create_time`, `create_by`, `update_by`, `main_token`, `decimals`, `withdraw_decimals`, `withdraw_min`, `withdraw_fixed_amount`) VALUES (1594887329895960578, 'usdt', '0x55d398326f99059ff775485246999027b3197955', 'BSC', 'bep20', NULL, NULL, 2, '2022-11-22 10:55:46', '2022-12-06 16:32:30', '88888888', '88888888', 0, 18, 6, 10, 1);
INSERT INTO `financial`.`coin` (`id`, `name`, `contract`, `chain`, `network`, `rate_url`, `rate_field`, `status`, `update_time`, `create_time`, `create_by`, `update_by`, `main_token`, `decimals`, `withdraw_decimals`, `withdraw_min`, `withdraw_fixed_amount`) VALUES (1597525353672802306, 'usdt', '0xdac17f958d2ee523a2206206994597c13d831ec7', 'ETH', 'erc20', NULL, NULL, 2, '2022-12-06 18:29:34', '2022-11-29 17:45:39', '88888888', '88888888', 0, 6, 6, 10, 1);
INSERT INTO `financial`.`coin` (`id`, `name`, `contract`, `chain`, `network`, `rate_url`, `rate_field`, `status`, `update_time`, `create_time`, `create_by`, `update_by`, `main_token`, `decimals`, `withdraw_decimals`, `withdraw_min`, `withdraw_fixed_amount`) VALUES (1597528554238193665, 'usdt', 'TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t', 'TRON', 'trc20', NULL, NULL, 2, '2022-11-29 17:51:03', '2022-11-29 17:51:03', '88888888', '88888888', 0, 6, 6,10, 1);

