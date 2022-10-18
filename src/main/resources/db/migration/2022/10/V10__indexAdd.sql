ALTER TABLE `financial`.`order_advance`
    ADD UNIQUE INDEX `uniqe`(`network`, `txid`) COMMENT '网络hash唯一主键';