package com.tianli.management.financial.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.tianli.currency.enums.CurrencyAdaptType;
import com.tianli.exception.Result;
import com.tianli.financial.service.FinancialProductService;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.management.financial.dao.FinancialUserMapper;
import com.tianli.management.financial.dto.FinancialUserListDto;
import com.tianli.management.financial.dto.FinancialUserRecordListDto;
import com.tianli.management.financial.dto.FinancialUserTotalDto;
import com.tianli.management.financial.vo.FinancialUserListVo;
import com.tianli.management.financial.vo.FinancialUserRecordListVo;
import com.tianli.sso.permission.AdminPrivilege;
import com.tianli.sso.permission.Privilege;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.*;

/**
 * @author lzy
 * @date 2022/4/1 6:20 下午
 */
@RestController
@RequestMapping("/management/financial/user/")
public class FinancialUserController {

    @Resource
    FinancialProductService financialProductService;

    @Resource
    FinancialUserMapper financialUserMapper;


    @GetMapping("/list")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result list(FinancialUserListDto financialUserListDto) {
        FinancialUserListVo financialUserListVo = new FinancialUserListVo();
        Map<Object, Object> result = MapUtil.builder()
                .put("page", financialUserListDto.getPage())
                .put("size", financialUserListDto.getSize())
                .put("records", financialUserListVo).build();
        Long total = financialUserMapper.total(financialUserListDto);
        if (ObjectUtil.isNull(total) || total <= 0L) {
            result.put("total", 0);
            return Result.success(result);
        }
        result.put("total", total);
        Integer page = (financialUserListDto.getPage() - 1) * financialUserListDto.getSize();
        Integer size = financialUserListDto.getSize();
        List<FinancialUserRecordListDto> financialUserRecordListDtos = financialUserMapper.page(page, size, financialUserListDto);
        if (CollUtil.isNotEmpty(financialUserRecordListDtos)) {
            List<FinancialUserRecordListVo> financialUserRecordListVos = new ArrayList<>(financialUserRecordListDtos.size());
            for (FinancialUserRecordListDto financialUserRecordListDto : financialUserRecordListDtos) {
                financialUserRecordListVos.add(FinancialUserRecordListVo.getFinancialUserRecordListVo(financialUserRecordListDto));
            }
            financialUserListVo.setFinancialUserRecordListVos(financialUserRecordListVos);
        }
        setTotalAmount(financialUserListDto, financialUserListVo);
        return Result.success(result);
    }

    @GetMapping("/financialProductNameList")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result financialProductNameList() {
        List<FinancialProduct> list = financialProductService.list();
        Set<String> names = new HashSet<>();
        if (CollUtil.isNotEmpty(list)) {
            list.forEach(financialProduct -> names.add(financialProduct.getName()));
        }
        return Result.success(names);
    }

    private void setTotalAmount(FinancialUserListDto financialUserListDto, FinancialUserListVo financialUserListVo) {
        FinancialUserTotalDto financialUserTotalDto = financialUserMapper.selectTotalAmount(financialUserListDto);
        BigInteger totalCurrentDeposit = financialUserMapper.selectTotalCurrentAmount(financialUserListDto);
        if (ObjectUtil.isNull(financialUserTotalDto)) {
            financialUserTotalDto = new FinancialUserTotalDto();
        } else {
            financialUserTotalDto.setTotalCurrentDeposit(totalCurrentDeposit);
        }
        financialUserListVo.setTotalCurrentDeposit(CurrencyAdaptType.usdt_omni.money(financialUserTotalDto.getTotalCurrentDeposit()));
        financialUserListVo.setTotalRedemption(CurrencyAdaptType.usdt_omni.money(financialUserTotalDto.getTotalRedemption()));
        financialUserListVo.setTotalHistoricalDeposits(CurrencyAdaptType.usdt_omni.money(financialUserTotalDto.getTotalHistoricalDeposits()));
    }
}
