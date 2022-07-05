package com.tianli.loan.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.common.init.RequestInitService;
import com.tianli.common.lock.RedisLock;
import com.tianli.currency_token.transfer.service.TokenContractService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.loan.dto.ApplyLoanDTO;
import com.tianli.loan.service.ILoanAddressService;
import com.tianli.loan.service.ILoanCycleService;
import com.tianli.loan.service.ILoanService;
import com.tianli.loan.vo.LoanListVo;
import com.tianli.loan.vo.LoanQueryVo;
import com.tianli.loan.vo.RepaymentDetailsVo;
import com.tianli.loan.vo.RepaymentRecordVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author lzy
 * @since 2022-05-26
 */
@RestController
@RequestMapping("/loan")
public class LoanController {

    @Resource
    ILoanService loanService;

    @Resource
    ILoanCycleService loanCycleService;

    @Resource
    RedisLock redisLock;

    @Resource
    RequestInitService requestInitService;

    @Resource
    ILoanAddressService loanAddressService;

    @Resource
    TokenContractService tokenContractService;

    public static final String LOAN_APPLY_KEY = "loan_apply_key_uid:{}";

    public static final String LOAN_REPAYMENT_KEY = "loan_repayment_key_uid:{}";

    @PostMapping("/apply")
    public Result apply(@RequestBody @Validated ApplyLoanDTO applyLoanDTO) {
        String key = StrUtil.format(LOAN_APPLY_KEY, requestInitService.uid());
        if (!redisLock._lock(key, 10L, TimeUnit.SECONDS)) {
            return Result.success();
        }
        try {
            loanService.apply(applyLoanDTO);
        } finally {
            redisLock.unlock();
        }
        return Result.success();
    }

    @GetMapping("/details")
    public Result details(Long id) {
        LoanQueryVo loan = loanService.details(id);
        return Result.success(loan);
    }

    @GetMapping("/repaymentDetails/")
    public Result repaymentDetails() {
        RepaymentDetailsVo repaymentDetailsVo = loanService.repaymentDetails();
        return Result.success(repaymentDetailsVo);
    }

    @GetMapping("/cycle")
    public Result cycle() {
        return Result.success(loanCycleService.list());
    }

    @GetMapping("/list")
    public Result list(String status, @RequestParam Integer page, @RequestParam Integer size) {
        IPage<LoanListVo> result = loanService.queryList(status, page, size);
        return Result.success(result);
    }

    @GetMapping("/repaymentRecord")
    public Result repaymentRecord(@RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "10") Integer size) {
        IPage<RepaymentRecordVo> result = loanService.repaymentRecord(page, size);
        return Result.success(result);
    }

    @PostMapping("/repayment")
    public Result repayment() {
        throw ErrorCodeEnum.SERVICE_NOT_AVAILABLE.generalException();
        /*String key = StrUtil.format(LOAN_REPAYMENT_KEY, requestInitService.uid());
        if (!redisLock._lock(key, 10L, TimeUnit.SECONDS)) {
            return Result.success();
        }
        try {
            loanService.repayment();
        } finally {
            redisLock.unlock();
        }
        return Result.success();*/
    }


}

