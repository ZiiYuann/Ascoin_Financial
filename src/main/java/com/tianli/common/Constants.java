package com.tianli.common;

import com.google.gson.Gson;
import com.tianli.chain.enums.ChainType;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
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

    // public EOS7WCxFVheywM5R4zZd51Ksrjp7ngLMkR45BXPCRRtqjWFKh3htn
    public static final String RED_ENVELOPE_PRIVATE_KEY = "5KfxvPXfXCMVBtGARPqKNpw1jhkacQa3WBoJ32VqF4AcQytjeuB";


    /**
     * 获取链上交易详情API
     */
    public static final String TRANSACTION_DETAILS_HOST = "https://www.twallet.pro";
    public static final String TRANSACTION_DETAILS_PATH = "/api/message/detail";

    public static final String WALLET_REDIS_LOCK_KEY = "WALLET_REDIS_LOCK_KEY";

    // 红包加密password
    public static final String RED_SECRET_KEY = "TPtvrD;=FdPN.EHpn,&_Ppaz#!7ZX7";
    // 红包加密salt
    public static final byte[] RED_SALT = new byte[]{-19, 110, -73, -101, -1, 36, 81, 18};

    public static final Gson GSON = new Gson();

    public static final ExecutorService COMPLETABLE_FUTURE_EXECUTOR = new ThreadPoolExecutor(8, 16, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1024), new ThreadFactory() {
        private AtomicInteger id = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "COMPLETABLE_FUTURE_EXECUTOR_" + id.getAndIncrement());
        }
    });

    public static final HashMap<Integer, List<ChainType>> CHAIN_TYPE_VERSION = new HashMap<>();

    static {
        CHAIN_TYPE_VERSION.put(0, List.of(ChainType.BSC, ChainType.ETH, ChainType.TRON));
    }
}
