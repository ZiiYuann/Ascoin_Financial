package com.tianli.management.agentadmin.senior;

import com.tianli.bet.mapper.BetResultEnum;
import com.tianli.common.init.RequestInitService;
import com.tianli.common.lock.RedisLock;
import com.tianli.deposit.mapper.DepositSettlementType;
import com.tianli.deposit.mapper.LowDepositChargeType;
import com.tianli.dividends.settlement.mapper.ChargeSettlementStatus;
import com.tianli.dividends.settlement.mapper.LowSettlementChargeType;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.management.agentadmin.AgentAdminService;
import com.tianli.management.agentadmin.dto.*;
import com.tianli.tool.MapTool;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/seniorManage")
public class SeniorController {

    @Resource
    private AgentAdminService agentAdminService;


    @GetMapping("/homeData")
    public Result homeStatData() {
        return Result.success(agentAdminService.statHomeData());
    }

    @GetMapping("/teamStatData")
    public Result teamStatData(@RequestParam(value = "page", defaultValue = "1") Integer page,
                               @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return Result.success(agentAdminService.statTeamData(page, size));
    }

    @GetMapping("rake/record")
    public Result rakeRecord(String phone, String bet_id, String startTime, String endTime,
                             @RequestParam(value = "page", defaultValue = "1") Integer page,
                             @RequestParam(value = "size", defaultValue = "10") Integer size){
        return Result.success(agentAdminService.rakeRecord(phone, bet_id, startTime, endTime, page, size));
    }

    @GetMapping("deposit/rechargePage")
    public Result depositPayPage(String txid, String startTime, String endTime,
                                 @RequestParam(value = "page", defaultValue = "1") Integer page,
                                 @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return Result.success(agentAdminService.depositRechargePage(txid, startTime, endTime, page, size));
    }

    @GetMapping("deposit/withdrawPage")
    public Result depositWithdrawPage(String txid, String startTime, String endTime,
                                      ChargeSettlementStatus status, DepositSettlementType settlementType,
                                      @RequestParam(value = "page", defaultValue = "1") Integer page,
                                      @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return Result.success(agentAdminService.depositWithdrawPage(txid, startTime, endTime, status, settlementType, page, size));
    }

    @GetMapping("deposit/withdraw")
    public Result getDepositWithdraw() {
        return Result.success(agentAdminService.getDepositWithdraw());
    }

    @PostMapping("deposit/withdraw/submit")
    public Result postDepositWithdraw(@RequestBody @Valid DepositWithdrawDTO deposit) {
        redisLock.lock("postDepositWithdraw_" + requestInitService.uid(), 1L, TimeUnit.MINUTES);
        if (deposit.getCurrencyType().isFiat()) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        agentAdminService.postDepositWithdraw(deposit.getWithdrawAmount(), deposit.getToAddress(), deposit.getNote(), deposit.getCurrencyType());
        return Result.success();
    }

    @Resource
    private RequestInitService requestInitService;

    @GetMapping("/dividends/page")
    public Result dividendsPage(String phone, BetResultEnum result, String startTime, String endTime,
                                @RequestParam(value = "page", defaultValue = "1") Integer page,
                                @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return Result.success(agentAdminService.dividendsPage(phone, result, startTime, endTime, page, size));
    }

    @GetMapping("/settlement/recharge/page")
    public Result settlementRechargePage(String txid, String startTime, String endTime, DepositSettlementType type,
                                         @RequestParam(value = "page", defaultValue = "1") Integer page,
                                         @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return Result.success(agentAdminService.settlementRechargePage(txid, type, startTime, endTime, page, size));
    }

    @GetMapping("/settlement/withdraw/page")
    public Result settlementWithdrawPage(String txid, ChargeSettlementStatus status, String startTime, String endTime,
                                         @RequestParam(value = "page", defaultValue = "1") Integer page,
                                         @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return Result.success(agentAdminService.settlementWithdrawPage(txid, status, startTime, endTime, page, size));
    }

    @GetMapping("/settlement/my")
    public Result settlementMyCurrency() {
        return Result.success(agentAdminService.settlementMyCurrency());
    }

    @PostMapping("/settlement/submit")
    public Result settlementWithdrawApply(@RequestBody @Valid SeniorSettlementDTO dto) {
        redisLock.lock("settlementWithdrawApply_" + requestInitService.uid(), 1L, TimeUnit.MINUTES);
        agentAdminService.settlementWithdrawApply(dto);
        return Result.success();
    }

    @GetMapping("rebate/page")
    public Result rebatePage(String phone, Long bet_id, String startTime, String endTime,
                             @RequestParam(value = "page", defaultValue = "1") Integer page,
                             @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return Result.success(agentAdminService.rebatePage(phone, bet_id, startTime, endTime, page, size));
    }

    /**
     * 添加下级代理商
     */
    @PostMapping("/saveAgent")
    public Result saveAgent(@RequestBody @Valid SaveLowAgentDTO dto) {
        agentAdminService.saveLowAgent(dto);
        return Result.success();
    }

    /**
     * 编辑下级代理商
     */
    @PostMapping("/updateAgent")
    public Result updateAgent(@RequestBody @Valid UpdateLowAgentDTO dto) {
        agentAdminService.updateLowAgent(dto);
        return Result.success();
    }

    /**
     * 删除下级代理商
     */
    @PostMapping("/deleteAgent/{id}")
    public Result deleteAgent(@PathVariable("id") Long id) {
        agentAdminService.deleteAgent(id);
        return Result.success();
    }

    /**
     * 下级代理商页面
     */
    @GetMapping("/lowAgent/page")
    public Result lowAgentPage(String nick, String phone, String startTime, String endTime,
                               @RequestParam(value = "page", defaultValue = "1") Integer page,
                               @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return Result.success(agentAdminService.lowAgentPage(nick, phone, startTime, endTime, page, size));
    }

    /**
     * 查询结算记录
     */
    @GetMapping("/lowAgent/{id}/settle/page")
    public Result lowAgentSettlePage(@PathVariable("id") Long id,
                                     LowSettlementChargeType type, String startTime, String endTime,
                                     @RequestParam(value = "page", defaultValue = "1") Integer page,
                                     @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return Result.success(agentAdminService.getLowAgentSettlePage(id, type, startTime, endTime, page, size));
    }

    /**
     * 新增结算记录
     */
    @PostMapping("/lowAgent/settle/save")
    public Result lowAgentSettleSave(@RequestBody @Valid LowSettlementDTO dto) {
        agentAdminService.saveLowAgentSettle(dto);
        return Result.success();
    }

    /**
     * 编辑结算记录
     */
    @PostMapping("/lowAgent/settle/update/{id}")
    public Result lowAgentSettleUpdate(@PathVariable("id") Long id,
                                       @RequestBody @Valid LowSettlementDTO dto) {
        agentAdminService.updateLowAgentSettle(id, dto);
        return Result.success();
    }

    /**
     * 删除结算记录
     */
    @DeleteMapping("/lowAgent/settle/delete/{id}")
    public Result lowAgentSettleDelete(@PathVariable("id") Long id) {
        agentAdminService.deleteLowAgentSettle(id);
        return Result.success();
    }

    /**
     * 新增下级保证金记录
     */
    @PostMapping("/lowAgent/deposit/save")
    public Result lowAgentDepositSave(@RequestBody @Valid LowDepositDTO dto) {
        agentAdminService.saveLowAgentDeposit(dto);
        return Result.success();
    }

    /**
     * 编辑下级保证金记录
     */
    @PostMapping("/lowAgent/deposit/update/{id}")
    public Result lowAgentDepositUpdate(@PathVariable("id") Long id,
                                        @RequestBody @Valid LowDepositDTO dto) {
        agentAdminService.updateLowAgentDeposit(id, dto);
        return Result.success();
    }

    /**
     * 删除下级保证金记录
     */
    @PostMapping("/lowAgent/deposit/delete/{id}")
    public Result lowAgentDepositUpdate(@PathVariable("id") Long id) {
        agentAdminService.deleteLowAgentDeposit(id);
        return Result.success();
    }

    /**
     * 查询下级保证金记录
     */
    @GetMapping("/lowAgent/{id}/deposit/page")
    public Result lowAgentDepositPage(@PathVariable("id") Long id,
                                      LowDepositChargeType type, String startTime, String endTime,
                                      @RequestParam(value = "page", defaultValue = "1") Integer page,
                                      @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return Result.success(agentAdminService.getLowAgentDepositPage(id, type, startTime, endTime, page, size));
    }

    /**
     * 推广配置
     */
    @GetMapping("/senior/promotion")
    public Result promotionConfiguration(){
        MapTool map = agentAdminService.promotionConfiguration();
        return Result.instance().setData(map);
    }

    /**
     * 推广配置-参数配置 普通场
     */
    @PostMapping("/senior/update/normal")
    public Result normalConfig(@RequestParam("normalRate") String normalRate){
        Double doubleRate = Double.valueOf(normalRate);
        if (doubleRate > 20) ErrorCodeEnum.throwException("返佣比例不能大于20%");
        agentAdminService.inviteRebate(doubleRate, true);
        return Result.success();
    }

    /**
     * 推广配置-参数配置 稳赚场
     */
    @PostMapping("/senior/update/steady")
    public Result steadyConfig(@RequestParam("steadyRate") String steadyRate){
        Double doubleRate = Double.valueOf(steadyRate);
        if (doubleRate > 20) ErrorCodeEnum.throwException("返佣比例不能大于20%");
        agentAdminService.inviteRebate(doubleRate, false);
        return Result.success();
    }

    @Resource
    private RedisLock redisLock;
}
