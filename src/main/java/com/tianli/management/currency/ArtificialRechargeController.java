package com.tianli.management.currency;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.charge.ChargeType;
import com.tianli.common.Constants;
import com.tianli.currency.ArtificialRechargeService;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.currency.log.CurrencyLog;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.currency.log.CurrencyLogService;
import com.tianli.currency.mapper.ArtificialRecharge;
import com.tianli.currency.mapper.ArtificialRechargeType;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.management.spot.entity.SGCharge;
import com.tianli.management.spot.service.SGChargeService;
import com.tianli.role.annotation.AdminPrivilege;
import com.tianli.role.annotation.FundsPasswordPrivilege;
import com.tianli.role.annotation.Privilege;
import com.tianli.tool.MapTool;
import com.tianli.tool.time.TimeTool;
import com.tianli.user.UserService;
import com.tianli.user.mapper.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 用户余额表 前端控制器
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@RestController
@RequestMapping("/artificial/recharge")
public class ArtificialRechargeController {

    @Resource
    private ArtificialRechargeService artificialRechargeService;

    @Resource
    private CurrencyLogService currencyLogService;

    @Resource
    private UserService userService;

    @Resource
    private SGChargeService sgChargeService;

    /**
     * 分页接口
     */
    @GetMapping("/page")
    @AdminPrivilege(and = Privilege.人工充值提现)
    public Result page(String username, String adminNick, String startTime, String endTime, ArtificialRechargeType type,
                       @RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Page<ArtificialRecharge> rechargePage = artificialRechargeService.page(new Page<>(page, size),
                new LambdaQueryWrapper<ArtificialRecharge>()
                        .orderByDesc(ArtificialRecharge::getCreate_time)
                        .like(StringUtils.isNotBlank(username), ArtificialRecharge::getUsername, username)
                        .like(StringUtils.isNotBlank(adminNick), ArtificialRecharge::getRecharge_admin_nick, adminNick)
                        .eq(Objects.nonNull(type), ArtificialRecharge::getType, type)
                        .ge(StringUtils.isNotBlank(startTime), ArtificialRecharge::getCreate_time, startTime)
                        .le(StringUtils.isNotBlank(endTime), ArtificialRecharge::getCreate_time, endTime));
        List<ArtificialRecharge> records = rechargePage.getRecords();
        long total = rechargePage.getTotal();
        BigInteger sumAmount = artificialRechargeService.getSumAmount(username, adminNick, startTime, endTime);
        Stream<ArtificialRechargePageVO> vo = records.stream().map(ArtificialRechargePageVO::trans);
        return Result.success(MapTool.Map().put("count", total)
                .put("list", vo)
                .put("sumAmount", TokenCurrencyType.usdt_omni.money(sumAmount)));
    }

    /**
     * 充值
     */
    @PostMapping("/submit")
    @AdminPrivilege(and = Privilege.手动充值)
    @FundsPasswordPrivilege
    public Result currentExchange(@RequestBody @Valid ArtificialRechargeDTO rechargeDTO) {
        artificialRechargeService.exchangeAmount(rechargeDTO);
        return Result.success();
    }

    /**
     * 撤销
     * 功能废除
     */
//    @PostMapping("/revoke/{id}")
//    @AdminPrivilege(and = Privilege.人工充值提现)
//    public Result revoke(@PathVariable("id") Long id) {
//        artificialRechargeService.revokeExchangeAmount(id);
//        return Result.success();
//    }

    @GetMapping("/detail/{id}")
    @AdminPrivilege(and = Privilege.人工充值提现)
    public Result detail(@PathVariable("id") Long id) {
        ArtificialRecharge byId = artificialRechargeService.getById(id);
        ArtificialRechargeDetailVO vo = ArtificialRechargeDetailVO.builder()
                .amount(TokenCurrencyType.usdt_omni.money(byId.getAmount()))
                .type(byId.getType())
                .recharge_admin_nick(byId.getRecharge_admin_nick())
                .create_time(byId.getCreate_time())
                .voucher_image(byId.getVoucher_image())
                .remark(byId.getRemark())
                .build();
        return Result.success(vo);
    }
    @GetMapping("/detail/log/{id}")
    @AdminPrivilege(and = Privilege.人工充值提现)
    public Result logDetail(@PathVariable("id") Long id) {
        CurrencyLog currencyLog = currencyLogService.getById(id);
        if(Objects.isNull(currencyLog)){
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        Long arId = Long.valueOf(currencyLog.getSn().replace("ar_", ""));
        ArtificialRecharge byId = artificialRechargeService.getById(arId);
        ArtificialRechargeDetailVO vo = ArtificialRechargeDetailVO.builder()
                .amount(TokenCurrencyType.usdt_omni.money(byId.getAmount()))
                .type(byId.getType())
                .recharge_admin_nick(byId.getRecharge_admin_nick())
                .create_time(byId.getCreate_time())
                .voucher_image(byId.getVoucher_image())
                .remark(byId.getRemark())
                .build();
        return Result.success(vo);
    }

    /**
     * 现货充值or现货提现提醒
     * @return
     */
    @GetMapping("/deferred/result")
    public DeferredResult<Result> getDeferredBetResult(){
        DeferredResult<Result> output = new DeferredResult<>(58L, () -> {
            LocalDateTime nowDateTime = LocalDateTime.now();
            LocalDateTime startDateTime = nowDateTime;
            LocalDateTime endDateTime = nowDateTime.plusSeconds(50);
            while (nowDateTime.isBefore(endDateTime)){
                List<SGCharge> list = sgChargeService.list(Wrappers.lambdaQuery(SGCharge.class)
                        .ge(SGCharge::getCreate_time, startDateTime)
                        //.in(SGCharge::getCharge_type, List.of(ChargeType.recharge.name(), ChargeType.withdraw.name()))
                );
                if(CollectionUtils.isEmpty(list)){
                    TimeTool.sleep(2L, TimeUnit.SECONDS);
                    nowDateTime = LocalDateTime.now();
                }else{
                    // 转换voList
                    return Result.success(list.stream().map(e -> {
                        User user = userService._get(e.getUid());
                        return SgChargeVO.trans(e, Objects.isNull(user) ? Constants.defaultUserNick : user.getUsername());
                    }).collect(Collectors.toList()));
                }
            }
            return Result.success(List.of());
        });
        return output;
    }

}