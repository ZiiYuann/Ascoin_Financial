package com.tianli;

import com.tianli.chain.entity.Coin;
import com.tianli.chain.service.contract.PolygonTriggerContract;
import com.tianli.common.ConfigConstants;
import com.tianli.exception.Result;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.MapTool;
import com.tianli.tool.time.TimeTool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Uint;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@RestController
public class HealthController {
    @RequestMapping("/ping")
    public Result ping() {
        return Result.instance();
    }

    private final static String time = TimeTool.getNowDateTimeDisplayString();

    @Value("${release.version:上线版本说明}")
    private String version;

    @RequestMapping("/version")
    public Result version() {
        return Result.success(MapTool.Map()
                .put("version", version)
                .put("time", time)
        );
    }

    @Resource
    PolygonTriggerContract polygonTriggerContract;


    @Resource
    ConfigService configService;

    @RequestMapping("/tokenTransfer")
    public Result tokenTransfer(@RequestParam(defaultValue = "14") Long nonce) {
        Coin coin = new Coin();
        coin.setContract("0xc2132D05D31c914a87C6611C10748AEb04B58e8F");
        polygonTriggerContract.sendRawTransaction(BigInteger.valueOf(nonce), 137L, "0xc2132D05D31c914a87C6611C10748AEb04B58e8F", FunctionEncoder.encode(
                new Function("transfer", List.of(new Address("0x28cD15D59F4267cA7599B7B89bd6edDCB9c78255"), new Uint(BigInteger.ZERO)), new ArrayList<>())
        ), BigInteger.ZERO, "120", BigInteger.valueOf(800000), configService.get(ConfigConstants.MAIN_WALLET_PASSWORD), "operation");
        return Result.success(MapTool.Map()
                .put("version", version)
                .put("time", time)
        );
    }
}
