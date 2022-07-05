package com.tianli.management.fundmanagement;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.tianli.charge.ChargeService;
import com.tianli.charge.ChargeType;
import com.tianli.charge.mapper.Charge;
import com.tianli.charge.mapper.ChargeStatus;
import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.currency.log.CurrencyLog;
import com.tianli.currency.log.CurrencyLogService;
import com.tianli.exception.Result;
import com.tianli.role.annotation.AdminPrivilege;
import com.tianli.role.annotation.Privilege;
import com.tianli.tool.MapTool;
import com.tianli.user.UserService;
import com.tianli.user.mapper.User;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("withdrawalManage")
public class WithdrawalManageController {
    @Resource
    private ChargeService chargeService;

    @Resource
    private CurrencyLogService currencyLogService;

    @Resource
    private WithdrawalManageService withdrawalManageService;

    @Resource
    private UserService userService;

    @GetMapping("/page")
    @AdminPrivilege(and = Privilege.提现管理)
    public Result page(String phone,
                       String txid,
                       ChargeStatus status,
                       @RequestParam(value = "ip", required = false) String ip,
                       @RequestParam(value = "equipment", required = false) String equipment,
                       @RequestParam(value = "grc_result", required = false) Boolean grc_result,
                       @RequestParam(value = "otherSec", defaultValue = "false") Boolean otherSec,
                       String startTime,
                       String endTime,
                       @RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "size", defaultValue = "10") Integer size) {
        int count = chargeService.selectNewCount(ip, equipment, grc_result, otherSec, null, status, ChargeType.withdraw, phone, txid, startTime, endTime);
        if (count <= 0) {
            return Result.instance().setData(MapTool.Map().put("total", 0).put("list", Lists.newArrayList()).put("totalAmount", BigDecimal.ZERO));
        }
        List<WithdrawalManagePO> vos = chargeService.selectNewPage(ip, equipment, grc_result, otherSec, null, status, ChargeType.withdraw, phone, txid, startTime, endTime, page, size);
        List<Map<String, Object>> list = chargeService.totalAmount(ip, equipment, grc_result, otherSec, null, status, ChargeType.withdraw, phone, txid, startTime, endTime);
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (CollUtil.isNotEmpty(list)) {
            for (Map<String, Object> map : list) {
                String token = Convert.toStr(map.get("token"));
                BigInteger sum = Convert.toBigInteger(map.get("sum"));
                double money = TokenCurrencyType.getTokenCurrencyType(token).money(sum);
                totalAmount = totalAmount.add(Convert.toBigDecimal(money));
            }
        }
        return Result.instance().setData(MapTool.Map().put("total", count)
                .put("list", vos.stream().map(WithdrawalManageVO::convert).collect(Collectors.toList())).put("totalAmount", totalAmount));
    }

    @GetMapping("/behavior/info/{id}")
    @AdminPrivilege(and = Privilege.提现管理)
    public Result behaviorInfo(@PathVariable("id") Long id) {
        WithdrawalManageBhvPO infoById = chargeService.getInfoById(id);
        return Result.success(WithdrawalManageBhvVO.trans(infoById));
    }

    @GetMapping("/currency/{sn}")
    @AdminPrivilege(and = Privilege.提现管理)
    public Result currency(@PathVariable("sn") String sn) {
        CurrencyLog currencyLog = currencyLogService.getOne(new LambdaQueryWrapper<CurrencyLog>().eq(CurrencyLog::getSn, sn));
        if (Objects.isNull(currencyLog)) {
            return Result.instance().setData(0);
        }
        double money;
        if (Objects.equals(currencyLog.getToken(), CurrencyTokenEnum.usdt_omni)) {
            money = TokenCurrencyType.usdt_omni.money(currencyLog.getRemain().add(currencyLog.getAmount()));
        } else {
            money = CurrencyTokenEnum.BF_bep20.money(currencyLog.getRemain().add(currencyLog.getAmount()));
        }
        return Result.instance().setData(money);
    }

    @PostMapping("/audit")
    @AdminPrivilege(and = Privilege.提现管理)
    public Result audit(@RequestBody @Valid ChargeAuditDTO dto) {
        Charge charge = chargeService.getById(dto.getId());
        if (charge == null) return Result.instance();
        User user = userService._get(charge.getUid());
        if (user.getUser_type().equals(0)) {
            // 普通会员, 审核成功则上链
            if(dto.getStatus().equals(ChargeStatus.chain_success)) dto.setStatus(ChargeStatus.chaining);
            withdrawalManageService.audit(dto);
        } else {
            // 员工
            withdrawalManageService.audit2(dto);
        }
        return Result.instance();
    }
}
