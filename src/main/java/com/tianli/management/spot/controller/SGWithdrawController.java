package com.tianli.management.spot.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.charge.mapper.ChargeStatus;
import com.tianli.currency_token.CurrencyTokenLogService;
import com.tianli.currency_token.mapper.CurrencyTokenLog;
import com.tianli.exception.Result;
import com.tianli.management.spot.dto.SGWithdrawAuditDTO;
import com.tianli.management.spot.entity.SGCharge;
import com.tianli.management.spot.service.SGChargeService;
import com.tianli.management.spot.vo.SGWithdrawListVo;
import com.tianli.role.annotation.AdminPrivilege;
import com.tianli.role.annotation.Privilege;
import com.tianli.user.UserService;
import com.tianli.user.mapper.User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author lzy
 * @date 2022/4/15 2:47 下午
 * 现货提现管理
 */
@RestController
@RequestMapping("/management/spot/withdraw")
public class SGWithdrawController {

    @Resource
    SGChargeService sgChargeService;
    @Resource
    private UserService userService;

    @Resource
    CurrencyTokenLogService currencyTokenLogService;

    @GetMapping("/page")
    @AdminPrivilege(and = Privilege.现货提现管理)
    public Result page(String username, String status, String startTime, String endTime,
                       @RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "size", defaultValue = "20") Integer size) {
        IPage<SGWithdrawListVo> sgWithdrawListVoIPage = sgChargeService.selectPage(username, status, startTime, endTime, page, size);
        return Result.success(sgWithdrawListVoIPage);
    }

    @GetMapping("/sumAmount")
    @AdminPrivilege(and = Privilege.现货提现管理)
    public Result sumAmount(String username, String status, String startTime, String endTime) {
        BigDecimal sumAmount = sgChargeService.sumAmount(username, status, startTime, endTime);
        return Result.success(ObjectUtil.isNull(sumAmount) ? BigDecimal.ZERO : sumAmount);
    }

    @PostMapping("/audit")
    @AdminPrivilege(and = Privilege.现货提现管理)
    public Result audit(@RequestBody @Validated SGWithdrawAuditDTO sgWithdrawAuditDTO) {
        SGCharge sgCharge = sgChargeService.getById(sgWithdrawAuditDTO.getId());
        if (sgCharge == null) return Result.instance();
        User user = userService._get(sgCharge.getUid());
        if (user.getUser_type().equals(0)) {
            if (sgWithdrawAuditDTO.getStatus().equals(ChargeStatus.chain_success))
                sgWithdrawAuditDTO.setStatus(ChargeStatus.chaining);
        }
        sgChargeService.audit(sgWithdrawAuditDTO);
        return Result.success();
    }

    @GetMapping("/currency/{sn}")
    @AdminPrivilege(and = Privilege.现货提现管理)
    public Result currency(@PathVariable("sn") String sn) {
        CurrencyTokenLog currencyTokenLog = currencyTokenLogService.getOne(new LambdaQueryWrapper<CurrencyTokenLog>().eq(CurrencyTokenLog::getSn, sn));
        if (Objects.isNull(currencyTokenLog)) {
            return Result.instance().setData(0);
        }
        BigDecimal money = currencyTokenLog.getRemain().add(currencyTokenLog.getAmount());
        return Result.instance().setData(money);
    }
}
