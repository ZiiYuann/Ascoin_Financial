ALTER TABLE `order`
    ADD COLUMN `update_time` datetime NULL AFTER `complete_time`;

UPDATE `order`
SET `update_time` = `complete_time`;