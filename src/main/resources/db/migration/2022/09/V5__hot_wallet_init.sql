CREATE TABLE `hot_wallet_detailed` (
                                       `id` bigint NOT NULL AUTO_INCREMENT,
                                       `uid` bigint DEFAULT NULL,
                                       `amount` decimal(30,8) NOT NULL,
                                       `coin` varchar(20) NOT NULL,
                                       `chain` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                                       `from_address` varchar(255) DEFAULT NULL,
                                       `to_address` varchar(255) NOT NULL,
                                       `hash` varchar(255) NOT NULL,
                                       `type` varchar(20) NOT NULL,
                                       `remarks` varchar(500) DEFAULT NULL,
                                       `create_time` datetime NOT NULL,
                                       PRIMARY KEY (`id`),
                                       KEY `defalut_index` (`type`,`create_time` DESC) COMMENT '默认索引'
) ENGINE=InnoDB AUTO_INCREMENT=1742482570772645091 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


INSERT INTO `hot_wallet_detailed` (`uid`, `amount`, `coin`, `chain`, `from_address`, `to_address`, `hash`,
                                               `type`, `remarks`, `create_time`)
    (SELECT o.uid,
            c.fee - c.service_fee,
            o.coin,
            CASE c.network
                WHEN 'trc20' THEN 'TRON'
                WHEN 'bep20' THEN 'BSC'
                WHEN 'erc20' THEN 'ETH'
                END as `chain`,
            c.from_address,
            c.to_address,
            c.txid,
            'user_withdraw',
            '',
            o.complete_time

     FROM `order` o
              INNER JOIN order_charge_info c ON o.related_id = c.id
     WHERE o.type IN ('withdraw')
       AND o.`status` = 'chain_success');

INSERT INTO `hot_wallet_detailed` (`uid`, `amount`, `coin`, `chain`, `from_address`, `to_address`, `hash`,
                                               `type`, `remarks`, `create_time`)
SELECT 0,
       log.amount,
       log.coin,
       CASE
           network
           WHEN 'trc20' THEN
               'TRON'
           WHEN 'bep20' THEN
               'BSC'
           WHEN 'erc20' THEN
               'ETH'
           END AS `chain`,
       '',
       address.`value`,
       log.txid,
       'imputation',
       '',
       create_time
FROM (SELECT *, CASE network WHEN 'trc20' THEN 'TRON' WHEN 'bep20' THEN 'BSC' WHEN 'erc20' THEN 'ETH' END AS `chain`
      FROM wallet_imputation_log
      WHERE `status` = 'success') AS log
         LEFT JOIN (
    SELECT 'BSC' AS 'chain',
           `value`
    FROM config
    WHERE `name` = 'bsc_main_wallet_address'
    UNION
    SELECT 'TRON' AS 'chain',
           `value`
    FROM config
    WHERE `name` = 'tron_main_wallet_address'
    UNION
    SELECT 'ETH' AS 'chain',
           `value`
    FROM config
    WHERE `name` = 'eth_main_wallet_address'
) AS address ON log.`chain` = address.`chain`;

