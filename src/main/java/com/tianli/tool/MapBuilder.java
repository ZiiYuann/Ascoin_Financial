package com.tianli.tool;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangqiyun on 2017/6/5.
 */
public class MapBuilder {
    private Map<String, String> map = new HashMap<>();

    public static MapBuilder Map() {
        return new MapBuilder();
    }

    public MapBuilder put(String key, String value) {
        map.put(key, value);
        return this;
    }

    public static Map<String, String> stringToMap(String str) {
        return new Gson().fromJson(str, new TypeToken<Map<String, String>>() {
        }.getType());
    }

    public Map<String, String> build() {
        return map;
    }

    public static Map<String, String> trans(Map<String, Object> map) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            result.put(entry.getKey(), entry.getValue().toString());
        }
        return result;
    }

    @Override
    public String toString() {
        return new Gson().toJson(map);
    }
}
