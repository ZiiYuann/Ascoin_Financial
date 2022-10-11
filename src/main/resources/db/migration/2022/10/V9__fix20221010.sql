-- 	本地申购基金类型修复
UPDATE `order`
SET type = 'fund_purchase'
WHERE order_no IN (
    SELECT concat('APU', oa.id)
    FROM order_advance oa
             LEFT JOIN financial_product pro ON oa.product_id = pro.id
    WHERE pro.type = 'fund'
      AND oa.finish = 1
)
  AND type = 'purchase';


# UPDATE financial_board_product f,
#     (
#         SELECT sum(amount) AS amount,
#                t
#         FROM (SELECT amount, LEFT(complete_time, 10) AS t
#               FROM `order`
#               WHERE type = 'purchase' AND `status` = 'chain_success') AS table1
#         GROUP BY t
#     ) a
# SET f.purchase_amount = a.amount
# WHERE f.create_time = a.t;