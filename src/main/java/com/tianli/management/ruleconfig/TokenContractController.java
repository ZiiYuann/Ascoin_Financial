package com.tianli.management.ruleconfig;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.currency_token.mapper.ChainType;
import com.tianli.currency_token.transfer.mapper.TokenContract;
import com.tianli.currency_token.transfer.service.TokenContractService;
import com.tianli.exception.Result;
import com.tianli.role.annotation.AdminPrivilege;
import com.tianli.role.annotation.Privilege;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/config/token")
public class TokenContractController {

    @GetMapping("/list")
    @AdminPrivilege(and = Privilege.提现手续费)
    public Result list(@RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "size", defaultValue = "10") Integer size,
                       Long tokenId, ChainType chainType) {
        List<TokenContract> list = tokenContractService.page(new Page<>(page, size), new LambdaQueryWrapper<TokenContract>()
                .eq(Objects.nonNull(tokenId), TokenContract::getId, tokenId)
                .eq(Objects.nonNull(chainType), TokenContract::getChain, chainType)
        ).getRecords();
        long total = tokenContractService.count(new LambdaQueryWrapper<TokenContract>()
                .eq(Objects.nonNull(tokenId), TokenContract::getId, tokenId)
                .eq(Objects.nonNull(chainType), TokenContract::getChain, chainType));

        return Result.instance().setList(list, total);
    }


    @PostMapping("/save")
    @AdminPrivilege(and = Privilege.提现手续费)
    public Result save(@RequestBody TokenContract saveBody) {
        TokenContract tokenContract = tokenContractService.getById(saveBody.getId());
        tokenContract.setWithdraw_fixed_amount(saveBody.getWithdraw_fixed_amount());
        tokenContract.setWithdraw_min_amount(saveBody.getWithdraw_min_amount());
        tokenContract.setWithdraw_rate(saveBody.getWithdraw_rate());
        tokenContractService.updateById(tokenContract);
        return Result.instance();
    }


    @Resource
    private TokenContractService tokenContractService;
}
