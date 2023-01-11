ALTER TABLE `service_fee`
    DROP INDEX `uq_index`,
    ADD UNIQUE INDEX `uq_index` (`create_time` DESC, `coin` ASC, `type` ASC, `network`) USING BTREE;
INSERT INTO `coin_base` (`name`, `weight`, `logo`, `create_time`, `create_by`, `update_time`, `update_by`, `rate_url`,
                         `rate_field`, `main_token`, `display`)
VALUES ('matic', 100,
        'https://assure-financial-jp.s3-accelerate.amazonaws.com/file/52722b2e-d755-4f9f-8f8b-e6d970d5252c.png',
        '2023-01-08 16:59:15', NULL, '2023-01-08 16:59:18', NULL, NULL, NULL, 1, 1);
INSERT INTO `coin_base` (`name`, `weight`, `logo`, `create_time`, `create_by`, `update_time`, `update_by`, `rate_url`,
                         `rate_field`, `main_token`, `display`)
VALUES ('trx', 100,
        'https://assure-financial-jp.s3-accelerate.amazonaws.com/file/f8a54210-1d2f-4542-96fc-e6e0d51b02f0.png', NULL,
        NULL, NULL, NULL, NULL, NULL, 0, 1);

INSERT INTO `coin` (`id`, `name`, `contract`, `chain`, `network`, `rate_url`, `rate_field`, `status`, `update_time`,
                    `create_time`, `create_by`, `update_by`, `main_token`, `decimals`, `withdraw_decimals`,
                    `withdraw_min`, `withdraw_fixed_amount`)
VALUES (1602544441464139778, 'eth', '', 'ARBITRUM', 'erc20_arbitrum', NULL, NULL, 2, '2023-01-08 16:56:24',
        '2023-01-08 16:56:22', NULL, NULL, 1, 18, 8, 0.00000000, 0.00000000);
INSERT INTO `coin` (`id`, `name`, `contract`, `chain`, `network`, `rate_url`, `rate_field`, `status`, `update_time`,
                    `create_time`, `create_by`, `update_by`, `main_token`, `decimals`, `withdraw_decimals`,
                    `withdraw_min`, `withdraw_fixed_amount`)
VALUES (9223372036854775806, 'eth', '', 'OPTIMISTIC', 'erc20_optimistic', NULL, NULL, 2, '2023-01-07 14:09:10',
        '2023-01-07 14:09:14', NULL, NULL, 1, 18, 8, 0.00000100, 0.00000100);
INSERT INTO `coin` (`id`, `name`, `contract`, `chain`, `network`, `rate_url`, `rate_field`, `status`, `update_time`,
                    `create_time`, `create_by`, `update_by`, `main_token`, `decimals`, `withdraw_decimals`,
                    `withdraw_min`, `withdraw_fixed_amount`)
VALUES (1600052748291899395, 'trx', '', 'TRON', 'trc20', NULL, NULL, 2, '2023-01-10 15:37:18', '2023-01-08 11:14:52',
        NULL, '超级管理员', 1, 6, 8, 0.10000000, 0.10000000);
INSERT INTO `coin` (`id`, `name`, `contract`, `chain`, `network`, `rate_url`, `rate_field`, `status`, `update_time`,
                    `create_time`, `create_by`, `update_by`, `main_token`, `decimals`, `withdraw_decimals`,
                    `withdraw_min`, `withdraw_fixed_amount`)
VALUES (9223372036854775807, 'matic', '', 'POLYGON', 'erc20_polygon', NULL, NULL, 2, '2023-01-10 15:39:56',
        '2023-01-08 17:00:33', NULL, '超级管理员', 1, 18, 8, 0.00000000, 0.10000000);
INSERT INTO `config` (`name`, `value`)
VALUES ('uutoken_host', 'https://uutoken.giantdt.com');
INSERT INTO `config` (`name`, `value`)
VALUES ('arbitrum_trigger_address', '0xd6c2bb2af98c4b83868a2a4c5d11ab8339a3bb15');
INSERT INTO `config` (`name`, `value`)
VALUES ('op_trigger_address', '0xd6c2bb2af98c4b83868a2a4c5d11ab8339a3bb15');
INSERT INTO `config` (`name`, `value`)
VALUES ('polygon_trigger_address', '0xd6c2bb2af98c4b83868a2a4c5d11ab8339a3bb15');
INSERT INTO `config` (`name`, `value`)
VALUES ('arbitrum_main_wallet_address', '0x85aa19e95b3647e7f324ba6a554c54c313a979e2');
INSERT INTO `config` (`name`, `value`)
VALUES ('op_main_wallet_address', '0x85aa19e95b3647e7f324ba6a554c54c313a979e2');
INSERT INTO `config` (`name`, `value`)
VALUES ('polygon_main_wallet_address', '0x85aa19e95b3647e7f324ba6a554c54c313a979e2');

DELETE
FROM `config`
WHERE `name` IN ('usdc_bep20_withdraw_fixed_amount',
                 'usdc_bep20_withdraw_min_amount',
                 'usdc_bep20_withdraw_rate',
                 'usdc_erc20_withdraw_fixed_amount',
                 'usdc_erc20_withdraw_min_amount',
                 'usdc_erc20_withdraw_rate',
                 'usdc_trc20_withdraw_fixed_amount',
                 'usdc_trc20_withdraw_min_amount',
                 'usdc_trc20_withdraw_rate',
                 'usdt_bep20_withdraw_fixed_amount',
                 'usdt_bep20_withdraw_min_amount',
                 'usdt_bep20_withdraw_rate',
                 'usdt_erc20_withdraw_fixed_amount',
                 'usdt_erc20_withdraw_min_amount',
                 'usdt_erc20_withdraw_rate',
                 'usdt_trc20_withdraw_fixed_amount',
                 'usdt_trc20_withdraw_min_amount',
                 'usdt_trc20_withdraw_rate',
                 'bnb_withdraw_fixed_amount',
                 'bnb_withdraw_min_amount',
                 'bnb_withdraw_rate',
                 'eth_withdraw_fixed_amount',
                 'eth_withdraw_min_amount',
                 'eth_withdraw_rate');

CREATE TABLE `occasional_address`
(
    `id`          bigint       NOT NULL,
    `address_id`  bigint       NOT NULL COMMENT 'address表的id',
    `chain`       varchar(20)  NOT NULL,
    `address`     varchar(128) NOT NULL,
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `registered`  tinyint      NOT NULL DEFAULT '0',
    `retry_count` int          NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `address_chain_idx` (`address`, `chain`),
    UNIQUE KEY `aid_chain_idx` (`address_id`, `chain`) USING BTREE
) ENGINE = InnoDB;

CREATE TABLE `address_mnemonic`
(
    `id`          bigint        NOT NULL,
    `address_id`  bigint        NOT NULL,
    `mnemonic`    varchar(1024) NOT NULL,
    `create_time` datetime DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uid_idx` (`address_id`)
) ENGINE = InnoDB;

DELETE
FROM `financial_board_product`
where `id` in (1747404088959323227, 1747494685917026688, 1747585282921996414, 1747675879852420947, 1747857073780343472,
             1747947670746496013, 1747947670754532199, 1748038267713546989, 1748128864678032724, 1748310058622585969,
             1748491252574278874, 1748581849512287449, 1748944237399417475, 1749034834342309673, 1749125431325249887,
             1749216028272324849, 1749216028281219447, 1749306625237663995,
             1749397222201982806, 1750121997982034728, 1753292891823283621);