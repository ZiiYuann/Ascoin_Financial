package com.tianli.management.newcurrency.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.CommonFunction;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.currency_token.token.TokenListService;
import com.tianli.currency_token.token.mapper.TokenList;
import com.tianli.currency_token.transfer.mapper.TokenContract;
import com.tianli.currency_token.transfer.service.TokenContractService;
import com.tianli.exception.Result;
import com.tianli.management.newcurrency.dao.NewCurrencyManagementMapper;
import com.tianli.management.newcurrency.entity.NewCurrencyManagement;
import com.tianli.management.newcurrency.entity.NewCurrencyManagementDTO;
import com.tianli.management.newcurrency.entity.NewCurrencyUserType;
import com.tianli.management.newcurrency.service.INewCurrencyManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * <p>
 * 新币管理端 服务实现类
 * </p>
 *
 * @author cc
 * @since 2022-06-16
 */
@Service
public class NewCurrencyManagementServiceImpl extends ServiceImpl<NewCurrencyManagementMapper, NewCurrencyManagement> implements INewCurrencyManagementService {

    @Autowired
    private NewCurrencyManagementMapper newCurrencyManagementMapper;
    @Autowired
    private TokenListService tokenListService;
    @Autowired
    private TokenContractService tokenContractService;

    @Override
    public Result page(NewCurrencyManagementDTO dto, Long current, Long size) {
        String currencyName = dto.getCurrencyName();
        String startTime = dto.getStartTime();
        String endTime = dto.getEndTime();
        QueryWrapper<NewCurrencyManagement> queryWrapper = new QueryWrapper<>();
        if (currencyName != null && currencyName != "") {
            queryWrapper.like("currency_name", currencyName);
        }
        if (startTime != null && startTime != "") {
            queryWrapper.ge("create_time", startTime);
        }
        if (endTime != null && endTime != "") {
            queryWrapper.le("create_time", endTime);
        }
        queryWrapper.eq("is_delete",0);
        queryWrapper.orderByDesc("create_time");
        Page<NewCurrencyManagement> page = newCurrencyManagementMapper.selectPage(new Page<>(current, size), queryWrapper);

        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        page.getRecords().forEach(p->{
            LocalDateTime time1 = LocalDateTime.parse(p.getTime1(), df);
            LocalDateTime time2 = LocalDateTime.parse(p.getTime2(), df);
            LocalDateTime time3 = LocalDateTime.parse(p.getTime3(), df);
            LocalDateTime time4 = LocalDateTime.parse(p.getTime4(), df);
            String type=NewCurrencyUserType.kind3.getCode();//"统计中";
            if(now.isAfter(time4)){
                type=NewCurrencyUserType.kind0.getCode();//"已完成"
            } else if (now.isAfter(time3)) {
                type=NewCurrencyUserType.kind1.getCode();//"待计算"
            } else if (now.isAfter(time2)) {
                type=NewCurrencyUserType.kind2.getCode();//"待投入"
            } else if (now.isAfter(time1)) {
                type=NewCurrencyUserType.kind3.getCode();//统计中
            }
            p.setType(type);
        });
        return Result.success(page);
    }

    @Override
    @Transactional
    public Result insert(NewCurrencyManagement newCurrencyManagement) {
        long id = CommonFunction.generalId();
        newCurrencyManagement.setId(id);
        newCurrencyManagement.setIs_delete(0);
        newCurrencyManagement.setCreate_time(LocalDateTime.now().toString());
        newCurrencyManagementMapper.insert(newCurrencyManagement);
        //保存token列表
        TokenList sort=tokenListService.getOne(new LambdaQueryWrapper<TokenList>().orderByDesc(TokenList::getSort).last("limit 1"));
        CurrencyCoinEnum currencyCoinEnum = CurrencyCoinEnum.getCurrencyCoinEnum(newCurrencyManagement.getCurrency_name_short().toLowerCase());
        TokenList tokenList = newCurrencyManagement.getTokenList();
        tokenList.setToken(currencyCoinEnum);
        tokenList.setName_short(newCurrencyManagement.getCurrency_name_short());
        tokenList.setName_full(newCurrencyManagement.getCurrency_name_short());
        tokenList.setActual_enable(0);
        tokenList.setNormal_enable(0);
        tokenList.setSort(sort.getSort()+1);
        tokenList.setPlatform_token(Boolean.TRUE);
        tokenListService.save(tokenList);
        //保存链合约地址绑定表
        List<TokenContract> list = newCurrencyManagement.getTokenContract();
        for (TokenContract tokenContract : list) {
            tokenContract.setToken(currencyCoinEnum);
            tokenContract.setPlatform_token(Boolean.TRUE);
        }
        tokenContractService.saveBatch(list);
        return Result.success("新增成功");
    }

    @Override
    @Transactional
    public Result updateByEntity(NewCurrencyManagement newCurrencyManagement) {
        newCurrencyManagementMapper.updateById(newCurrencyManagement);
        //保存token列表
        CurrencyCoinEnum currencyCoinEnum = CurrencyCoinEnum.getCurrencyCoinEnum(newCurrencyManagement.getCurrency_name_short().toLowerCase());
        TokenList tokenList = newCurrencyManagement.getTokenList();
        tokenList.setToken(currencyCoinEnum);
        tokenList.setName_short(newCurrencyManagement.getCurrency_name_short());
        tokenList.setName_full(newCurrencyManagement.getCurrency_name_short());
        tokenList.setActual_enable(null);
        tokenList.setNormal_enable(null);
        tokenList.setSort(null);
        tokenListService.updateById(tokenList);
        //删除
        tokenContractService.remove(new LambdaQueryWrapper<TokenContract>().eq(TokenContract::getToken,currencyCoinEnum));
        //保存链合约地址绑定表
        List<TokenContract> list = newCurrencyManagement.getTokenContract();
        list.forEach(tokenContract -> tokenContract.setPlatform_token(Boolean.TRUE));
        list.removeIf(s -> s.getContract_address()=="" ||s.getContract_address()==null);
        tokenContractService.saveBatch(list);
        return Result.success(newCurrencyManagement);
    }

    @Override
    @Transactional
    public Result deleteById(Long id) {
        NewCurrencyManagement newCurrencyManagement = new NewCurrencyManagement();
        newCurrencyManagement.setId(id);
        newCurrencyManagement.setIs_delete(1);
        newCurrencyManagementMapper.updateById(newCurrencyManagement);
        return Result.success("删除成功");
    }

    @Override
    public Result getListById(Long id) {
        NewCurrencyManagement p = newCurrencyManagementMapper.selectById(id);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime time1 = LocalDateTime.parse(p.getTime1(), df);
        LocalDateTime time2 = LocalDateTime.parse(p.getTime2(), df);
        LocalDateTime time3 = LocalDateTime.parse(p.getTime3(), df);
        LocalDateTime time4 = LocalDateTime.parse(p.getTime4(), df);
        String type=NewCurrencyUserType.kind3.getCode();//"统计中";
        if(now.isAfter(time4)){
            type=NewCurrencyUserType.kind0.getCode();//"已完成"
        } else if (now.isAfter(time3)) {
            type=NewCurrencyUserType.kind1.getCode();//"待计算"
        } else if (now.isAfter(time2)) {
            type=NewCurrencyUserType.kind2.getCode();//"待投入"
        } else if (now.isAfter(time1)) {
            type=NewCurrencyUserType.kind3.getCode();//统计中
        }
        p.setType(type);
        //查询tokenlist和tokencontract
        CurrencyCoinEnum currencyCoinEnum = CurrencyCoinEnum.getCurrencyCoinEnum(p.getCurrency_name_short().toLowerCase());
        TokenList tokenList = tokenListService.getOne(new LambdaQueryWrapper<TokenList>().eq(TokenList::getToken, currencyCoinEnum));
        List<TokenContract> tokenContractList = tokenContractService.list(new LambdaQueryWrapper<TokenContract>().eq(TokenContract::getToken, currencyCoinEnum));
        p.setTokenList(tokenList);
        p.setTokenContract(tokenContractList);
        return Result.success(p);
    }

    @Override
    public List<NewCurrencyManagement> queryOnlineToken() {
        return this.list(Wrappers.lambdaQuery(NewCurrencyManagement.class)
                .le(NewCurrencyManagement::getTime5,LocalDateTimeUtil.formatNormal(LocalDateTimeUtil.now())));
    }

}
