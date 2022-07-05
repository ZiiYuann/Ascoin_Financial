package com.tianli.currency_token.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.common.CommonFunction;
import com.tianli.common.init.RequestInitService;
import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.currency_token.TokenFavoriteService;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.currency_token.mapper.TokenFavorite;
import com.tianli.currency_token.vo.TokenFavoriteListVo;
import com.tianli.exception.Result;
import com.tianli.exchange.entity.KLinesInfo;
import com.tianli.exchange.processor.CoinProcessor;
import com.tianli.exchange.vo.Mini24HrTickerVo;
import com.tianli.management.newcurrency.entity.NewCurrencyManagement;
import com.tianli.management.newcurrency.service.INewCurrencyManagementService;
import com.tianli.tool.Bian24HrInfo;
import com.tianli.tool.BianPriceCache;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/currency/token/favorite")
public class TokenFavoriteController {

    /**
     * 添加自选
     * @param stock
     * @param fiat
     * @param type
     * @param tag
     * @return
     */
    @GetMapping("/add")
    public Result favorite(CurrencyCoinEnum stock, CurrencyCoinEnum fiat, CurrencyTypeEnum type,
                           @RequestParam(value = "tag", defaultValue = "1") Integer tag) {
        Long uid = requestInitService.uid();
        TokenFavorite tokenFavorite = tokenFavoriteService.getOne(new LambdaQueryWrapper<TokenFavorite>()
                .eq(TokenFavorite::getUid, uid).eq(TokenFavorite::getFiat, fiat).eq(TokenFavorite::getStock, stock)
                .eq(TokenFavorite::getType, type)
        );
        if(tokenFavorite == null) tokenFavorite = TokenFavorite.builder()
                .fiat(fiat).stock(stock).type(type).create_time(requestInitService.now()).update_time(requestInitService.now())
                .id(CommonFunction.generalId()).sort(0).uid(uid).build();
        else tokenFavorite.setUpdate_time(requestInitService.now());
        if(tag == 1) tokenFavoriteService.saveOrUpdate(tokenFavorite);
        if(tag == 0) tokenFavoriteService.removeById(tokenFavorite.getId());
        return Result.instance();
    }

    /**
     * 批量删除自选列表
     * @param
     * @return
     */
    @PostMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody List<Long> tokenFavoriteIds) {
        Long uid = requestInitService.uid();
        tokenFavoriteService.remove(Wrappers.lambdaQuery(TokenFavorite.class)
                .eq(TokenFavorite::getUid, uid).in(TokenFavorite::getId, tokenFavoriteIds));
        return Result.instance();
    }

    /**
     * 自选列表排序
     * @param
     * @return
     */
    @PostMapping("/sort")
    public Result sort(@RequestBody List<TokenFavorite> tokenFavoriteList) {
        Long uid = requestInitService.uid();
        tokenFavoriteService.updateBatchById(tokenFavoriteList);
        return Result.instance();
    }

    /**
     * 所有币列表
     * @param page
     * @param size
     * @param type
     * @return
     */
    @GetMapping("/list")
    public Result favorite(@RequestParam(value = "page", defaultValue = "1") Integer page,
                           @RequestParam(value = "size", defaultValue = "10") Integer size,
                           CurrencyTypeEnum type) {
        Long uid = requestInitService.uid();
        List<TokenFavorite> list = tokenFavoriteService.page(new Page<>(page, size), new LambdaQueryWrapper<TokenFavorite>()
                .eq(TokenFavorite::getUid, uid)
                .eq(Objects.nonNull(type), TokenFavorite::getType, type)
                .orderByAsc(TokenFavorite::getSort).orderByDesc(TokenFavorite::getUpdate_time)
        ).getRecords();
        long count = tokenFavoriteService.count(new LambdaQueryWrapper<TokenFavorite>().eq(TokenFavorite::getType, type).eq(TokenFavorite::getUid, uid));
        List<NewCurrencyManagement> currencyManagements = newCurrencyManagementService.queryOnlineToken();
        Map<String, NewCurrencyManagement> currencyManagementMap = new HashMap<>();
        if (CollUtil.isNotEmpty(currencyManagements)) {
            currencyManagementMap = currencyManagements.stream().collect(Collectors.toMap(NewCurrencyManagement::getCurrency_name_short, Function.identity(), (v1, v2) -> v1));
        }
        List<TokenFavoriteListVo> result = new ArrayList<>();
        for (TokenFavorite tokenFavorite : list) {
            TokenFavoriteListVo tokenFavoriteListVo = BeanUtil.copyProperties(tokenFavorite, TokenFavoriteListVo.class);
            NewCurrencyManagement newCurrencyManagement = currencyManagementMap.get(tokenFavoriteListVo.getStock().name().toUpperCase());
            if (ObjectUtil.isNull(newCurrencyManagement)) {
                Bian24HrInfo bian24HrInfo = BianPriceCache.getPrice(tokenFavoriteListVo.getStock().name().toUpperCase() + "USDT");
                tokenFavoriteListVo.setBian24HrInfo(bian24HrInfo);
                tokenFavoriteListVo.setIs_platform(Boolean.FALSE);
            } else {
                KLinesInfo kLinesInfo = coinProcessor.getDayKLinesInfoBySymbol(tokenFavoriteListVo.getStock().name().toUpperCase() + "USDT");
                tokenFavoriteListVo.setPlatform24HrTicker(Mini24HrTickerVo.getMini24HrTickerVo(kLinesInfo));
                tokenFavoriteListVo.setIs_platform(Boolean.TRUE);
            }
            result.add(tokenFavoriteListVo);
        }
        return Result.instance().setList(result, count);
    }

    @Resource
    CoinProcessor coinProcessor;

    @Resource
    INewCurrencyManagementService newCurrencyManagementService;
    @Resource
    private RequestInitService requestInitService;
    @Resource
    private TokenFavoriteService tokenFavoriteService;
}
