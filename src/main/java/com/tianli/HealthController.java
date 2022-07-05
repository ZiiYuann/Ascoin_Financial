package com.tianli;

import com.tianli.exception.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @RequestMapping("/ping")
    public Result ping() {
        return Result.instance();
    }

}
