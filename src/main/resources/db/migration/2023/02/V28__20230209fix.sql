
UPDATE `order` set create_time = date_sub(create_time,interval 1 hour) where type = 'transaction_reward' ;