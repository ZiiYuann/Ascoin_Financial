package com.tianli.management.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.base.MoreObjects;
import com.tianli.accountred.dto.RedEnvelopStatusDTO;
import com.tianli.accountred.entity.RedEnvelope;
import com.tianli.accountred.query.RedEnvelopeGetQuery;
import com.tianli.accountred.query.RedEnvelopeGiveRecordQuery;
import com.tianli.accountred.service.RedEnvelopeService;
import com.tianli.accountred.service.RedEnvelopeSpiltService;
import com.tianli.accountred.vo.RedEnvelopeExchangeCodeVO;
import com.tianli.chain.entity.Coin;
import com.tianli.chain.enums.ChainType;
import com.tianli.chain.service.CoinService;
import com.tianli.common.Constants;
import com.tianli.common.PageQuery;
import com.tianli.common.RedisConstants;
import com.tianli.common.RedisService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.management.query.FundIncomeCompensateQuery;
import com.tianli.management.query.FundIncomeTestQuery;
import com.tianli.mconfig.ConfigService;
import com.tianli.openapi.query.OpenapiRedQuery;
import com.tianli.product.afinancial.entity.FinancialRecord;
import com.tianli.product.afinancial.service.FinancialRecordService;
import com.tianli.product.afund.entity.FundIncomeRecord;
import com.tianli.product.afund.entity.FundRecord;
import com.tianli.product.afund.service.IFundIncomeRecordService;
import com.tianli.product.afund.service.IFundRecordService;
import com.tianli.task.FinancialIncomeTask;
import com.tianli.task.FundIncomeTask;
import com.tianli.tool.IPUtils;
import com.tianli.tool.crypto.PBE;
import com.tianli.tool.time.TimeTool;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author chenb
 * @apiNote
 * @since 2022-09-20
 **/
@RestController
@RequestMapping("/management/test")
public class TestController {

    @Resource
    private IFundRecordService fundRecordService;
    @Resource
    private IFundIncomeRecordService fundIncomeRecordService;
    @Resource
    private FundIncomeTask fundIncomeTask;
    @Resource
    private ConfigService configService;
    @Resource
    private FinancialIncomeTask financialIncomeTask;
    @Resource
    private FinancialRecordService financialRecordService;
    @Resource
    private RedisService redisService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedEnvelopeService redEnvelopeService;
    @Resource
    private CoinService coinService;
    @Resource
    private RedEnvelopeSpiltService redEnvelopeSpiltService;

//    /**
//     * 基金补偿
//     */
//    @PutMapping("/fund/compensate")
//    public Result fundIncomeCompensate(@RequestBody FundIncomeCompensateQuery query) {
//        fundIncomeTask.incomeCompensate(query);
//        return Result.success();
//    }

//    /**
//     * 交易记录
//     */
//    @PutMapping("/fund/income")
//    public Result fundIncome(@RequestBody FundIncomeTestQuery query) {
//
//        configService.get("taskTest");
//
//        LocalDateTime now = MoreObjects.firstNonNull(query.getNow(), LocalDateTime.now());
//        LocalDateTime nowZero = TimeTool.minDay(now);
//        LambdaQueryWrapper<FundRecord> eq = new LambdaQueryWrapper<FundRecord>()
//                .eq(FundRecord::getUid, query.getUid());
//        if (Objects.nonNull(query.getRecordId())) {
//            eq = eq.eq(FundRecord::getId, query.getRecordId());
//        }
//        List<FundRecord> list = fundRecordService.list(eq);
//        list.forEach(record -> {
//            List<FundIncomeRecord> incomeRecords = Optional.ofNullable(fundIncomeRecordService.list(new LambdaQueryWrapper<FundIncomeRecord>()
//                            .eq(FundIncomeRecord::getFundId, record.getId())
//                            .orderByDesc(FundIncomeRecord::getCreateTime)
//                    )
//            ).orElse(new ArrayList<>());
//
//            if (CollectionUtils.isNotEmpty(incomeRecords)) {
//                FundIncomeRecord fundIncomeRecordFirst = incomeRecords.get(0);
//                if (fundIncomeRecordFirst.getCreateTime().equals(nowZero)) {
//                    FundIncomeRecord fundIncomeRecordLast = incomeRecords.get(incomeRecords.size() - 1);
//                    fundIncomeRecordFirst.setCreateTime(fundIncomeRecordLast.getCreateTime().plusDays(-1));
//                    fundIncomeRecordService.updateById(fundIncomeRecordFirst);
//                }
//            }
//
//            // 计息时间为4天后，所以手动修改为5天前
//            record.setCreateTime(MoreObjects.firstNonNull(query.getCreateTime(), now.plusDays(-8)));
//            // 利息修改为前一天的时间
//            for (int i = 0; i < incomeRecords.size(); i++) {
//                FundIncomeRecord fundIncomeRecord = incomeRecords.get(i);
//                fundIncomeRecord.setCreateTime(nowZero.plusDays(-(i + 1)));
//            }
//
//
//            fundRecordService.updateById(record);
//
//            fundIncomeTask.calculateIncome(record, now);
//        });
//
//        return Result.success();
//    }


    /**
     * 交易记录
     */
//    @PutMapping("/financial/income")
//    public Result financialIncome(@RequestBody FundIncomeTestQuery query) {
//        configService.get("taskTest");
//
//        LocalDateTime createTime = query.getCreateTime();
//
//        FinancialRecord financialRecord = financialRecordService.selectById(query.getRecordId(), query.getUid());
//
//        // 重新设置时间
//        financialRecord.setPurchaseTime(createTime);
//        financialRecord.setStartIncomeTime(createTime.toLocalDate().plusDays(1).atStartOfDay());
//        financialRecord.setEndTime(financialRecord.getStartIncomeTime().plusDays(financialRecord.getProductTerm().getDay()));
//        financialRecordService.updateById(financialRecord);
//
//        financialIncomeTask.incomeExternalTranscation(financialRecord, query.getNow());
//        return Result.success();
//    }

//    @PostMapping("/fund/income/rollback")
//    public Result incomeRollback(Long incomeId) {
//        fundIncomeRecordService.rollback(incomeId);
//        return Result.success();
//    }
    @GetMapping("/rds")
    public Result rdsGet(String key) {
        Set<String> keys = stringRedisTemplate.keys("*" + key + "*");
        if (CollectionUtils.isEmpty(keys)){
            return new Result("没有key");
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
        stringRedisTemplate.opsForValue().set(key, value, 1, TimeUnit.DAYS);
        return new Result<>();
    }

    @GetMapping("/coin/push")
    public Result<Void> coinPush(String contract) {
        Coin coin = coinService.getByContract(contract);
        coinService.push(coin);
        return new Result<>();
    }

    @GetMapping("/coin/push/main/{chain}")
    public Result<Void> coinPushMain(@PathVariable ChainType chain) {
        // 这个接口只适用于BSC、TRON、ETH链 其他推送无效
        Coin coin = coinService.mainToken(chain, chain.getMainToken());
        coinService.push(coin);
        return new Result<>();
    }

    /**
     * 领取站外红包 垃圾需求逼我写垃圾代码
     */
    @GetMapping("/red/extern")
    public Result<RedEnvelopeExchangeCodeVO> externRedGet(String fingerprint, String ip, String id) {
        String fingerprintKey = RedisConstants.RED_ENVELOPE_LIMIT + fingerprint + ":" + id;

        RedEnvelopStatusDTO redEnvelopStatusDTO;
        if ((redEnvelopStatusDTO = redEnvelopeSpiltService.getIpOrFingerDTO(fingerprint, Long.valueOf(id))) != null) {
            return new Result<>(redEnvelopStatusDTO);
        }
        return new Result<>(redEnvelopeSpiltService.getExchangeCode(Long.parseLong(id), ip, fingerprintKey));
    }

    @PostMapping("/red/get")
    public Result redGet(Long uid, Long shortUid, @RequestBody @Valid RedEnvelopeGetQuery query) {
        if (Objects.isNull(shortUid)) {
            ErrorCodeEnum.ACCOUNT_ERROR.throwException();
        }
        return Result.success().setData(redEnvelopeService.get(uid, shortUid, query));
    }
}
