package com.tianli.exchange.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.exchange.dto.KLinesListDTO;
import com.tianli.exchange.dto.Ticker24DTO;
import com.tianli.exchange.entity.KLinesInfo;
import com.tianli.exchange.enums.KLinesIntervalEnum;
import com.tianli.exchange.vo.Mini24HrTickerVo;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author lzy
 * @since 2022-06-09
 */
public interface IKLinesInfoService extends IService<KLinesInfo> {


    List<KLinesInfo> findAllKLine(List<String> symbols, Long startTime, Long endTime, KLinesIntervalEnum kLinesIntervalEnum);

    KLinesInfo findLastBySymbol(String symbol);

    void addBatch(List<KLinesInfo> kLinesInfos);

    List<List<Object>> kLines(KLinesListDTO kLinesListDTO);

    /**
     * 24小时行情
     * @param ticker24DTO
     * @return
     */
    List<Mini24HrTickerVo> ticker24hr(Ticker24DTO ticker24DTO);
}
