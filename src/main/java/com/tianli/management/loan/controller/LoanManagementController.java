package com.tianli.management.loan.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.common.CommonFunction;
import com.tianli.exception.Result;
import com.tianli.loan.entity.LoanCycle;
import com.tianli.loan.entity.LoanRepaymentRecord;
import com.tianli.loan.service.ILoanCycleService;
import com.tianli.loan.service.ILoanRepaymentRecordService;
import com.tianli.management.loan.dto.LoanAuditDTO;
import com.tianli.management.loan.service.LoanManagementService;
import com.tianli.management.loan.vo.LoanAuditDetailsVo;
import com.tianli.management.loan.vo.LoanListVo;
import com.tianli.role.annotation.AdminPrivilege;
import com.tianli.role.annotation.Privilege;
import com.tianli.tool.MapTool;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author lzy
 * @date 2022/5/26 15:05
 */
@RestController
@RequestMapping("/management/loan")
public class LoanManagementController {

    @Resource
    LoanManagementService loanManagementService;

    @Resource
    ILoanCycleService loanCycleService;

    @Resource
    ILoanRepaymentRecordService loanRepaymentRecordService;


    @GetMapping("/list")
    @AdminPrivilege(and = Privilege.借款记录)
    public Result list(String username, String status, String reviewer, String startTime, String endTime,
                       @RequestParam Integer page, @RequestParam Integer size) {
        IPage<LoanListVo> result = loanManagementService.queryList(username, status, reviewer, startTime, endTime, page, size);
        return Result.success(result);
    }


    @PostMapping("/audit")
    @AdminPrivilege(and = Privilege.借款记录)
    public Result audit(@RequestBody @Validated LoanAuditDTO loanAuditDTO) {
        loanManagementService.audit(loanAuditDTO);
        return Result.success();
    }

    @RequestMapping("/auditDetails/{id}")
    @AdminPrivilege(and = Privilege.借款记录)
    public Result auditDetails(@PathVariable Long id) {
        LoanAuditDetailsVo loanAuditDetailsVo = loanManagementService.auditDetails(id);
        return Result.success(loanAuditDetailsVo);
    }

    @GetMapping("/total")
    @AdminPrivilege(and = Privilege.借款记录)
    public Result total(String username, String status, String reviewer, String startTime, String endTime) {
        BigDecimal totalAmount = loanManagementService.total(username, status, reviewer, startTime, endTime);
        return Result.success(MapTool.Map().put("totalAmount", totalAmount));
    }

    @GetMapping("/cycleList")
    @AdminPrivilege(and = Privilege.借款记录)
    public Result cycleList(Integer page, Integer size) {
        Page<LoanCycle> result = loanCycleService.page(new Page<>(page, size), Wrappers.lambdaQuery(LoanCycle.class).orderByDesc(LoanCycle::getId));
        return Result.success(result);
    }

    @PostMapping("/auditCycle")
    @AdminPrivilege(and = Privilege.借款记录)
    public Result auditCycle(@RequestBody @Validated LoanCycle loanCycle) {
        if (ObjectUtil.isNull(loanCycle.getId())) {
            loanCycle.setId(CommonFunction.generalId());
            loanCycle.setCreate_time(LocalDateTime.now());
        } else {
            loanCycle.setUpdate_time(LocalDateTime.now());
        }
        loanCycleService.saveOrUpdate(loanCycle);
        return Result.success();
    }

    @GetMapping("/repaymentRecord")
    @AdminPrivilege(and = Privilege.借款记录)
    public Result repaymentRecord(@RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "10") Integer size,
                                  @RequestParam Long id) {
        Page<LoanRepaymentRecord> recordPage = loanRepaymentRecordService.page(new Page<>(page, size), Wrappers.lambdaQuery(LoanRepaymentRecord.class)
                .eq(LoanRepaymentRecord::getLoan_id, id)
                .orderByDesc(LoanRepaymentRecord::getCreate_time));
        return Result.success(recordPage);
    }

}
