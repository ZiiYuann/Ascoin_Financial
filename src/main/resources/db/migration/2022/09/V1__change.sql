DROP INDEX idx_order_id ON `borrow_interest_record`;
ALTER TABLE `borrow_interest_record` ADD UNIQUE KEY `uk_order_id` (`order_id`,`interest_accrual_time`);