create table address
(
    id          bigint                                   not null comment '主键'
        primary key,
    create_time datetime                                 not null comment '创建时间',
    uid         bigint                                   not null comment '用户id',
    type        enum ('normal')                          not null comment '地址类型',
    eth         varchar(60)                              null comment '以太坊地址eth / usdt-erc20',
    tron        varchar(60)                              null comment '波场地址 tron / usdt-trc20',
    bsc         varchar(60)                              null,
    btc         varchar(60)                              null,
    constraint address_bsc_uindex
        unique (bsc),
    constraint address_tron_uindex
        unique (tron),
    constraint eth
        unique (eth),
    constraint uid
        unique (uid, type)
)
    comment '用户的云账户充值地址表';


