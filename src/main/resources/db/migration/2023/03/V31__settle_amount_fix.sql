UPDATE account_balance
set balance = balance + 220191.18169,
    remain  = remain + 220191.18169
where coin = 'usdt'
  and uid = 1744837314334425090
  and id = 1744838237678368073;

UPDATE `account_balance_operation_log`
SET `amount`      = 220191.18169,
    `create_time` = '2023-03-26 11:26:01',
    `balance`     = 14.59570075,
    `freeze`      = '0.00000000',
    `remain`      = 14.59570075,
    `des`         = '修复结算产生的bug 1760993634094563630日志无效'
WHERE `id` = 1760993634424487844;