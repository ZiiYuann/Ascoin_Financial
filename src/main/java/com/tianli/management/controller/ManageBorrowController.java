package com.tianli.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.account.query.IdsQuery;
import com.tianli.common.PageQuery;
import com.tianli.common.QueryWrapperUtils;
import com.tianli.common.RedisLockConstants;
import com.tianli.currency.service.CurrencyService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.management.query.BorrowHedgeEntrustIoUQuery;
import com.tianli.product.aborrow.convert.BorrowConvert;
import com.tianli.product.aborrow.entity.*;
import com.tianli.product.aborrow.enums.HedgeStatus;
import com.tianli.product.aborrow.query.*;
import com.tianli.product.aborrow.service.*;
import com.tianli.product.aborrow.vo.*;
import com.tianli.sso.permission.AdminPrivilege;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-07
 **/
@RestController
@RequestMapping("/management/borrow")
public class ManageBorrowController {

    @Resource
    private BorrowConfigCoinService borrowConfigCoinService;
    @Resource
    private BorrowConfigPledgeService borrowConfigPledgeService;
    @Resource
    private BorrowRecordService borrowRecordService;
    @Resource
    private BorrowRecordCoinService borrowRecordCoinService;
    @Resource
    private BorrowRecordPledgeService borrowRecordPledgeService;
    @Resource
    private BorrowOperationLogService borrowOperationLogService;
    @Resource
    private BorrowConvert borrowConvert;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private BorrowHedgeEntrustService borrowHedgeEntrustService;
    @Resource
    private CurrencyService currencyService;

    // 新增或者修改借币配置
    @AdminPrivilege
    @PostMapping("/config/coin")
    public Result<Void> configCoin(@RequestBody @Valid BorrowConfigCoinIoUQuery query) {
        borrowConfigCoinService.insertOrUpdate(query);
        return new Result<>();
    }

    // 借币配置列表
    @AdminPrivilege
    @GetMapping("/config/coins")
    public Result<IPage<MBorrowConfigCoinVO>> configCoins(PageQuery<BorrowConfigCoin> page, BorrowQuery query) {
        return new Result<>(borrowConfigCoinService.MBorrowConfigCoinVOPage(page.page(), query));
    }

    // 修改借币配置状态
    @AdminPrivilege
    @PostMapping("/config/coin/status")
    public Result<Void> configCoinStatus(@RequestBody BorrowQuery query) {
        borrowConfigCoinService.modifyStatus(query.getCoin(), query.getBorrowStatus());
        return new Result<>();
    }

    // 新增或者修改质押币配置
    @AdminPrivilege
    @PostMapping("/config/pledge")
    public Result<Void> configCoin(@RequestHeader(value = "force", required = false) boolean force,
                                   @RequestBody @Valid BorrowConfigPledgeIoUQuery query) {
        Set<String> keys = stringRedisTemplate.keys(RedisLockConstants.LOCK_BORROW + "*");
        if (!force && CollectionUtils.isNotEmpty(keys)) {
            ErrorCodeEnum.BORROW_M_LOCK_ERROR.throwException();
        }
        borrowConfigPledgeService.insertOrUpdate(query);
        return new Result<>();
    }

    // 质押币配置列表
    @AdminPrivilege
    @GetMapping("/config/pledges")
    public Result<IPage<MBorrowConfigPledgeVO>> configPledge(PageQuery<BorrowConfigPledge> page, BorrowQuery query) {
        return new Result<>(borrowConfigPledgeService.MBorrowConfigCoinVOPage(page.page(), query));
    }

    // 修改质押币配置状态
    @AdminPrivilege
    @PostMapping("/config/pledge/status")
    public Result<Void> configCoin(@RequestBody BorrowQuery query) {
        borrowConfigPledgeService.modifyStatus(query.getCoin(), query.getBorrowStatus());
        return new Result<>();
    }

    @AdminPrivilege
    @GetMapping("/orders")
    public Result<IPage<MBorrowUserVO>> order(PageQuery<BorrowRecord> page, BorrowUserQuery query) {
        IPage<MBorrowUserVO> result = borrowRecordService.pledgeUsers(page.page(), query);
        return new Result<>(result);
    }

    // 借币
    @AdminPrivilege
    @GetMapping("/user/coin")
    public Result<List<MBorrowRecordVO>> userBorrow(@RequestParam("uid") Long uid,
                                                    @RequestParam("bid") Long bid) {
        var result = borrowRecordCoinService.listByUid(uid, bid)
                .stream().map(coin -> MBorrowRecordVO.builder()
                        .brId(coin.getId())
                        .amount(coin.getAmount())
                        .coin(coin.getCoin()).build()).collect(Collectors.toList());
        return new Result<>(result);
    }

    // 质押币
    @AdminPrivilege
    @GetMapping("/user/pledge")
    public Result<List<MBorrowRecordVO>> userPledge(@RequestParam("uid") Long uid,
                                                    @RequestParam("bid") Long bid) {
        var result = borrowRecordPledgeService.listByUid(uid, bid)
                .stream().map(coin -> MBorrowRecordVO.builder()
                        .amount(coin.getAmount())
                        .pledgeType(coin.getPledgeType())
                        .rate(currencyService.getDollarRate(coin.getCoin()))
                        .coin(coin.getCoin()).build()).collect(Collectors.toList());
        return new Result<>(result);
    }

    // 操作
    @AdminPrivilege
    @GetMapping("/user/operation")
    public Result<IPage<MBorrowOperationLogVO>> userOperation(PageQuery<BorrowOperationLog> page
            , MBorrowOperationLogQuery query) {
        IPage<MBorrowOperationLogVO> result = borrowOperationLogService.page(page.page()
                        , QueryWrapperUtils.generate(BorrowOperationLog.class, query))
                .convert(borrowConvert::toMBorrowOperationLogVO);
        return new Result<>(result);
    }


    @AdminPrivilege
    @PostMapping("/hedge")
    public Result<Void> hedge(@RequestBody BorrowHedgeEntrustIoUQuery query) {
        borrowHedgeEntrustService.manual(query);
        return new Result<>();
    }

    @AdminPrivilege
    @GetMapping("/hedges")
    public Result<IPage<MBorrowHedgeEntrustVO>> hedges(PageQuery<BorrowHedgeEntrust> page, MBorrowHedgeQuery query) {
        IPage<MBorrowHedgeEntrustVO> result = borrowHedgeEntrustService.vos(page.page(), query);
        return new Result<>(result);
    }

    @AdminPrivilege
    @PostMapping("/hedge/cancel")
    public Result<Void> hedgeCancel(@RequestBody IdsQuery query) {
        borrowHedgeEntrustService.cancel(query);
        return new Result<>();
    }

}
