package com.tianli.management.fundmanagement;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.bet.BetService;
import com.tianli.bet.mapper.Bet;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.exception.Result;
import com.tianli.role.annotation.AdminPrivilege;
import com.tianli.role.annotation.Privilege;
import com.tianli.tool.MapTool;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author cs
 * @Date 2022-01-14 11:21 上午
 */
@RestController
@RequestMapping("dividendsManage")
public class DividendsManageController {
    @Resource
    private BetService betService;

    @GetMapping("page")
    @AdminPrivilege(and = Privilege.抽成记录)
    public Result page(String phone, String agentPhone, String startTime, String endTime,
                       @RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Page<Bet> betPage = betService.page(new Page<>(page, size), Wrappers.<Bet>lambdaQuery()
                .orderByDesc(Bet::getId)
                .like(StringUtils.isNotBlank(phone), Bet::getUid_username, phone)
                .like(StringUtils.isNotBlank(agentPhone), Bet::getAgent_username, agentPhone)
                .ge(StringUtils.isNotBlank(startTime), Bet::getCreate_time, startTime)
                .le(StringUtils.isNotBlank(endTime), Bet::getCreate_time, endTime));
        Map<String, Object> statMap = betService.getMap(Wrappers.<Bet>query().select("ifnull(SUM(`amount`), 0) as amountSum, \n" +
                        " ifnull(SUM(CASE WHEN `profit_token` = 'BF' THEN `pf_profit` ELSE 0 END), 0) as pfProfitSumBF,\n" +
                        " ifnull(SUM(CASE WHEN `profit_token` = 'usdt' THEN `pf_profit` ELSE 0 END), 0) as pfProfitSum,\n" +
                        " ifnull(SUM(CASE WHEN `profit_token` = 'BF' THEN `agent_profit` ELSE 0 END), 0) as agentProfitSumBF,\n" +
                        " ifnull(SUM(CASE WHEN `profit_token` = 'usdt' THEN `agent_profit` ELSE 0 END), 0) as agentProfitSum")
                .like(StringUtils.isNotBlank(phone), "`uid_username`", phone)
                .like(StringUtils.isNotBlank(agentPhone), "`agent_username`", agentPhone)
                .ge(StringUtils.isNotBlank(startTime), "`create_time`", startTime)
                .le(StringUtils.isNotBlank(endTime), "`create_time`", endTime));
        MapTool stat = MapTool.Map().put("amountSum", TokenCurrencyType.usdt_omni.money(new BigInteger(statMap.get("amountSum").toString())))
                .put("pfProfitSumBF", TokenCurrencyType.BF_bep20.money(new BigInteger(statMap.get("pfProfitSumBF").toString())))
                .put("pfProfitSum", TokenCurrencyType.usdt_omni.money(new BigInteger(statMap.get("pfProfitSum").toString())))
                .put("agentProfitSumBF", TokenCurrencyType.BF_bep20.money(new BigInteger(statMap.get("agentProfitSumBF").toString())))
                .put("agentProfitSum", TokenCurrencyType.usdt_omni.money(new BigInteger(statMap.get("agentProfitSum").toString())));
        List<DividendsManageVO> collect = betPage.getRecords().stream().map(DividendsManageVO::trans).collect(Collectors.toList());
        return Result.success(MapTool.Map().put("total", betPage.getTotal()).put("list", collect).put("stat", stat));
    }
}
