package com.tianli.management.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tianli.account.query.IdsQuery;
import com.tianli.accountred.dto.RedEnvelopStatusDTO;
import com.tianli.accountred.service.RedEnvelopeService;
import com.tianli.accountred.service.RedEnvelopeSpiltService;
import com.tianli.accountred.vo.RedEnvelopeExchangeCodeVO;
import com.tianli.common.RedisConstants;
import com.tianli.exception.Result;
import com.tianli.management.query.FundIncomeTestQuery;
import com.tianli.mconfig.ConfigService;
import com.tianli.product.afinancial.entity.FinancialRecord;
import com.tianli.product.afinancial.service.FinancialRecordService;
import com.tianli.task.FinancialIncomeTask;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Set;

/**
 * @author chenb
 * @apiNote
 * @since 2022-09-20
 **/
@RestController
@RequestMapping("/management/test")
public class TestController {

    @Resource
    private ConfigService configService;
    @Resource
    private FinancialIncomeTask financialIncomeTask;
    @Resource
    private FinancialRecordService financialRecordService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedEnvelopeService redEnvelopeService;
    @Resource
    private RedEnvelopeSpiltService redEnvelopeSpiltService;

    @PostMapping("/red/bloom/delete")
    public Result<Void> redBloomDelete() {
        redEnvelopeService.deleteBloomFilter();
        return new Result<>();

    }

    @PostMapping("/red/bloom/init")
    public Result<Void> redBloomInit() {
        redEnvelopeService.initBloomFilter();
        return new Result<>();
    }

    @GetMapping("/rds")
    public Result<?> rdsGet(String key) {
        Set<String> keys = stringRedisTemplate.keys("*" + key + "*");
        if (CollectionUtils.isEmpty(keys)) {
            return Result.success("没有key");
        }
        HashMap<String, String> map = new HashMap<>();
        keys.forEach(index ->
        {
            String value = stringRedisTemplate.opsForValue().get(index);
            map.put(index, value);

        });

        return Result.success(map);
    }

    @DeleteMapping("/rds")
    public Result<Void> rdsDelete(String key) {
        stringRedisTemplate.delete(key);
        return new Result<>();
    }

    @PostMapping("/rds")
    public Result<Void> rdsSaveOrUpdate(@RequestBody String str) {
        JSONObject jsonObject = JSONUtil.parseObj(str);
        String key = jsonObject.getStr("key");
        String value = jsonObject.getStr("value");
        stringRedisTemplate.opsForValue().set(key, value);
        return new Result<>();
    }

    /**
     * 交易记录
     */
    @PutMapping("/financial/income")
    public Result<Void> financialIncome(@RequestBody FundIncomeTestQuery query) {
        configService.get("taskTest");

        LocalDateTime createTime = query.getCreateTime();

        FinancialRecord financialRecord = financialRecordService.selectById(query.getRecordId(), query.getUid());

        // 重新设置时间
        financialRecord.setPurchaseTime(createTime);
        financialRecord.setStartIncomeTime(createTime.toLocalDate().plusDays(1).atStartOfDay());
        financialRecord.setEndTime(financialRecord.getStartIncomeTime().plusDays(financialRecord.getProductTerm().getDay()));
        financialRecordService.updateById(financialRecord);

        financialIncomeTask.incomeExternalTranscation(financialRecord, query.getNow());
        return Result.success();
    }

    @GetMapping("/red/extern")
    public Result<RedEnvelopeExchangeCodeVO> externRedGet(String fingerprint, String ip, String id) {
        RedEnvelopStatusDTO redEnvelopStatusDTO;
        if ((redEnvelopStatusDTO = redEnvelopeSpiltService.getIpOrFingerDTO(fingerprint, Long.valueOf(id))) != null) {
            return new Result<>(redEnvelopStatusDTO);
        }
        return new Result<>(redEnvelopeSpiltService.getExchangeCode(Long.parseLong(id), ip, fingerprint));
    }

    /**
     * 提现黑名单
     */
    @PostMapping("/withdraw/black")
    public Result<Void> withdraw(@RequestBody @Valid IdsQuery query) {
        stringRedisTemplate.opsForSet().add(RedisConstants.WITHDRAW_BLACK, query.getId() + "");
        return Result.success();
    }



}
