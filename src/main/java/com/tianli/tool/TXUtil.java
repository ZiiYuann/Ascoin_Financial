package com.tianli.tool;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tianli.chain.enums.ChainType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.Charset;
import java.util.Objects;

/**
 * @author lzy
 * @since  2022/4/27 16:10
 */
@Slf4j
public class TXUtil {

    private static final String CHAIN_TYPE = "chainType";

    private TXUtil() {
        throw new IllegalStateException("Utility class");
    }


    public static Long getBlockNumber(String url, ChainType txChainTypeEnum) {
        Long blockNumber = -1L;
        if (StringUtils.isBlank(url) || Objects.isNull(txChainTypeEnum)) {
            return blockNumber;
        }
        try {
            String result = HttpUtil.get(url + "?" + URLUtil.buildQuery(MapTool.Map().put(CHAIN_TYPE, txChainTypeEnum.name()), Charset.defaultCharset()));
            JSONObject jsonObject = JSONUtil.parseObj(result);
            if (ObjectUtil.isNotNull(jsonObject)) {
                Object data = jsonObject.get("data");
                if (ObjectUtil.isNotNull(data)) {
                    blockNumber = Convert.toLong(data);
                }
            }
        } catch (Exception e) {
            log.error("获取最新区块报错:{}", e.toString());
        }
        return blockNumber;
    }

    /**
     * 内部交易查询
     *
     * @param txChainTypeEnum 链
     * @param txHash 归集哈希
     */
    public static Object insiderTrading(String url, ChainType txChainTypeEnum, String txHash) {
        if (StringUtils.isBlank(url) || ObjectUtil.isNull(txChainTypeEnum) || StringUtils.isBlank(txHash)) {
            return null;
        }
        String result = HttpUtil.get(url + "?" + URLUtil.buildQuery(MapTool.Map()
                .put(CHAIN_TYPE, txChainTypeEnum.name())
                .put("txhash", txHash), Charset.defaultCharset()));
        JSONObject jsonObject = JSONUtil.parseObj(result);
        return jsonObject.get("data");
    }

}
