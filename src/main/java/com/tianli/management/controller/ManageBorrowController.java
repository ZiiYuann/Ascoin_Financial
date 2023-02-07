package com.tianli.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.common.PageQuery;
import com.tianli.exception.Result;
import com.tianli.product.aborrow.entity.BorrowConfigCoin;
import com.tianli.product.aborrow.entity.BorrowConfigPledge;
import com.tianli.product.aborrow.query.BorrowConfigCoinIoUQuery;
import com.tianli.product.aborrow.query.BorrowConfigPledgeIoUQuery;
import com.tianli.product.aborrow.query.BorrowQuery;
import com.tianli.product.aborrow.service.BorrowConfigCoinService;
import com.tianli.product.aborrow.service.BorrowConfigPledgeService;
import com.tianli.product.aborrow.vo.MBorrowConfigCoinVO;
import com.tianli.product.aborrow.vo.MBorrowConfigPledgeVO;
import com.tianli.sso.permission.AdminPrivilege;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-07
 **/
@RestController
@RequestMapping("/management/borrow/")
public class ManageBorrowController {

    @Resource
    private BorrowConfigCoinService borrowConfigCoinService;
    @Resource
    private BorrowConfigPledgeService borrowConfigPledgeService;

    @AdminPrivilege
    @PostMapping("/config/coin")
    public Result<Void> configCoin(@RequestBody @Valid BorrowConfigCoinIoUQuery query) {
        borrowConfigCoinService.insertOrUpdate(query);
        return new Result<>();
    }

    @AdminPrivilege
    @GetMapping("/config/coins")
    public Result<IPage<MBorrowConfigCoinVO>> configCoins(PageQuery<BorrowConfigCoin> page, BorrowQuery query) {
        return new Result<>(borrowConfigCoinService.MBorrowConfigCoinVOPage(page.page(), query));
    }

    @AdminPrivilege
    @PostMapping("/config/coin/status")
    public Result<Void> configCoinStatus(@RequestBody BorrowQuery query) {
        borrowConfigCoinService.modifyStatus(query.getCoin(), query.getBorrowStatus());
        return new Result<>();
    }

    @AdminPrivilege
    @PostMapping("/config/pledge")
    public Result<Void> configCoin(@RequestBody @Valid BorrowConfigPledgeIoUQuery query) {
        borrowConfigPledgeService.insertOrUpdate(query);
        return new Result<>();
    }

    @AdminPrivilege
    @GetMapping("/config/pledges")
    public Result<IPage<MBorrowConfigPledgeVO>> configPledge(PageQuery<BorrowConfigPledge> page, BorrowQuery query) {
        return new Result<>(borrowConfigPledgeService.MBorrowConfigCoinVOPage(page.page(), query));
    }

    @AdminPrivilege
    @PostMapping("/config/pledge/status")
    public Result<Void> configCoin(@RequestBody BorrowQuery query) {
        borrowConfigPledgeService.modifyStatus(query.getCoin(), query.getBorrowStatus());
        return new Result<>();
    }


}
