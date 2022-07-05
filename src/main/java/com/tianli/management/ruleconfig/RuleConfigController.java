package com.tianli.management.ruleconfig;

import com.google.gson.Gson;
import com.tianli.bet.BFRewardConfig;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.management.ruleconfig.mapper.BetDuration;
import com.tianli.mconfig.ConfigService;
import com.tianli.role.annotation.AdminPrivilege;
import com.tianli.role.annotation.Privilege;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("config")
public class RuleConfigController {
    @GetMapping("bet/duration")
    @AdminPrivilege(or = {Privilege.时长配置, Privilege.激活码管理})
    public Result getBetDuration(){
        List<BetDuration> list = ruleConfigService.selectAll();
        return Result.success(list.stream().map(BetDurationVO::trans).collect(Collectors.toList()));
    }

    @PostMapping("bet/duration")
    @AdminPrivilege(and = Privilege.时长配置)
    public Result updateBetDuration(@RequestBody @Valid BetDurationDTO betDuration){
        ruleConfigService.updateDuration(betDuration);
        return Result.success();
    }

    @PostMapping("update/param")
    @AdminPrivilege(and = Privilege.参数配置)
    public Result updateParam(@RequestBody @Valid ParamConfigDTO paramConfig){
        ruleConfigService.updateParam(paramConfig);
        return Result.success();
    }

    @PostMapping("/update/special/param")
    @AdminPrivilege(and = Privilege.特殊配置)
    public Result updateSpecialParam(@RequestBody @Valid SpecialParamConfigDTO paramConfig){
        ruleConfigService.updateSpecialParam(paramConfig);
        return Result.success();
    }
    @PostMapping("/update/special/param2")
    @AdminPrivilege(and = Privilege.特殊配置)
    public Result updateSpecialParam(@RequestBody List<BFRewardConfig> rewardConfigLis){
        if(CollectionUtils.isEmpty(rewardConfigLis)){
            return Result.fail(ErrorCodeEnum.ARGUEMENT_ERROR);
        }
        configService.replace(ConfigConstants.DEFAULT_BF_CONSTANT_REWARD_KEY, new Gson().toJson(rewardConfigLis));
        return Result.success();
    }

    @GetMapping("/special/param")
    @AdminPrivilege(and = Privilege.特殊配置)
    public Result specialParam(){
        SpecialParamConfigVO specialParam = ruleConfigService.getSpecialParam();
        return Result.success(specialParam);
    }

    @GetMapping("param/list")
    @AdminPrivilege(and = Privilege.参数配置)
    public Result paramList(){
        ParamConfigVO paramConfig = ruleConfigService.paramList();
        return Result.instance().setData(paramConfig);
    }

    @Resource
    private RuleConfigService ruleConfigService;

    @Resource
    private ConfigService configService;

}
