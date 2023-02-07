CREATE TABLE `account_user_transfer`
(
    `id`                bigint         NOT NULL,
    `transfer_uid`      bigint         NOT NULL COMMENT '转帐id',
    `receive_uid`       bigint         NOT NULL COMMENT '收钱id',
    `coin`              varchar(20)    NOT NULL COMMENT '币种',
    `amount`            decimal(20, 8) NOT NULL COMMENT '金额',
    `create_time`       datetime       NOT NULL,
    `transfer_order_no` varchar(50) DEFAULT NULL COMMENT '转帐对应订单号',
    `external_pk`       bigint      DEFAULT NULL COMMENT '外部主键值',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB;