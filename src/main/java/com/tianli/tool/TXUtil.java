package com.tianli.tool;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tianli.wallet.enums.TXChainTypeEnum;
import com.tianli.wallet.vo.TXBlockQueryVo;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;

/**
 * @author lzy
 * @date 2022/4/27 16:10
 */
@Slf4j
public class TXUtil {


    public static Long getBlockNumber(String url, TXChainTypeEnum txChainTypeEnum) {
        Long blockNumber = -1L;
        if (StrUtil.isBlank(url) || Objects.isNull(txChainTypeEnum)) {
            return blockNumber;
        }
        try {
            String result = HttpUtil.get(url + "?" + URLUtil.buildQuery(MapTool.Map().put("chainType", txChainTypeEnum.name()), Charset.defaultCharset()));
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
     * 根据指定的区块查询
     *
     * @param url
     * @param txChainTypeEnum
     * @param contractAddress
     * @param blockNumber
     * @return
     */
    public static List<TXBlockQueryVo> blockQuery(String url, TXChainTypeEnum txChainTypeEnum, List<String> contractAddress, Long blockNumber) {
        List<TXBlockQueryVo> txBlockQueryVos = null;
        if (StrUtil.isBlank(url) || ObjectUtil.isNull(txChainTypeEnum) || CollUtil.isEmpty(contractAddress) || blockNumber < 0) {
            return null;
        }
        try {
            String result = HttpUtil.post(url, JSONUtil.toJsonStr(MapTool.Map()
                    .put("chainType", txChainTypeEnum.name())
                    .put("contracts", contractAddress)
                    .put("blockNumber", blockNumber)));
            JSONObject jsonObject = JSONUtil.parseObj(result);
            Object data = jsonObject.get("data");
            if (ObjectUtil.isNotNull(data)) {
                txBlockQueryVos = JSONUtil.toList(JSONUtil.parseArray(data), TXBlockQueryVo.class);
            }
        } catch (Exception e) {
            log.error("根据区块查询失败,错误:{}", e.toString());
        }

        return txBlockQueryVos;
    }

    /**
     * 内部交易查询
     *
     * @param txChainTypeEnum 链
     * @param txhash          归集哈希
     * @return
     */
    public static Object insiderTrading(String url, TXChainTypeEnum txChainTypeEnum, String txhash) {
        if (StrUtil.isBlank(url) || ObjectUtil.isNull(txChainTypeEnum) || StrUtil.isBlank(txhash)) {
            return null;
        }
        String result = HttpUtil.get(url + "?" + URLUtil.buildQuery(MapTool.Map()
                .put("chainType", txChainTypeEnum.name())
                .put("txhash", txhash), Charset.defaultCharset()));
        JSONObject jsonObject = JSONUtil.parseObj(result);
        return jsonObject.get("data");
    }


    /*public static void main(String[] args) {
        List<TXBlockQueryVo> voList= blockQuery("https://nft-data-center.assure.pro/api/tx/list", TXChainTypeEnum.TRON, List.of("TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t"),	41342078L);
        System.out.println(voList);
        for (TXBlockQueryVo txBlockQueryVo : voList) {
            if (txBlockQueryVo.getHash().equals("9de4e6a0dff25f93e33ac42ba4998067fa310ed60f4eb3708dd0b181af597435")) {
                System.out.println(txBlockQueryVo);
            }
        }
    }*/

    public static void main(String[] args) {
        Long blockNumber = getBlockNumber("https://nft-data-center.assure.pro/api/tx/blockNumber", TXChainTypeEnum.ETH);
        System.out.println(blockNumber);
    }

    /*public static void main(String[] args) {
        Object o = insiderTrading("https://nft-data-center.assure.pro/api/tx/txlistinternal", TXChainTypeEnum.TRON, "b8c5ea52982d95668e9a195b34e11c187e01eb69ce09de6e11aaed8b89708bb7");
        System.out.println(o);
    }*/
}
