alter table order_advance
    collate = utf8mb4_general_ci;
alter table order_advance
    modify term varchar(20) collate utf8mb4_general_ci null comment '限期';