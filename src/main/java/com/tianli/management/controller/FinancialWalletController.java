package com.tianli.management.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.tianli.common.PageQuery;
import com.tianli.currency.enums.CurrencyAdaptType;
import com.tianli.exception.Result;
import com.tianli.financial.service.FinancialProductService;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.management.dao.FinancialUserMapper;
import com.tianli.management.dto.FinancialUserRecordListDto;
import com.tianli.management.dto.FinancialUserListDto;
import com.tianli.management.dto.FinancialUserTotalDto;
import com.tianli.management.query.FinancialBoardQuery;
import com.tianli.management.vo.FinancialUserListVO;
import com.tianli.management.vo.FinancialUserRecordListVO;
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
 * @since 2022/4/1 6:20 下午
 */
@RestController
@RequestMapping("/management/financial/wallet/")
public class FinancialWalletController {

    @Resource
    FinancialProductService financialProductService;

    @Resource
    FinancialUserMapper financialUserMapper;


    @GetMapping("/board")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result board(FinancialBoardQuery query) {
        query.calTime();
        return  null;
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

}
