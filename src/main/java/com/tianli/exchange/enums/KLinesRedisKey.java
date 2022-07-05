package com.tianli.exchange.enums;

/**
 * @author lzy
 * @date 2022/6/13 10:29
 */
public class KLinesRedisKey {
    /**
     * 分钟k线
     */
    public static final String CURRENT_K_LINE_KEY = "current_opening_time:{}";

    public static final String CURRENT_K_LINE_LOCK = "current_symbol_lock_opening_time:{}";


    public static final String CURRENT_M_K_LINE_UPDATE_LOCK = "current_symbol:{}_lock_opening_time:{}";

    public static final String CURRENT_5M_K_LINE_KEY = "current_5m_opening_time:{}";

    public static final String CURRENT_15M_K_LINE_KEY = "current_15m_opening_time:{}";

    public static final String CURRENT_30M_K_LINE_KEY = "current_30m_opening_time:{}";

    public static final String CURRENT_60M_K_LINE_KEY = "current_60m_opening_time:{}";

    /**
     * 日k线
     */
    public static final String CURRENT_DAY_K_LINE_KEY = "current_day_opening_time:{}";

    public static final String CURRENT_DAY_K_LINE_LOCK = "current_day_k_line_lock_opening_time:{}";

    public static final String CURRENT_SYMBOL_DAY_K_LINE_LOCK = "current_symbol:{}_day_k_line_lock_opening_time:{}";

    public static final String AUTO_GENERATE_LOCK = "auto_generate_lock";

    public static final String PUSH_K_LINES_LOCK = "push_k_lines_lock";
}
