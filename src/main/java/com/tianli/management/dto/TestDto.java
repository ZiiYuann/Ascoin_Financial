package com.tianli.management.dto;

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
        log.info(Objects.requireNonNull(ReflectTool.invoke(ApplicationContextTool.get(name), method, null)).toString());
        return name;
    }
}
