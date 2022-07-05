package com.tianli.exchange.processor;

import com.tianli.exchange.dto.ExchangeCoreResponseConvertDTO;
import com.tianli.exchange.entity.KLinesInfo;
import com.tianli.exchange.push.DepthStream;
import com.tianli.exchange.vo.DepthVo;

import java.util.List;
import java.util.Map;

/**
 * @author lzy
 * @date 2022/6/10 13:43
 */
public interface CoinProcessor {


    /**
     * 根据交易对查询当前分钟的k线
     *
     * @param symbol
     * @return
     */
    KLinesInfo getKLinesInfo(String symbol);

    /**
     * 查询当前分钟的所有k线
     *
     * @return
     */
    Map<String, KLinesInfo> getKLinesInfo();

    /**
     * 初始化日k线
     */
    void initDayKLines();

    /**
     * 查询所有已经上架的平台币
     * @return
     */
    List<String> getOnlineTradingPair();

    /**
     * 根据所传k线集合设置k线的最终属性值
     * @param kLine
     * @param kLinesInfos
     * @return
     */
    KLinesInfo setKline(KLinesInfo kLine, List<KLinesInfo> kLinesInfos);

    void autoGenerate();

    /**
     * 根据交易对查询当天的k线
     * @param symbol
     * @return
     */
    KLinesInfo getDayKLinesInfoBySymbol(String symbol);

    /**
     * 查询某天的k线
     *
     * @param time 开盘时间
     * @return
     */
    Map<String, KLinesInfo> getDayKLinesInfo(Long time);

    /**
     * 生产其他间隔k线
     */
    void generateKLine();

    /**
     * 设置k线
     *
     * @param coreResponseDTO
     */
    void processTrade(ExchangeCoreResponseConvertDTO coreResponseDTO);

    /**
     * 设置币深度信息
     * @param depthStream
     * @param symbol
     */
    void setDepth(DepthStream depthStream, String symbol);

    DepthVo getDepth(String symbol);
}
