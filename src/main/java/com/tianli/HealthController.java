package com.tianli;

import com.tianli.exception.Result;
import com.tianli.tool.MapTool;
import com.tianli.tool.time.TimeTool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
