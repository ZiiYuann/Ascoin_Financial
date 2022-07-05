package com.tianli.management.spot.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.exception.Result;
import com.tianli.management.spot.service.SGRechargeService;
import com.tianli.management.spot.vo.SGRechargeByTypeVo;
import com.tianli.management.spot.vo.SGRechargeListVo;
import com.tianli.role.annotation.AdminPrivilege;
import com.tianli.role.annotation.Privilege;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author lzy
 * @date 2022/4/24 14:05
 */
@RestController
@RequestMapping("/management/spot/recharge")
public class SGRechargeController {

    @Resource
    SGRechargeService sgRechargeService;

    @GetMapping("/page")
    @AdminPrivilege(and = Privilege.现货充值记录)
    public Result page(String username, String token, String startTime, String endTime, String txid, Long salesman_id,
                       @RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "size", defaultValue = "10") Integer size) {
        IPage<SGRechargeListVo> result = sgRechargeService.page(username, token, startTime, endTime, txid, salesman_id, page, size);
        return Result.success(result);
    }

    @GetMapping("/sumAmount")
    @AdminPrivilege(and = Privilege.现货充值记录)
    public Result sumAmount(String username, String token, String startTime, String endTime, String txid, Long salesman_id) {
        BigDecimal sumUAmount = sgRechargeService.sumUAmount(username, token, startTime, endTime, txid, salesman_id);
        return Result.success(ObjectUtil.isNull(sumUAmount) ? BigDecimal.ZERO : sumUAmount);
    }

    @GetMapping("/listSumAmount")
    @AdminPrivilege(and = Privilege.现货充值记录)
    public Result listSumAmount(Integer token,String currencyType,Integer page,Integer size) {
        IPage<SGRechargeByTypeVo> sumUAmount = sgRechargeService.listSumAmount(token,currencyType,page,size);
        return Result.success(sumUAmount);
    }
}
