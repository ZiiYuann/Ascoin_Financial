package com.tianli.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.address.mapper.Address;
import com.tianli.common.PageQuery;
import com.tianli.exception.Result;
import com.tianli.management.vo.MUserHoldRecordSummaryVO;
import com.tianli.management.vo.MWalletUserManagerDataVO;
import com.tianli.management.vo.UserAmountDetailsVO;
import com.tianli.product.entity.ProductHoldRecord;
import com.tianli.product.afinancial.entity.FinancialProduct;
import com.tianli.product.afinancial.query.ProductHoldQuery;
import com.tianli.product.afinancial.service.FinancialService;
import com.tianli.product.afinancial.vo.MUserHoldRecordDetailsVO;
import com.tianli.management.service.ManageUserService;
import com.tianli.management.vo.MUserListVO;
import com.tianli.openapi.query.OpenapiAccountQuery;
import com.tianli.openapi.service.OpenApiService;
import com.tianli.openapi.dto.StatisticsDataDto;
import com.tianli.sso.permission.AdminPrivilege;
import com.tianli.sso.permission.Privilege;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-20
 **/
@RestController
@RequestMapping("/management/financial/user")
public class ManageUserController {

    /**
     * 理财用户管理 列表数据
     */
    @GetMapping("/list")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result<IPage<MUserListVO>> user(PageQuery<Address> page, String uid) {
        return new Result<>(manageUserService.financialUserPage(uid, page.page()));
    }

    /**
     * 理财用户管理-理财用户列表
     */
    @GetMapping("/hold/record/summary")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result<IPage<MUserHoldRecordSummaryVO>> holdRecord(PageQuery<ProductHoldRecord> page
            , ProductHoldQuery query) {
        return new Result<>(manageUserService.summaryHoldRecordPage(query, page.page()));
    }

    /**
     * 理财用户管理-持仓中
     */
    @GetMapping("/hold/record/details")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result<IPage<MUserHoldRecordDetailsVO>> userHold(PageQuery<FinancialProduct> pageQuery
            , ProductHoldQuery query) {
        return new Result<>(financialService.detailsHoldProductPage(pageQuery.page(), query));
    }

    /**
     * 理财用户管理-持仓用户
     */
    @GetMapping("/hold/record/data")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result<MUserHoldRecordSummaryVO> holdRecordData(ProductHoldQuery query) {
        return new Result<>(manageUserService.userHoldRecordData(query));
    }

    /**
     * 理财用户管理 上方累计数据
     */
    @GetMapping("/data")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result<MWalletUserManagerDataVO> data(String uid) {
        return new Result<>(financialService.mWalletUserManagerData(uid));
    }

    /**
     * 用户资金数据
     */
    @GetMapping("/details/{uid}")
    @AdminPrivilege(and = Privilege.理财管理, api = "/management/financial/user/details/uid")
    public Result<UserAmountDetailsVO> details(@PathVariable Long uid) {
        return Result.success(financialService.userAmountDetailsVO(uid));
    }

    /**
     * 账户信息
     */
    @GetMapping("/account")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result statisticsData(OpenapiAccountQuery query) {
        return Result.success(openApiService.accountData(query));
    }

    /**
     * 账户信息
     */
    @GetMapping("/account/sub")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result statisticsData(Long chatId, Long uid, PageQuery<StatisticsDataDto> pageQuery) {
        return Result.success(openApiService.accountSubData(chatId, uid, pageQuery));
    }

    @Resource
    private FinancialService financialService;
    @Resource
    private OpenApiService openApiService;
    @Resource
    private ManageUserService manageUserService;
}
