package com.tianli.management.chainfinance;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.currency_token.mapper.ChainType;
import com.tianli.currency_token.transfer.mapper.TokenContract;
import com.tianli.currency_token.transfer.service.TokenContractService;
import com.tianli.exception.Result;
import com.tianli.management.chainfinance.dto.MainWalletLogDTO;
import com.tianli.role.annotation.AdminPrivilege;
import com.tianli.role.annotation.Privilege;
import com.tianli.wallet.MainWalletLogService;
import com.tianli.wallet.mapper.MainWalletLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("chain")
public class ChainFinanceController {

    @Resource
    TokenContractService tokenContractService;


    @GetMapping("/wallet/log/list")
    @AdminPrivilege(and = Privilege.主钱包交易明细)
    public Result mainWalletLog(String address, String txid, ChainType chain_type, String start, String end,
                                @RequestParam(value = "page", defaultValue = "1") Integer page,
                                @RequestParam(value = "size", defaultValue = "10") Integer size) {
        LambdaQueryWrapper<MainWalletLog> wrapper = new LambdaQueryWrapper<MainWalletLog>()
                .eq(Objects.nonNull(chain_type), MainWalletLog::getChain_type, chain_type)
                .eq(StringUtils.isNotBlank(txid), MainWalletLog::getTxid, txid)
                .gt(StringUtils.isNotBlank(start), MainWalletLog::getCreate_time, start)
                .lt(StringUtils.isNotBlank(end), MainWalletLog::getCreate_time, end)
                .or().eq(StringUtils.isNotBlank(address), MainWalletLog::getTo_address, address)
                .or().eq(StringUtils.isNotBlank(address), MainWalletLog::getFrom_address, address)
                .orderByDesc(MainWalletLog::getCreate_time);
        List<MainWalletLog> list = mainWalletLogService.page(new Page<>(page, size), wrapper).getRecords();
        long count = mainWalletLogService.count(wrapper);
        List<TokenContract> tokenContractList = tokenContractService.list();
        return Result.instance().setList(list.stream().map(mainWalletLog -> MainWalletLogDTO.trans(mainWalletLog, tokenContractList)).collect(Collectors.toList()), count);
    }

    @GetMapping("/wallet/log/stat")
    @AdminPrivilege(and = Privilege.主钱包交易明细)
    public Result stat(String address, String txid, ChainType chain_type, String start, String end) {
        Map<String, Object> result = mainWalletLogService.sumAmount(address, txid, chain_type, start, end);
        return Result.success(result);
    }

    @Resource
    private MainWalletLogService mainWalletLogService;
}
