package com.tianli.management.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.common.PageQuery;
import com.tianli.exception.Result;
import com.tianli.management.bo.WalletAgentBO;
import com.tianli.management.entity.WalletAgent;
import com.tianli.management.query.WalletAgentQuery;
import com.tianli.management.service.IWalletAgentService;
import com.tianli.management.vo.WalletAgentVO;
import com.tianli.sso.permission.AdminPrivilege;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * <p>
 * 云钱包代理人 前端控制器
 * </p>
 *
 * @author xianeng
 * @since 2022-08-30
 */
@RestController
@RequestMapping("/management/agent")
public class ManageAgentController {

    @Resource
    private IWalletAgentService walletAgentService;

    /**
     * 添加代理人
     */
    @PostMapping
    @AdminPrivilege
    public Result addAgent(@RequestBody @Valid WalletAgentBO bo) {
        walletAgentService.saveAgent(bo);
        return Result.success();
    }

    /**
     * 更新代理人
     */
    @PutMapping
    @AdminPrivilege
    public Result updateAgent(@RequestBody @Valid WalletAgentBO bo) {
        walletAgentService.updateAgent(bo);
        return Result.success();
    }

    /**
     * 删除代理人
     */
    @DeleteMapping("/{id}")
    @AdminPrivilege(api = "/management/agent/id")
    public Result delAgent(@PathVariable Long id) {
        walletAgentService.delAgent(id);
        return Result.success();
    }

    /**
     * 查询代理人详情
     */
    @GetMapping("/{id}")
    @AdminPrivilege(api = "/management/agent/id")
    public Result info(@PathVariable Long id) {
        WalletAgentVO agentVO = walletAgentService.getById(id);
        return Result.success(agentVO);
    }

    /**
     * 查询代理人列表
     */
    @GetMapping("/list")
    @AdminPrivilege
    public Result list(PageQuery<WalletAgent> pageQuery, WalletAgentQuery query) {
        IPage<WalletAgentVO> page = walletAgentService.getPage(pageQuery, query);
        return Result.success(page);
    }

}

