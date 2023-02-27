ALTER TABLE `red_envelope` MODIFY COLUMN `flag` varchar (255) NOT NULL DEFAULT '' COMMENT '红包唯一标示符号 群号、或者hash啥的' AFTER `short_uid`;
ALTER TABLE `red_envelope`
    ADD COLUMN `channel` varchar(20) NOT NULL DEFAULT 'CHAT' AFTER `coin`;
ALTER TABLE `red_envelope`
    ADD COLUMN `receive_amount` decimal(20, 8) NOT NULL DEFAULT 0.00000000 COMMENT '已经领取金额' AFTER `receive_num`;

ALTER TABLE `red_envelope_spilt_get_record`
    ADD COLUMN `exchange_code` varchar(50) NULL DEFAULT NULL COMMENT '兑换码' AFTER `device_number`;

CREATE TABLE `red_envelope_config`
(
    `coin`         varchar(10)    NOT NULL COMMENT '币别',
    `channel`      varchar(255)   NOT NULL COMMENT '渠道',
    `num`          int NULL DEFAULT NULL COMMENT '数量',
    `limit_amount` decimal(20, 8) NOT NULL COMMENT '限制总金额',
    `min_amount`   decimal(20, 8) NOT NULL COMMENT '最小金额',
    `create_by`    varchar(20)    NOT NULL,
    `create_time`  datetime       NOT NULL,
    `update_by`    varchar(20)    NOT NULL,
    `update_time`  datetime NULL DEFAULT NULL,
    UNIQUE INDEX `uq_index`(`coin` ASC, `channel` ASC) USING BTREE
) ENGINE = InnoDB;
