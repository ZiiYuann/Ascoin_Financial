package com.tianli.management.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.borrow.bo.BorrowPledgeCoinConfigBO;
import com.tianli.borrow.entity.BorrowPledgeCoinConfig;
import com.tianli.borrow.service.IBorrowPledgeCoinConfigService;
import com.tianli.borrow.vo.BorrowPledgeCoinConfigVO;
import com.tianli.common.PageQuery;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.exception.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 质押币种配置 前端控制器
 * </p>
 *
 * @author xianeng
 * @since 2022-07-28
 */
@RestController
@RequestMapping("/management/borrow/coin/pledge/config")
public class BorrowPledgeCoinConfigController {

    @Autowired
    private IBorrowPledgeCoinConfigService borrowPledgeCoinConfigService;

    @PostMapping
    private Result save(@RequestBody BorrowPledgeCoinConfigBO bo){
        borrowPledgeCoinConfigService.saveConfig(bo);
        return Result.success();
    }

    @PutMapping
    private Result update(@RequestBody BorrowPledgeCoinConfigBO bo){
        borrowPledgeCoinConfigService.updateConfig(bo);
        return Result.success();
    }

    @DeleteMapping("/{ids}")
    private Result delete(@PathVariable Long[] ids){
        borrowPledgeCoinConfigService.delConfig(ids);
        return Result.success();
    }

    @GetMapping
    private Result pageList(PageQuery<BorrowPledgeCoinConfig> pageQuery, CurrencyCoin coin){
        IPage<BorrowPledgeCoinConfigVO> pageList = borrowPledgeCoinConfigService.pageList(pageQuery, coin);
        return Result.success(pageList);
    }

}

