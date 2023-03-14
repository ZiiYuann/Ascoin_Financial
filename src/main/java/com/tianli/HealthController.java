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
}
