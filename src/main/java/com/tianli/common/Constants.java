package com.tianli.common;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author wangqiyun
 * @Date 2019/3/16 4:10 PM
 */
public class Constants {
    public static final String phone_verify_regex = "^\\d+$";

    public static final String email_verify_regex = "^[a-z0-9A-Z]+[- | a-z0-9A-Z . _]+@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-z]{2,}$";

    public static final String password_verify_regex = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$";

    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static final DateTimeFormatter dateTimeFormatterFraction = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    public static final DateTimeFormatter standardDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static final DateTimeFormatter standardDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final String defaultUserNick = "Financial";

    /**
     * 获取链上交易详情API
     */
    public static final String TRANSACTION_DETAILS_HOST = "https://www.twallet.pro";
    public static final String TRANSACTION_DETAILS_PATH = "/api/message/detail";

    public static final String WALLET_REDIS_LOCK_KEY = "WALLET_REDIS_LOCK_KEY";

    public static final ExecutorService COMPLETABLE_FUTURE_EXECUTOR = new ThreadPoolExecutor(8, 16, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1024), new ThreadFactory() {
        private AtomicInteger id = new AtomicInteger(1);
        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "COMPLETABLE_FUTURE_EXECUTOR_" + id.getAndIncrement());
        }
    });
}
