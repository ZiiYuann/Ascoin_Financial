ALTER TABLE `order`
DROP INDEX `unique_order_no`,
DROP INDEX `orderNoIndex`,
ADD UNIQUE INDEX `uqIndex`(`uid`, `type`, `order_no`),
ADD INDEX `orderNoIndex`(`order_no`, `uid`);


