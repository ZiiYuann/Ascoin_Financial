package com.tianli.tool;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tianli.tool.http.HttpHandler;
import com.tianli.tool.http.HttpRequest;
import com.tianli.tool.judge.JsonObjectTool;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class IpAddressTool {

    public String getAddress(String ip) {
        String stringResult = HttpHandler.execute(new HttpRequest().setUrl("https://api.ip.sb/geoip/" + ip)).getStringResult();
        JsonObject jsonObject = new Gson().fromJson(stringResult, JsonObject.class);
        String country = JsonObjectTool.getAsString(jsonObject, "country");
        String region = JsonObjectTool.getAsString(jsonObject, "region");
        String city = JsonObjectTool.getAsString(jsonObject, "city");
        List<String> address = new ArrayList<>();
        if(city != null) address.add(city);
        if(region != null) address.add(region);
        if(country != null) address.add(country);
        return String.join(".", address);
    }
}
