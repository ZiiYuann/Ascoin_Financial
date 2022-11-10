package com.tianli.management.dto;

import com.tianli.common.webhook.WebHookService;
import com.tianli.tool.ApplicationContextTool;
import com.tianli.tool.ReflectTool;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Data
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class TestDto {

    private String name;

    private String method;

    public String getName() {
        WebHookService.send("【测试环境】" + Objects.requireNonNull(ReflectTool.invoke(ApplicationContextTool.get(name), method, null)
                .toString()));
        return "name";
    }

    public String getMethod() {
        return "method";
    }


}
