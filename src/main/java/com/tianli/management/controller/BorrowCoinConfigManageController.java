package com.tianli.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.borrow.bo.BorrowOrderConfigBO;
import com.tianli.borrow.entity.BorrowCoinConfig;
import com.tianli.borrow.service.IBorrowCoinConfigService;
import com.tianli.borrow.vo.BorrowCoinConfigVO;
import com.tianli.common.PageQuery;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.exception.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/management/borrow/coin/config")
public class BorrowCoinConfigManageController {
    @Autowired
    private IBorrowCoinConfigService borrowCoinConfigService;

    /**
     * 新增配置
     * @param bo
     * @return
     */
    @PostMapping
    private Result save(@RequestBody BorrowOrderConfigBO bo){
        borrowCoinConfigService.saveConfig(bo);
        return Result.success();
    }

    /**
     * 修改配置
     * @param bo
     * @return
     */
    @PutMapping
    private Result update(@RequestBody BorrowOrderConfigBO bo){
        borrowCoinConfigService.updateConfig(bo);
        return Result.success();
    }

    /**
     * 删除配置
     * @param ids
     * @return
     */
    @DeleteMapping("/{ids}")
    private Result delete(@PathVariable Long[] ids){
        borrowCoinConfigService.delConfig(ids);
        return Result.success();
    }

    /**
     * 查询配置
     * @param pageQuery
     * @param coin
     * @return
     */
    @GetMapping
    private Result pageList(PageQuery<BorrowCoinConfig> pageQuery, CurrencyCoin coin){
        IPage<BorrowCoinConfigVO> pageList = borrowCoinConfigService.pageList(pageQuery, coin);
        return Result.success(pageList);
    }
}
