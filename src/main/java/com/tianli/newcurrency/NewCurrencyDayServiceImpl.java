package com.tianli.newcurrency;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.CommonFunction;
import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.currency_token.CurrencyTokenService;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.currency_token.mapper.CurrencyTokenMapper;
import com.tianli.currency_token.token.TokenListService;
import com.tianli.currency_token.token.mapper.TokenList;
import com.tianli.exception.Result;
import com.tianli.management.newcurrency.dao.NewCurrencyManagementMapper;
import com.tianli.management.newcurrency.entity.*;
import com.tianli.management.newcurrency.service.INewCurrencyManagementService;
import com.tianli.management.newcurrency.service.INewCurrencyUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * <p>
 * 每天0点用户币余额 服务实现类
 * </p>
 *
 * @author cc
 * @since 2022-06-16
 */
@Service
public class NewCurrencyDayServiceImpl extends ServiceImpl<NewCurrencyDayMapper, NewCurrencyDay> implements INewCurrencyDayService {

    @Autowired
    private NewCurrencyDayMapper newCurrencyDayMapper;

    @Autowired
    private CurrencyTokenMapper currencyTokenMapper;
    @Autowired
    private CurrencyTokenService currencyToken;
    @Autowired
    private NewCurrencyManagementMapper newCurrencyManagementMapper;
    @Autowired
    private INewCurrencyManagementService managementService;
    @Autowired
    private INewCurrencyUserService iNewCurrencyUserService;

    @Resource
    TokenListService tokenListService;

    @Override
    public void syncSaveCurrency() {
        LocalDateTime now = LocalDateTime.now();
        List<NewCurrencyDay> list = newCurrencyDayMapper.getCurrencyByDay();
        list.stream().forEach(newCurrencyDay -> newCurrencyDay.setCreate_time(now));
        this.saveBatch(list);
    }

    @Override
    public Result selectNewCurrecy(Long currencyId, Long uid) {
        //配置数据
        NewCurrencyManagement one = newCurrencyManagementMapper.selectOne(
                new LambdaQueryWrapper<NewCurrencyManagement>()
                        .eq(NewCurrencyManagement::getId, currencyId)
                        .eq(NewCurrencyManagement::getIs_delete, "0")
        );
        //用户在time1和time4之间的余额数据用来计算USDT平均持仓
        BigDecimal maxInputSum = new BigDecimal(BigInteger.ZERO);////可投入最大额度
        BigDecimal avgSum = new BigDecimal("0");// 现货USDT日平均持仓
        NewCurrencyUser currencyUser = new NewCurrencyUser();
        if (uid != null) {
            List<NewCurrencyDay> newCurrencyDays = newCurrencyDayMapper.selectList(
                    new LambdaQueryWrapper<NewCurrencyDay>().ge(NewCurrencyDay::getCreate_time, one.getTime1())
                            .le(NewCurrencyDay::getCreate_time, one.getTime4()).eq(NewCurrencyDay::getUid, uid)
            );
            BigDecimal sum = newCurrencyDays.stream()
                    .filter(p -> p.getRemain() != null)
                    .map(NewCurrencyDay::getRemain)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);//总和
            if (newCurrencyDays.size() != 0) {
                avgSum = sum.divide(new BigDecimal(newCurrencyDays.size()), 18, RoundingMode.DOWN);//项目需求向下取，确保不超买
            }
            /*待投入*/
            BigDecimal selfMaxCurrency = one.getSelf_max_currency();//个人最大硬顶
            maxInputSum = avgSum.compareTo(selfMaxCurrency) < 0 ? avgSum : selfMaxCurrency;
            //确认投入
            //扣款数量
            //获得代币数量
            currencyUser = iNewCurrencyUserService.getOne(
                    new LambdaQueryWrapper<NewCurrencyUser>().eq(NewCurrencyUser::getCurrency_id, currencyId)
                            .eq(NewCurrencyUser::getUid, uid)
            );
            if (currencyUser != null) {
                currencyUser.transferTime();
            }
        }

        //USDT投入总量
        //参与人数
        NewCurrencySumDTO allSum = iNewCurrencyUserService.sumNewCurrencyByCurrencyName(currencyId);
        //状态
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime time1 = LocalDateTime.parse(one.getTime1(), df);
        LocalDateTime time2 = LocalDateTime.parse(one.getTime2(), df);
        LocalDateTime time3 = LocalDateTime.parse(one.getTime3(), df);
        LocalDateTime time4 = LocalDateTime.parse(one.getTime4(), df);
        LocalDateTime now = LocalDateTime.now();
        String type = NewCurrencyUserType.kind3.getCode();//"统计中";
        if (now.isAfter(time4)) {
            type = NewCurrencyUserType.kind0.getCode();//"已完成"
        } else if (now.isAfter(time3)) {
            type = NewCurrencyUserType.kind1.getCode();//"待计算"
        } else if (now.isAfter(time2)) {
            type = NewCurrencyUserType.kind2.getCode();//"待投入"
        } else if (now.isAfter(time1)) {
            type = NewCurrencyUserType.kind3.getCode();//统计中
        }
        HashMap<String, Object> map = new HashMap<>();
        /*时间转为时间戳*/
        if (one != null) {
            one.transferTime();
        }

        map.put("one", one);
        map.put("avgSum", avgSum);
        map.put("maxInputSum", maxInputSum);
        map.put("currencyUser", currencyUser);
        map.put("allSum", allSum);
        map.put("type", type);
        return Result.success(map);
    }

    @Override
    public Result getNewCurrency() {
        List<NewCurrencyManagement> list = newCurrencyManagementMapper.selectList(
                new LambdaQueryWrapper<NewCurrencyManagement>().eq(NewCurrencyManagement::getIs_delete, "0")
        );
        //转为字典
        List<NewCurrencyManagementDictCodeDTO> dictcode = list.stream().map(p -> {
            return NewCurrencyManagementDictCodeDTO.builder().currencyName(p.getCurrency_name()).currencyId(p.getId()).build();
        }).collect(Collectors.toList());
        return Result.success(dictcode);
    }

    @Override
    public Result tradingRecord(Long uid, Long current, Long size) {
        Page<NewCurrencyUser> page = iNewCurrencyUserService.page(
                new Page<>(current, size), new LambdaQueryWrapper<NewCurrencyUser>().eq(NewCurrencyUser::getUid, uid));
        page.getRecords().forEach(p -> {
            p.transferTime();
        });
        return Result.success(page);
    }

    @Override
    public Result laungchpad(Long current, Long size) {
        //各个币种统计人数和总额
        List<NewCurrencySumDTO> allSum = iNewCurrencyUserService.selectNewCurrency();
        Map<String, String> toSum = allSum.stream().collect(Collectors.toMap(NewCurrencySumDTO::getCurrencyName, NewCurrencySumDTO::getSum));
        Map<String, String> toPersonNumber = allSum.stream().collect(Collectors.toMap(NewCurrencySumDTO::getCurrencyName, NewCurrencySumDTO::getPersonNumber));

        QueryWrapper<NewCurrencyManagement> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_delete", 0);
        queryWrapper.orderByDesc("create_time");
        Page<NewCurrencyManagement> page = newCurrencyManagementMapper.selectPage(new Page<>(current, size), queryWrapper);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        page.getRecords().forEach(p -> {
            /*时间转为时间戳*/
            p.transferTime();
            p.setPersonNumber(toPersonNumber.get(p.getCurrency_name()));//参与人数
            p.setSum(toSum.get(p.getCurrency_name()));//投入总额
            LocalDateTime time1 = LocalDateTime.parse(p.getTime1(), df);
            LocalDateTime time2 = LocalDateTime.parse(p.getTime2(), df);
            LocalDateTime time3 = LocalDateTime.parse(p.getTime3(), df);
            LocalDateTime time4 = LocalDateTime.parse(p.getTime4(), df);
            String type = NewCurrencyUserType.kind3.getCode();//"统计中";
            if (now.isAfter(time4)) {
                type = NewCurrencyUserType.kind0.getCode();//"已完成"
            } else if (now.isAfter(time3)) {
                type = NewCurrencyUserType.kind1.getCode();//"待计算"
            } else if (now.isAfter(time2)) {
                type = NewCurrencyUserType.kind2.getCode();//"待投入"
            } else if (now.isAfter(time1)) {
                type = NewCurrencyUserType.kind3.getCode();//统计中
            }
            p.setType(type);
        });
        NewCurrencyDayDTO newCurrencyDayDTO = iNewCurrencyUserService.laungchpad();
        HashMap<String, Object> map = new HashMap<>();
        map.put("newCurrencyDayDTO", newCurrencyDayDTO);
        map.put("page", page);
        return Result.success(map);
    }

    @Autowired
    private CurrencyTokenService currencyTokenService;

    @Override
    @Transactional
    public Result inputConfirm(NewCurrencyUser newCurrencyUser) {
        //简称和代币名称，价格
        NewCurrencyManagement one = newCurrencyManagementMapper.selectById(newCurrencyUser.getCurrency_id());
        newCurrencyUser.setCurrency_name_short(one.getCurrency_name_short());
        newCurrencyUser.setToken(one.getToken());
        newCurrencyUser.setSale_price(one.getSale_price());
        //新增new_currency_user
        newCurrencyUser.setId(CommonFunction.generalId());
        newCurrencyUser.setAmount_buy_time(LocalDateTime.now());
        newCurrencyUser.setType(NewCurrencyType.lock.name());
        iNewCurrencyUserService.save(newCurrencyUser);
        //修改currency_token表
        Long sn = newCurrencyUser.getId();
        currencyTokenService.freeze(newCurrencyUser.getUid(),
                CurrencyTypeEnum.actual, CurrencyCoinEnum.usdt, newCurrencyUser.getAmount_buy(), sn.toString(), CurrencyLogDes.交易);
        return Result.instance();
    }


    private void online() {
        List<NewCurrencyManagement> managementList = managementService.list(new LambdaQueryWrapper<NewCurrencyManagement>()
                .eq(NewCurrencyManagement::getIs_delete, "0")
                .eq(NewCurrencyManagement::getOnline_processing, Boolean.FALSE)
                .le(NewCurrencyManagement::getTime5, LocalDateTime.now().toString()));
        if (CollUtil.isEmpty(managementList)) {
            return;
        }
        for (NewCurrencyManagement newCurrencyManagement : managementList) {
            TokenList tokenList = tokenListService.getOne(Wrappers.lambdaQuery(TokenList.class)
                    .eq(TokenList::getName_short, newCurrencyManagement.getCurrency_name_short())
                    .eq(TokenList::getActual_enable, 0));
            if (ObjectUtil.isNotNull(tokenList)) {
                tokenList.setActual_enable(1);
                tokenListService.updateById(tokenList);
            }
            newCurrencyManagement.setOnline_processing(Boolean.TRUE);
            managementService.updateById(newCurrencyManagement);
        }
    }

    @Override
    @Transactional
    public void syncComputedCurrency() {
        //判断是否上线新币
        this.online();
        /*
        投入<个人硬顶
	        实际扣款=投入
	        获得代币=实际扣款/出售价格
	        返还=0
        投入>个人硬顶
            实际扣款=个人硬顶
            获得代币=实际扣款/出售价格
            返还=投入-个人硬顶
        * */
        List<NewCurrencyManagement> managementList = managementService.list(new LambdaQueryWrapper<NewCurrencyManagement>()
                .eq(NewCurrencyManagement::getIs_delete, "0")
                .le(NewCurrencyManagement::getTime4, LocalDateTime.now().toString())//当前时间大于分发时间就计算代币
        );
        //获得配置表的个人硬顶，出售价格
        Map<Long, NewCurrencyManagement> entityMap = managementList.stream().collect(Collectors.toMap(NewCurrencyManagement::getId, p -> p));
        //币名id列表
        List<Long> currencyIdList = managementList.stream().map(NewCurrencyManagement::getId).collect(Collectors.toList());
        if (currencyIdList.size() == 0) {
            return;//没有需要处理的用户数据
        }
        List<NewCurrencyUser> currencyUserList = iNewCurrencyUserService.list(
                new LambdaQueryWrapper<NewCurrencyUser>()
                        .eq(NewCurrencyUser::getType, NewCurrencyType.lock.name())
                        .in(NewCurrencyUser::getCurrency_id, currencyIdList)
        );
        //币名id->新币用户列表
        Map<Long, List<NewCurrencyUser>> currentUserMap = currencyUserList.stream()
                .collect(Collectors.groupingBy(NewCurrencyUser::getCurrency_id));
        currentUserMap.forEach(new BiConsumer<Long, List<NewCurrencyUser>>() {
            @Override
            public void accept(Long currencyId, List<NewCurrencyUser> newCurrencyUsers) {
                //某用户应分配代币数量<=个人硬顶
                List<NewCurrencyUser> withInList = new ArrayList<>();
                //某用户应分配代币数量>个人硬顶
                List<NewCurrencyUser> withOutList = new ArrayList<>();
                withInList = newCurrencyUsers.stream().filter(p -> {
                            return p.getAmount_buy().compareTo(entityMap.get(currencyId).getSelf_max_currency()) <= 0;//投入<=硬顶
                        }
                ).collect(Collectors.toList());
                withOutList = newCurrencyUsers.stream().filter(p -> {
                            return p.getAmount_buy().compareTo(entityMap.get(currencyId).getSelf_max_currency()) > 0;//投入>硬顶
                        }
                ).collect(Collectors.toList());
                //处理扣款，返回，代币，状态
                if (withOutList.size() == 0) {
                    withInList.forEach(p -> {
                        p.setAmount_reduce(p.getAmount_buy());//扣款=投入
                        p.setCurrency_count(p.getAmount_reduce().divide(entityMap.get(currencyId).getSale_price(), 18, RoundingMode.DOWN));//代币=扣款/价格
                        p.setAmount_return(new BigDecimal(BigInteger.ZERO));//返回0
                        p.setType(NewCurrencyType.deduct.name());
                    });
                } else {
                    for (NewCurrencyUser p : withOutList) {
                        p.setAmount_reduce(entityMap.get(currencyId).getSelf_max_currency());//扣款=个人硬顶
                        p.setCurrency_count(p.getAmount_reduce().divide(entityMap.get(currencyId).getSale_price(), 18, RoundingMode.DOWN));//代币=扣款/价格
                        p.setAmount_return(p.getAmount_buy().subtract(p.getAmount_reduce()));//返回=投入-个人硬顶
                        p.setType(NewCurrencyType.deduct.name());
                    }
                    withInList.forEach(p -> {
                        p.setAmount_reduce(p.getAmount_buy());//扣款=投入
                        p.setCurrency_count(p.getAmount_reduce().divide(entityMap.get(currencyId).getSale_price(), 18, RoundingMode.DOWN));//代币=扣款/价格
                        p.setAmount_return(new BigDecimal(BigInteger.ZERO));//返回0
                        p.setType(NewCurrencyType.deduct.name());
                    });
                }
            }
        });
        iNewCurrencyUserService.saveOrUpdateBatch(currencyUserList);
        //保存到用户币表和日志中
        currencyUserList.forEach(p -> {
            CurrencyCoinEnum currencyCoinEnum = CurrencyCoinEnum.getCurrencyCoinEnum(p.getCurrency_name_short().toLowerCase());
            //USDT变化
            currencyTokenService.unfreeze(p.getUid(), CurrencyTypeEnum.actual, CurrencyCoinEnum.usdt, p.getAmount_buy(), String.valueOf(p.getId()), CurrencyLogDes.现货交易);
            currencyTokenService.decrease(p.getUid(), CurrencyTypeEnum.actual, CurrencyCoinEnum.usdt, p.getAmount_buy(), String.valueOf(p.getId()), CurrencyLogDes.扣款);
            currencyTokenService.increase(p.getUid(), CurrencyTypeEnum.actual, CurrencyCoinEnum.usdt, p.getAmount_return(), String.valueOf(p.getId()), CurrencyLogDes.回款);
            //新币变化
            currencyTokenService.increase(p.getUid(), CurrencyTypeEnum.actual, currencyCoinEnum, p.getCurrency_count(), String.valueOf(p.getId()), CurrencyLogDes.奖励);
        });
    }

}
