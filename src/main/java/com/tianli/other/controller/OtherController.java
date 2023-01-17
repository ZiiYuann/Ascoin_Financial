package com.tianli.other.controller;

import com.tianli.account.query.IdsQuery;
import com.tianli.common.RedisConstants;
import com.tianli.currency.service.DigitalCurrencyExchange;
import com.tianli.exception.Result;
import com.tianli.other.service.BannerService;
import com.tianli.other.vo.IpInfoVO;
import com.tianli.tool.IPUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author chenb
 * @apiNote
 * @since 2022-12-12
 **/
@RestController
@RequestMapping("/other")
public class OtherController {

    @Resource
    private BannerService bannerService;
    @Resource
    private DigitalCurrencyExchange digitalCurrencyExchange;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/banner/list")
    public Result bannerList() {
        return Result.success(bannerService.processList());
    }

    /**
     * 获取ip相关的信息
     */
    @GetMapping("/ip")
    public Result ip(HttpServletRequest request) {
        try {
            var mapOptional = Optional.of(IPUtils.ipAnalysis(IPUtils.getIpAddress(request)));
            var result = mapOptional.orElse(new IpInfoVO());
            return Result.success(result);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.success(new IpInfoVO());
        }
    }

    /**
     * 获取usdt兑cny汇率
     */
    @GetMapping("/usdtCny")
    public Result usdtCny() {
        HashMap<String, Double> result = new HashMap<>();
        result.put("rate", digitalCurrencyExchange.usdtCnyPrice());
        return Result.success(result);
    }

    /**
     * 提现黑名单
     */
    @PostMapping("/withdraw/black")
    public Result withdraw(@RequestBody @Valid IdsQuery query) {
        stringRedisTemplate.opsForSet().add(RedisConstants.WITHDRAW_BLACK, query.getId() + ""); // 添加黑名单用户
        return Result.success();
    }

    /**
     * 提现黑名单剔除
     */
    @PostMapping("/withdraw/black/remove")
    public Result withdrawRemove(@RequestBody @Valid IdsQuery query) {
        stringRedisTemplate.opsForSet().remove(RedisConstants.WITHDRAW_BLACK, query.getId() + ""); // 添加黑名单用户
        return Result.success();
    }

    /**
     * 提现黑名单剔除
     */
    @GetMapping("/withdraw/black/set")
    public Result withdrawSet() {
        Set<String> members = stringRedisTemplate.opsForSet().members(RedisConstants.WITHDRAW_BLACK);// 添加黑名单用户\
        return Result.success(members);
    }

}
