create table account_balance
(
    id      bigint         not null
        primary key,
    uid     bigint         not null,
    coin    varchar(20)    not null comment '币别',
    balance decimal(20, 8) not null comment '余额',
    freeze  decimal(20, 8) not null comment '冻结',
    remain  decimal(20, 8) not null comment '剩余',
    logo    varchar(255)   null
)
    comment '用户余额表

';

create table account_balance_operation_log
(
    id                 bigint         null,
    uid                bigint         null,
    account_balance_id bigint         null,
    charge_type        varchar(20)    null,
    coin               varchar(20)    null,
    network            varchar(20)    null,
    log_type           varchar(20)    null,
    order_no           varchar(60)    null,
    amount             decimal(16, 8) null,
    create_time        datetime       null,
    balance            varchar(16)    null,
    freeze             varchar(16)    null,
    remain             decimal(16, 8) null,
    des                varchar(255)   null
)
    comment '用户余额操作日志';

create table address
(
    id          bigint          not null comment '主键'
        primary key,
    create_time datetime        not null comment '创建时间',
    uid         bigint          not null comment '用户id',
    type        enum ('normal') not null comment '地址类型',
    eth         varchar(60)     null comment '以太坊地址eth / usdt-erc20',
    tron        varchar(60)     null comment '波场地址 tron / usdt-trc20',
    bsc         varchar(60)     null,
    btc         varchar(60)     null,
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

create table borrow_coin_config
(
    id                   bigint         null,
    coin                 varchar(20)    null comment '币种',
    minimum_borrow       decimal(20, 8) null comment '最小可借',
    maximum_borrow       decimal(20, 8) null comment '最大可借',
    annual_interest_rate decimal(20, 8) null comment '年利率',
    create_time          datetime       null comment '创建时间',
    constraint uk_coin
        unique (coin)
)
    comment '借币数据配置';

create table borrow_coin_order
(
    id                  bigint                            null comment 'ID',
    uid                 bigint                            null comment '用户ID',
    borrow_coin         varchar(20)                       null comment '借出币种',
    logo                varchar(1024)                     null comment '币种图标',
    borrow_capital      decimal(20, 8)                    null comment '借出本金',
    cumulative_interest decimal(20, 8) default 0.00000000 null comment '累计利息',
    repay_amount        decimal(20, 8) default 0.00000000 null comment '已还金额',
    wait_repay_capital  decimal(20, 8) default 0.00000000 null comment '待还本金',
    wait_repay_interest decimal(20, 8) default 0.00000000 null comment '待还利息',
    pledge_coin         varchar(20)                       null comment '质押币种',
    pledge_amount       decimal(20, 8) default 0.00000000 null comment '质押金额',
    pledge_rate         decimal(20, 8)                    null comment '当前质押率',
    pledge_status       int                               null comment '质押状态',
    status              int                               null comment '订单状态',
    borrow_time         datetime                          null comment '借出时间',
    borrow_duration     bigint                            null comment '借款时长',
    settlement_time     datetime                          null comment '结算时间',
    create_time         datetime                          null comment '创建时间'
)
    comment '借币订单';

create table borrow_interest_record
(
    id                    bigint         null comment 'ID',
    order_id              bigint         null comment '订单ID',
    coin                  varchar(20)    null comment '币种',
    wait_repay_capital    decimal(20, 8) null comment '待付本金',
    wait_repay_interest   decimal(20, 8) null comment '待付利息',
    interest_accrual      decimal(20, 8) null comment '本次计息',
    annual_interest_rate  decimal(20, 8) null comment '年利率',
    interest_accrual_time datetime       null comment '计息时间',
    create_time           datetime       null comment '创建日期'
)
    comment '借币利息记录';

create index idx_order_id
    on borrow_interest_record (order_id, interest_accrual_time);

create table borrow_order_num_daily
(
    id               bigint null comment 'ID',
    order_num        int    null comment '订单数',
    statistical_date date   null comment '统计日期',
    constraint uk_statistical_date
        unique (statistical_date)
)
    comment '计息中订单每日统计';

create table borrow_pledge_coin_config
(
    id                      bigint         null comment 'id',
    coin                    varchar(20)    null comment '币种',
    initial_pledge_rate     decimal(20, 8) null comment '初始质押率',
    warn_pledge_rate        decimal(20, 8) null comment '警告质押率',
    liquidation_pledge_rate decimal(20, 8) null comment '强平质押率',
    create_time             datetime       null comment '创建时间',
    constraint uk_coin
        unique (coin)
)
    comment '质押币种配置';

create table borrow_pledge_record
(
    id          bigint         null comment 'ID',
    order_id    bigint         null comment '订单ID',
    coin        varchar(20)    null comment '币种',
    amount      decimal(20, 8) null comment '数量',
    type        int            null comment '类型',
    pledge_time datetime       null comment '调整时间',
    create_time datetime       null comment '创建时间'
)
    comment '借币质押记录';

create index idx_order_id
    on borrow_pledge_record (order_id);

create table borrow_repay_record
(
    id                    bigint         null comment 'ID',
    order_id              bigint         null comment '订单ID',
    coin                  varchar(20)    null comment '币别',
    repay_amount          decimal(20, 8) null comment '还款金额',
    repay_capital         decimal(20, 8) null comment '本金还款',
    repay_interest        decimal(20, 8) null comment '利息还款',
    release_pledge_amount decimal(20, 8) null comment '释放质押物',
    status                int            null comment '状态',
    type                  int            null comment '类型',
    repay_time            datetime       null comment '还款时间',
    create_time           datetime       null comment '创建时间'
)
    comment '借币还款记录';

create index idx_order_id
    on borrow_repay_record (order_id);

create table chain_callback_log
(
    id          bigint auto_increment
        primary key,
    type        varchar(20) null,
    chain       varchar(20) null,
    log         text        null,
    status      varchar(20) null,
    msg         text        null,
    create_time datetime    null
)
    comment '数据中心推送回调日志';

create table config
(
    name  varchar(100)  not null
        primary key,
    value varchar(3000) not null
)
    comment '配置表';

create table financial_board_product
(
    id                    bigint         not null
        primary key,
    redeem_amount         decimal(16, 8) not null comment '赎回金额',
    settle_amount         decimal(16, 8) not null comment '结算金额',
    purchase_amount       decimal(16, 8) not null comment '申购金额',
    transfer_amount       decimal(16, 8) not null comment '转存金额',
    create_time           datetime       not null comment '创建时间',
    income                decimal(20, 8) not null comment '累计收益',
    current_product_count bigint         not null comment '活期产品持有数量',
    fixed_product_count   bigint         not null comment '定期产品持有数量',
    total_product_count   bigint         not null comment '总产品持有数量',
    hold_user_count       bigint         not null comment '持有用户数量'
)
    comment '理财产品展板表';

create table financial_board_wallet
(
    id                   bigint         not null
        primary key,
    recharge_amount      decimal(16, 8) not null comment '充值金额',
    withdraw_amount      decimal(16, 8) not null comment '提币金额',
    active_wallet_count  bigint         not null comment '激活云钱包数量',
    create_time          date           not null,
    total_service_amount decimal(20, 8) not null,
    usdt_service_amount  decimal(20, 8) not null
)
    comment '理财钱包展板表';

create table financial_income_accrue
(
    id            varchar(30)    not null
        primary key,
    uid           bigint         null,
    record_id     bigint         null comment 'financial_record 主键',
    coin          varchar(20)    null,
    income_amount decimal(16, 8) null comment '收益',
    create_time   datetime       null,
    update_time   datetime       null,
    constraint unique_index1
        unique (uid, record_id)
)
    comment '理财收益合计表';

create table financial_income_daily
(
    id            varchar(30)    not null
        primary key,
    uid           bigint         null,
    record_id     bigint         null comment 'financial_record 主键',
    income_amount decimal(20, 8) null comment '收益',
    create_time   datetime       null,
    finish_time   datetime       null
)
    comment '理财收益单日表';

create table financial_product
(
    id                   bigint         null,
    logo                 varchar(255)   null comment 'logo地址',
    coin                 varchar(20)    null comment '币别',
    type                 varchar(15)    null comment '产品类型',
    term                 varchar(15)    null comment '申购期限',
    status               varchar(15)    null comment '产品状态',
    name                 varchar(20)    null comment '产品名称',
    name_en              varchar(30)    null,
    rate                 decimal(10, 4) null comment '年利率',
    risk_type            varchar(15)    null comment '风险类型',
    business_type        varchar(15)    null comment '运营类型',
    person_quota         decimal(20, 8) null comment '个人额度',
    total_quota          decimal(20, 8) null comment '总额度',
    create_time          datetime       null,
    update_time          datetime       null,
    limit_purchase_quota decimal(20, 8) null comment '最低申购额度',
    deleted              tinyint(1)     null comment '删除状态'
)
    comment '理财产品表';

create table financial_record
(
    id                bigint         not null
        primary key,
    uid               bigint         not null,
    product_id        bigint         not null,
    product_name      varchar(20)    not null comment '产品名称（冗余）',
    product_name_en   varchar(30)    not null comment '产品名称英文（冗余）',
    product_type      varchar(20)    not null comment '产品类型（冗余）',
    risk_type         varchar(20)    not null comment '风险类型（冗余）',
    coin              varchar(20)    not null comment '币别（冗余）',
    product_term      varchar(255)   not null comment '产品期限（冗余）',
    rate              decimal(6, 4)  not null comment '年化利率（冗余）',
    logo              varchar(255)   not null comment 'logo（冗余）',
    business_type     varchar(20)    not null comment '运营类型（冗余）',
    status            varchar(20)    not null comment '记录状态 PROCESS 进行中 SUCCESS 完成',
    hold_amount       decimal(16, 8) not null comment '持有数额',
    purchase_time     datetime       null comment '申购时间',
    redeem_time       datetime       null comment '赎回时间（最新一次）',
    end_time          datetime       null comment '结束时间（结算时间）',
    start_income_time datetime       null comment '开始计息时间',
    auto_renewal      tinyint(1)     null comment '是否自动续费',
    update_time       datetime       null comment '更新时间'
)
    comment '理财申购记录表';

create table `order`
(
    id             bigint                                     not null,
    uid            bigint                                     not null,
    type           varchar(20)                                not null comment '订单类型',
    status         varchar(20)                                not null comment '订单状态',
    order_no       varchar(60)                                not null comment '订单号',
    coin           varchar(15)                                not null comment '币别',
    service_amount decimal(20, 8) unsigned default 0.00000000 null comment '手续费',
    amount         decimal(20, 8)                             not null comment '金额',
    related_id     bigint                                     null comment '关联资源id',
    reviewer_id    varchar(20)                                null comment '审核记录id',
    create_time    datetime                                   not null comment '创建时间',
    complete_time  datetime                                   null comment '完成时间'
)
    comment '订单表';

create index complete_time
    on `order` (complete_time desc, type asc, amount asc);

create index uid
    on `order` (uid, type, status);

create table order_charge_info
(
    id           bigint         not null
        primary key,
    uid          bigint         null,
    network      varchar(20)    null,
    coin         varchar(20)    not null comment '币别包装类型',
    fee          decimal(30, 8) not null comment '总金额',
    service_fee  decimal(30, 8) null comment '手续费',
    real_fee     decimal(30)    not null comment '真实金额',
    miner_fee    decimal(30, 8) null comment '矿工费',
    txid         varchar(100)   null,
    from_address varchar(60)    null,
    to_address   varchar(60)    null,
    create_time  datetime       not null
)
    comment '订单附录（区块链信息表）';

create table order_review
(
    id          bigint       null,
    rid         bigint       null comment '审核人id',
    remarks     varchar(255) null comment '审核备注',
    status      varchar(255) null comment '审核状态(冗余)',
    create_time datetime     null comment '审核时间'
)
    comment '订单审核记录表';

create table user_info
(
    id         bigint       null,
    address    varchar(100) null,
    sign_chain varchar(255) null
);

create table wallet_imputation
(
    id          bigint auto_increment
        primary key,
    uid         bigint         null,
    address_id  bigint         null comment '地址表id',
    amount      decimal(30, 8) not null comment '归集金额',
    address     varchar(255)   null,
    network     varchar(20)    null comment '网络',
    coin        varchar(20)    null comment '币别',
    status      varchar(20)    null comment '归集状态',
    create_time datetime       null,
    update_time datetime       null
)
    comment '归集信息表';

create table wallet_imputation_log
(
    id           bigint         not null
        primary key,
    txid         varchar(100)   null,
    amount       decimal(20, 8) not null comment '归集金额',
    from_address varchar(255)   null,
    network      varchar(20)    null comment '网络',
    coin         varchar(20)    null comment '币别',
    status       varchar(20)    null comment '归集状态',
    create_time  datetime       null
)
    comment '归集记录表';

create table wallet_imputation_log_appendix
(
    id           bigint auto_increment
        primary key,
    txid         varchar(100)   null,
    amount       decimal(20, 8) not null comment '归集金额',
    to_address   varchar(60)    null,
    from_address varchar(60)    null,
    network      varchar(20)    null
)
    comment '归集记录附录表
';

create table wallet_imputation_temporary
(
    id          bigint auto_increment
        primary key,
    uid         bigint         null,
    amount      decimal(20, 8) not null comment '归集金额',
    `from`      varchar(255)   null,
    network     varchar(20)    null comment '网络',
    coin        varchar(20)    null comment '币别',
    create_time datetime       null
)
    comment '归集信息临时表';

-- 固定手续费
INSERT INTO `financial`.`config` (`name`, `value`) VALUES ('usdt_trc20_withdraw_fixed_amount', '1');
INSERT INTO `financial`.`config` (`name`, `value`) VALUES ('usdt_bep20_withdraw_fixed_amount', '1');
INSERT INTO `financial`.`config` (`name`, `value`) VALUES ('usdt_erc20_withdraw_fixed_amount', '1');

-- 最小提币金额
INSERT INTO `financial`.`config` (`name`, `value`) VALUES ('usdt_erc20_withdraw_min_amount', '10');
INSERT INTO `financial`.`config` (`name`, `value`) VALUES ('usdt_trc20_withdraw_min_amount', '10');
INSERT INTO `financial`.`config` (`name`, `value`) VALUES ('usdt_bep20_withdraw_min_amount', '10');

-- 手续费率
INSERT INTO `financial`.`config` (`name`, `value`) VALUES ('usdt_trc20_withdraw_rate', '0');
INSERT INTO `financial`.`config` (`name`, `value`) VALUES ('usdt_erc20_withdraw_rate', '0');
INSERT INTO `financial`.`config` (`name`, `value`) VALUES ('usdt_bep20_withdraw_rate', '0');

INSERT INTO financial.config (name, value) VALUES ('data_center_url_register_path', 'http://nft-data-center.abctest.pro/api/push/callbackAddress/register');
INSERT INTO financial.config (name, value) VALUES ('system_url_path_prefix', 'http://financial.abctest.pro');