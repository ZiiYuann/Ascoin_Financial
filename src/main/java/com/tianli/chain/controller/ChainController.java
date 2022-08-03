package com.tianli.chain.controller;

import com.tianli.chain.service.contract.ContractAdapter;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.exception.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-03
 **/
@RestController
@RequestMapping("/chain")
public class ChainController {

    @Resource
    private ContractAdapter contractAdapter;

    /**
     * 校验地址有效
     */
    @GetMapping("/valid/address")
    public Result activateWallet(NetworkType networkType, String address) {
        boolean validAddress = contractAdapter.getOne(networkType).isValidAddress(address);
        Map<String, Boolean> result = new HashMap<>();
        result.put("valid", validAddress);
        return Result.success().setData(result);
    }

}
