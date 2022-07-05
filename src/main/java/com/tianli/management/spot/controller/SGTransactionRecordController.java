package com.tianli.management.spot.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.exception.Result;
import com.tianli.management.spot.service.SGTransactionRecordService;
import com.tianli.management.spot.vo.SGTransactionRecordListVo;
import com.tianli.role.annotation.AdminPrivilege;
import com.tianli.role.annotation.Privilege;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author lzy
 * @date 2022/4/16 11:04 上午
 */
@RestController
@RequestMapping("/management/spot/transactionRecord")
public class SGTransactionRecordController {

    @Resource
    SGTransactionRecordService sgTransactionRecordService;

    @GetMapping("/page")
    @AdminPrivilege(and = Privilege.现货交易记录)
    public Result page(String username, String token, String startTime, String endTime,
                       @RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "size", defaultValue = "10") Integer size) {
        IPage<SGTransactionRecordListVo> result = sgTransactionRecordService.selectPage(username, token, startTime, endTime, page, size);
        return Result.success(result);
    }
}
