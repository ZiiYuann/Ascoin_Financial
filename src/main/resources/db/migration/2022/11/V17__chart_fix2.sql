alter table fund_transaction_record
    modify type varchar(20) collate utf8mb4_general_ci null comment '类型';