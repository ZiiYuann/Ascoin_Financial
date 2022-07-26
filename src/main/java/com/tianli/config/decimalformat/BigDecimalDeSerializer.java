package com.tianli.config.decimalformat;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.math.BigDecimal;

@JsonComponent
public class BigDecimalDeSerializer extends JsonDeserializer<BigDecimal> {
    @Override
    public BigDecimal deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {

        // 千分位分隔的数值从前端到后端是需要反序列化为BigDecimal。需要去掉“,”
        // return new BigDecimal(jsonParser.getText().replaceAll(",", ""));

        // 上面的代码是 copy 过来的, 原本是用于解析 #,###.000 这样的字符串, 本项目的场景暂时不需要
        return new BigDecimal(jsonParser.getText());
    }
}

