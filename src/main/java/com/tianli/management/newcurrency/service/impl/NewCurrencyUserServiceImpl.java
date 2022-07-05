package com.tianli.management.newcurrency.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.exception.Result;
import com.tianli.management.newcurrency.dao.NewCurrencyManagementMapper;
import com.tianli.management.newcurrency.dao.NewCurrencyUserMapper;
import com.tianli.management.newcurrency.entity.NewCurrencyManagement;
import com.tianli.management.newcurrency.entity.NewCurrencySumDTO;
import com.tianli.management.newcurrency.entity.NewCurrencyUser;
import com.tianli.management.newcurrency.entity.NewCurrencyUserDTO;
import com.tianli.management.newcurrency.service.INewCurrencyUserService;
import com.tianli.newcurrency.NewCurrencyDayDTO;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 新币用户表 服务实现类
 * </p>
 *
 * @author cc
 * @since 2022-06-16
 */
@Service
public class NewCurrencyUserServiceImpl extends ServiceImpl<NewCurrencyUserMapper, NewCurrencyUser> implements INewCurrencyUserService {

    @Autowired
    private NewCurrencyUserMapper userMapper;
    @Autowired
    private NewCurrencyManagementMapper managementMapper;

    @Override
    public Result page(NewCurrencyUserDTO dto, Long current, Long size) {
        String currencyName = dto.getCurrencyName();
        String startTime = dto.getStartTime();
        String endTime = dto.getEndTime();
        QueryWrapper<NewCurrencyUser> queryWrapper = new QueryWrapper<>();
        if (currencyName != null && currencyName != "") {
            queryWrapper.like("currency_name", currencyName);
        }
        if (startTime != null && startTime != "") {
            queryWrapper.ge("amount_buy_time", startTime);
        }
        if (endTime != null && endTime != "") {
            queryWrapper.le("amount_buy_time", endTime);
        }
        queryWrapper.orderByDesc("amount_buy_time");
        Page<NewCurrencyUser> page = userMapper.selectPage(new Page<>(current, size), queryWrapper);
        page.getRecords().forEach(p->{
            p.transferTime();
        });
        return Result.success(page);
    }

    @Override
    public Result sumNewCurrency() {
        NewCurrencySumDTO sum= userMapper.sumNewCurrency();
        return Result.success(sum);
    }

    @Override
    public NewCurrencySumDTO sumNewCurrencyByCurrencyName(Long currencyId) {
        return userMapper.sumNewCurrencyByCurrencyName(currencyId);
    }

    @Override
    public NewCurrencyDayDTO laungchpad() {
        return userMapper.laungchpad();
    }

    @Override
    public List<NewCurrencySumDTO> selectNewCurrency() {
        return userMapper.selectNewCurrency();
    }

}
