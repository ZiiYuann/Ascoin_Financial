package com.tianli.tool;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.TreeMap;

/**
 * Created by wangqiyun on 16/5/23.
 */
public class TreeMapTool extends TreeMap<String, Object> {
    @Override
    public TreeMapTool put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    public static TreeMapTool Map() {
        return new TreeMapTool();
    }

    public static TreeMap<String, Object> stringToMap(String str) {
        return new Gson().fromJson(str, new TypeToken<TreeMap<String, Object>>() {
        }.getType());
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
