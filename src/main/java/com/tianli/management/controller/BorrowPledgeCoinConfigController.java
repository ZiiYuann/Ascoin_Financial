package com.tianli.management.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.borrow.bo.BorrowPledgeCoinConfigBO;
import com.tianli.borrow.entity.BorrowPledgeCoinConfig;
import com.tianli.borrow.service.IBorrowPledgeCoinConfigService;
import com.tianli.borrow.vo.BorrowPledgeCoinConfigVO;
import com.tianli.common.PageQuery;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.exception.Result;
import com.tianli.sso.permission.AdminPrivilege;
import com.tianli.sso.permission.Privilege;
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

    /**
     * 新增配置
     * @param bo
     * @return
     */
    @PostMapping
    @AdminPrivilege(and = Privilege.质押币配置)
    private Result save(@RequestBody BorrowPledgeCoinConfigBO bo){
        borrowPledgeCoinConfigService.saveConfig(bo);
        return Result.success();
    }

    /**
     * 修改配置
     * @param bo
     * @return
     */
    @PutMapping
    @AdminPrivilege(and = Privilege.质押币配置)
    private Result update(@RequestBody BorrowPledgeCoinConfigBO bo){
        borrowPledgeCoinConfigService.updateConfig(bo);
        return Result.success();
    }

    /**
     * 删除配置
     * @param ids
     * @return
     */
    @DeleteMapping("/{ids}")
    @AdminPrivilege(and = Privilege.质押币配置)
    private Result delete(@PathVariable Long[] ids){
        borrowPledgeCoinConfigService.delConfig(ids);
        return Result.success();
    }

    /**
     * 查询配置
     * @param pageQuery
     * @param coin
     * @return
     */
    @GetMapping
    @AdminPrivilege(and = Privilege.质押币配置)
    private Result pageList(PageQuery<BorrowPledgeCoinConfig> pageQuery, CurrencyCoin coin){
        IPage<BorrowPledgeCoinConfigVO> pageList = borrowPledgeCoinConfigService.pageList(pageQuery, coin);
        return Result.success(pageList);
    }

}

