package com.tianli.management.recycle;

import com.tianli.currency_token.transfer.mapper.TokenContract;
import com.tianli.currency_token.transfer.service.TokenContractService;
import com.tianli.exception.Result;
import com.tianli.role.annotation.AdminPrivilege;
import com.tianli.role.annotation.FundsPasswordPrivilege;
import com.tianli.role.annotation.Privilege;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author cs
 * @Date 2022-03-28 10:12 上午
 */
@RestController
@RequestMapping("recycle")
public class RecycleController {
    @Resource
    private RecycleService recycleService;

    @Resource
    TokenContractService tokenContractService;

    @PostMapping("/execute")
    @FundsPasswordPrivilege
    @AdminPrivilege(and = Privilege.待归集管理)
    public Result execute(@RequestBody RecycleDTO dto) {
        return Result.success(recycleService.execute(dto));
    }

    @GetMapping("/tokenList")
    @AdminPrivilege(or = {Privilege.待归集管理, Privilege.提现手续费})
    public Result tokenList() {
        List<TokenContract> list = tokenContractService.list();
        list.forEach(tokenContract -> tokenContract.setContract_address(null));
        return Result.success(list);
    }

    @GetMapping("/list")
    @AdminPrivilege(and = Privilege.待归集管理)
    public Result balance(String address,
                          @RequestParam(value = "tokenId") Integer tokenId,
                          @RequestParam(value = "page", defaultValue = "1") Integer page,
                          @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return Result.success(recycleService.balance(address, tokenId, page, size));
    }
}
