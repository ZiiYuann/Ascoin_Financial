package com.tianli.management.salesman.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.exception.Result;
import com.tianli.management.salesman.dto.CustomerAssignmentDto;
import com.tianli.management.salesman.dto.SalesmanEditDto;
import com.tianli.management.salesman.service.SalesmanService;
import com.tianli.management.salesman.vo.SalesmanInfoListVo;
import com.tianli.management.salesman.vo.SalesmanLeaderListVo;
import com.tianli.management.salesman.vo.SalesmanListVo;
import com.tianli.role.annotation.AdminPrivilege;
import com.tianli.role.annotation.Privilege;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author lzy
 * @date 2022/4/6 4:17 下午
 */
@RestController
@RequestMapping("/management/salesman")
public class SalesmanController {

    @Resource
    SalesmanService salesmanService;


    @PostMapping("/edit")
    @AdminPrivilege(and = Privilege.小组管理)
    public Result edit(@RequestBody @Validated SalesmanEditDto salesmanEditDto) {
        salesmanService.edit(salesmanEditDto);
        return Result.success();
    }

    @PostMapping("/deleteSalesman/{id}")
    @AdminPrivilege(and = Privilege.小组管理)
    public Result deleteSalesman(@PathVariable Long id) {
        salesmanService.deleteSalesman(id);
        return Result.success();
    }

    @GetMapping("/leaderList")
    @AdminPrivilege(and = Privilege.小组管理)
    public Result leaderList() {
        List<SalesmanLeaderListVo> result = salesmanService.leaderList();
        return Result.success(result);
    }

    @GetMapping("/salesmanList")
    @AdminPrivilege(or = {Privilege.小组管理, Privilege.客户管理, Privilege.现货充值记录})
    public Result salesmanList(@RequestParam(value = "page", defaultValue = "1") Integer page,
                               @RequestParam(value = "size", defaultValue = "10") Integer size,
                               String username) {
        IPage<SalesmanListVo> result = salesmanService.salesmanList(page, size, username);
        return Result.success(result);
    }

    @PostMapping("/customerAssignment")
    @AdminPrivilege(and = Privilege.小组管理)
    public Result customerAssignment(@RequestBody @Validated CustomerAssignmentDto customerAssignmentDto) {
        salesmanService.customerAssignment(customerAssignmentDto);
        return Result.success();
    }

    @GetMapping("/infoList")
    @AdminPrivilege(and = Privilege.小组管理)
    public Result infoList(@RequestParam(value = "page", defaultValue = "1") Integer page,
                           @RequestParam(value = "size", defaultValue = "10") Integer size,
                           @RequestParam(value = "id", required = false) Long id) {
        IPage<SalesmanInfoListVo> result = salesmanService.infoList(page, size, id);
        return Result.success(result);
    }
}
