package com.tianli.exchange.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.tianli.common.lock.RedisLock;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exchange.dao.KLinesInfoMapper;
import com.tianli.exchange.dto.KLinesListDTO;
import com.tianli.exchange.dto.Ticker24DTO;
import com.tianli.exchange.entity.KLinesInfo;
import com.tianli.exchange.enums.KLinesIntervalEnum;
import com.tianli.exchange.processor.CoinProcessor;
import com.tianli.exchange.service.IKLinesInfoService;
import com.tianli.exchange.vo.Mini24HrTickerVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author lzy
 * @since 2022-06-09
 */
@Service
public class KLinesInfoServiceImpl extends ServiceImpl<KLinesInfoMapper, KLinesInfo> implements IKLinesInfoService {

    @Resource
    private RedisLock redisLock;

    @Resource
    KLinesInfoMapper kLinesInfoMapper;

    @Resource
    CoinProcessor coinProcessor;

    @Resource
    Gson gson;


    @Override
    public List<KLinesInfo> findAllKLine(List<String> symbols, Long startTime, Long endTime, KLinesIntervalEnum kLinesIntervalEnum) {
        return this.list(Wrappers.lambdaQuery(KLinesInfo.class)
                .in(KLinesInfo::getSymbol, symbols)
                .ge(ObjectUtil.isNotNull(startTime), KLinesInfo::getOpening_time, startTime)
                .le(ObjectUtil.isNotNull(endTime), KLinesInfo::getOpening_time, endTime)
                .eq(KLinesInfo::getInterval, kLinesIntervalEnum.getInterval())
                .orderByAsc(KLinesInfo::getOpening_time));
    }

    @Override
    public KLinesInfo findLastBySymbol(String symbol) {
        return this.getOne(Wrappers.lambdaQuery(KLinesInfo.class)
                .eq(KLinesInfo::getSymbol, symbol)
                .gt(KLinesInfo::getClosing_price, BigDecimal.ZERO)
                .orderByDesc(KLinesInfo::getOpening_time)
                .last("limit 1"));
    }

    @Override
    public void addBatch(List<KLinesInfo> kLinesInfos) {
        kLinesInfoMapper.addBatch(kLinesInfos);
    }

    @Override
    public List<List<Object>> kLines(KLinesListDTO kLinesListDTO) {
        List<KLinesInfo> kLinesInfos = this.list(Wrappers.lambdaQuery(KLinesInfo.class)
                .eq(KLinesInfo::getSymbol, kLinesListDTO.getSymbol())
                .eq(KLinesInfo::getInterval, kLinesListDTO.getInterval())
                .ge(ObjectUtil.isNotNull(kLinesListDTO.getStartTime()) && kLinesListDTO.getStartTime() > 0, KLinesInfo::getOpening_time, kLinesListDTO.getStartTime())
                .le(ObjectUtil.isNotNull(kLinesListDTO.getEndTime()) && kLinesListDTO.getEndTime() > 0, KLinesInfo::getOpening_time, kLinesListDTO.getEndTime())
                .orderByDesc(KLinesInfo::getOpening_time)
                .last("limit " + kLinesListDTO.getLimit()));
        ListUtil.reverse(kLinesInfos);
        if ((ObjectUtil.isNull(kLinesListDTO.getStartTime()) || kLinesListDTO.getStartTime() <= 0) && (ObjectUtil.isNull(kLinesListDTO.getEndTime())) || kLinesListDTO.getEndTime() <= 0) {
            //加上最近的一根柱子的数据
            KLinesInfo lastKLinesInfo = getLastKLinesInfo(kLinesListDTO);
            if (ObjectUtil.isNotNull(lastKLinesInfo)) {
                kLinesInfos.add(lastKLinesInfo);
            }
        }
        List<List<Object>> result = new ArrayList<>(kLinesListDTO.getLimit());
        for (KLinesInfo kLinesInfo : kLinesInfos) {
            List<Object> info = new ArrayList<>();
            info.add(kLinesInfo.getOpening_time());
            info.add(kLinesInfo.getOpening_price());
            info.add(kLinesInfo.getHighest_price());
            info.add(kLinesInfo.getLowest_price());
            info.add(kLinesInfo.getClosing_price());
            info.add(kLinesInfo.getVolume());
            info.add(kLinesInfo.getClosing_time());
            info.add(kLinesInfo.getTurnover());
            info.add(kLinesInfo.getTurnover_num());
            info.add(kLinesInfo.getActive_buy_volume());
            info.add(kLinesInfo.getActive_buy_turnover());
            result.add(info);
        }
        return result;
    }

    private KLinesInfo getLastKLinesInfo(KLinesListDTO kLinesListDTO) {
        KLinesIntervalEnum kLinesIntervalEnum = KLinesIntervalEnum.convert(kLinesListDTO.getInterval());
        if (ObjectUtil.isNull(kLinesIntervalEnum)) {
            return null;
        }
        KLinesInfo kLinesInfo = coinProcessor.getKLinesInfo(kLinesListDTO.getSymbol());
        if (ObjectUtil.isNull(kLinesInfo)) {
            return null;
        }
        if (kLinesInfo.getOpening_price().compareTo(BigDecimal.ZERO) == 0) {
            KLinesInfo dayKLinesInfoBySymbol = coinProcessor.getDayKLinesInfoBySymbol(kLinesInfo.getSymbol());
            BigDecimal closingPrice = dayKLinesInfoBySymbol.getClosing_price();
            kLinesInfo.setOpening_price(closingPrice);
            kLinesInfo.setHighest_price(closingPrice);
            kLinesInfo.setLowest_price(closingPrice);
            kLinesInfo.setClosing_price(closingPrice);
        }
        switch (kLinesIntervalEnum) {
            case one:
                return kLinesInfo;
            case five:
            case fifteen:
            case thirty:
            case sixty:
                return setLastKLinesInfo(kLinesInfo, kLinesIntervalEnum, kLinesListDTO.getSymbol(), KLinesIntervalEnum.getIntervalTime(kLinesIntervalEnum));
            case day:
                KLinesInfo dayKLinesInfoBySymbol = coinProcessor.getDayKLinesInfoBySymbol(kLinesInfo.getSymbol());
                BigDecimal closingPrice = dayKLinesInfoBySymbol.getClosing_price();
                if (dayKLinesInfoBySymbol.getOpening_price().compareTo(BigDecimal.ZERO) == 0) {
                    dayKLinesInfoBySymbol.setOpening_price(closingPrice);
                }
                if (dayKLinesInfoBySymbol.getHighest_price().compareTo(BigDecimal.ZERO) == 0) {
                    dayKLinesInfoBySymbol.setHighest_price(closingPrice);
                }
                if (dayKLinesInfoBySymbol.getLowest_price().compareTo(BigDecimal.ZERO) == 0) {
                    dayKLinesInfoBySymbol.setLowest_price(closingPrice);
                }
                return dayKLinesInfoBySymbol;
        }
        return null;
    }

    private KLinesInfo setLastKLinesInfo(KLinesInfo kLinesInfo, KLinesIntervalEnum kLinesIntervalEnum, String symbol, Long intervalTime) {
        List<KLinesInfo> kLinesInfoList = this.findAllKLine(ListUtil.of(symbol), intervalTime, null, KLinesIntervalEnum.one);
        if (CollUtil.isEmpty(kLinesInfoList)) {
            return kLinesInfo;
        }
        kLinesInfo.setOpening_time(intervalTime);
        kLinesInfo.setClosing_time(intervalTime + (kLinesIntervalEnum.getMinute() * 60000) - 1);
        kLinesInfo.setInterval(kLinesIntervalEnum.getInterval());
        kLinesInfo = coinProcessor.setKline(kLinesInfo, kLinesInfoList);
        return kLinesInfo;
    }

    @Override
    public List<Mini24HrTickerVo> ticker24hr(Ticker24DTO ticker24DTO) {
        if (StrUtil.isNotBlank(ticker24DTO.getSymbol()) && StrUtil.isNotBlank(ticker24DTO.getSymbols())) {
            throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
        }
        List<String> symbolList = new ArrayList<>();
        if (StrUtil.isNotBlank(ticker24DTO.getSymbol())) {
            symbolList.add(ticker24DTO.getSymbol());
        } else if (StrUtil.isNotBlank(ticker24DTO.getSymbols())) {
            List list = gson.fromJson(ticker24DTO.getSymbols(), List.class);
            if (CollUtil.isNotEmpty(list)) {
                list.forEach(symbol -> symbolList.add(symbol.toString()));
            }
        }
        List<Mini24HrTickerVo> mini24HrTickerVos = new ArrayList<>();
        Map<String, KLinesInfo> dayKLinesInfo = coinProcessor.getDayKLinesInfo(null);
        if (CollUtil.isEmpty(dayKLinesInfo)) {
            return mini24HrTickerVos;
        }
        if (CollUtil.isEmpty(symbolList)) {
            for (String symbol : dayKLinesInfo.keySet()) {
                KLinesInfo kLinesInfo = dayKLinesInfo.get(symbol);
                mini24HrTickerVos.add(Mini24HrTickerVo.getMini24HrTickerVo(kLinesInfo));
            }
        } else {
            for (String symbol : symbolList) {
                KLinesInfo kLinesInfo = dayKLinesInfo.get(symbol);
                mini24HrTickerVos.add(Mini24HrTickerVo.getMini24HrTickerVo(kLinesInfo));
            }
        }
        return mini24HrTickerVos;
    }
}
