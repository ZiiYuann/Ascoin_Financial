package com.tianli.captcha.phone.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tianli.tool.MapBuilder;
import com.tianli.tool.http.HttpHandler;
import com.tianli.tool.http.HttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @Author wangqiyun
 * @Date 2019/2/18 2:37 PM
 */

@Service
public class YunpianRequestService {
    public boolean sendSMS(String apikey, String phone, String text, Map<String, String> param) {
        for (Map.Entry<String, String> entry : param.entrySet()) {
            text = text.replace("#" + entry.getKey() + "#", entry.getValue());
        }
        Map<String, String> map = MapBuilder.Map().put("apikey", apikey).put("mobile", phone).put("text", text).build();
        String result = this.http(map);
        if (!StringUtils.isEmpty(result)) {
            JsonObject jsonObject = new Gson().fromJson(result, JsonObject.class);
            if (jsonObject != null && jsonObject.get("code") != null && jsonObject.get("code").getAsInt() == 0)
                return true;
        }
        return false;
    }


    private String http(Map<String, String> param) {
        HttpRequest httpRequest = new HttpRequest().setUrl(URL).setMethod(HttpRequest.Method.POST)
                .setQueryMap(param);
        return HttpHandler.execute(httpRequest).getStringResult();
    }

    private final String URL = "https://us.yunpian.com/v2/sms/single_send.json";
}
