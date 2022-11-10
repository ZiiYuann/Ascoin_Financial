alter table account_balance
    add constraint uq_index
        unique (uid, coin);

create index `index`
    on exception_msg (id desc);

alter table financial_product
    add recommend tinyint(1) default 0 null comment '是否推荐';

alter table financial_record
    modify hold_amount decimal(20, 8) not null comment '持有金额';

alter table financial_record
    modify wait_amount decimal(20, 8) default 0.00000000 null comment '待记利息金额' after update_time;

alter table financial_record
    modify income_amount decimal(20, 8) default 0.00000000 null comment '记录利息金额' after wait_amount;