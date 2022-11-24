ALTER TABLE `order_reward_record`
    DROP INDEX `order_reward_record_order_id_uindex`,
    ADD UNIQUE INDEX `uq_index`(`order_id` ASC, `type`) USING BTREE;