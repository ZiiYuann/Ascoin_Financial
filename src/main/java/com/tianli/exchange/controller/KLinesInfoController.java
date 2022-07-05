package com.tianli.exchange.controller;


import com.tianli.exception.Result;
import com.tianli.exchange.dto.KLinesListDTO;
import com.tianli.exchange.dto.Ticker24DTO;
import com.tianli.exchange.service.IKLinesInfoService;
import com.tianli.exchange.vo.Mini24HrTickerVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author lzy
 * @since 2022-06-09
 */
@RestController
@RequestMapping("/kLinesInfo")
public class KLinesInfoController {

    @Resource
    IKLinesInfoService ikLinesInfoService;


    @GetMapping("/kLines")
    public Result kLines(@Validated KLinesListDTO kLinesListDTO) {
        return Result.success(ikLinesInfoService.kLines(kLinesListDTO));
    }

    @GetMapping("/ticker/24hr")
    public Result ticker(Ticker24DTO ticker24DTO) {
        List<Mini24HrTickerVo> miniTickerStreams = ikLinesInfoService.ticker24hr(ticker24DTO);
        return Result.success(miniTickerStreams);
    }
}

