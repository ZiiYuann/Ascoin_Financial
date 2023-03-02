CREATE TABLE `push_message`
(
    `id`          bigint       NOT NULL,
    `uid`         bigint       NOT NULL,
    `title`       varchar(255) NOT NULL,
    `content`     text,
    `create_time` datetime     NOT NULL,
    KEY `normal_index` (`uid`, `create_time` DESC)
) ENGINE = InnoDB;