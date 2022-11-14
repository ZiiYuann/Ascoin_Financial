alter table financial.fund_transaction_record
    collate = utf8mb4_general_ci;
alter table financial.fund_transaction_record
    modify product_name varchar(255) collate utf8mb4_general_ci null comment '产品名称';
alter table financial.fund_transaction_record
    modify coin varchar(20) collate utf8mb4_general_ci null comment '币别';