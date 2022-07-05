package com.tianli.tool;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by wangqiyun on 16/5/23.
 */
public class MapTool extends HashMap<String, Object> {
    @Override
    @NonNull
    public MapTool put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    public static MapTool Map() {
        return new MapTool();
    }

    public static MapTool Mapok() {
        MapTool mapTool = new MapTool();
        mapTool.put("code", "0");
        return mapTool;
    }

    public static MapTool Mapoktime() {
        MapTool mapTool = Mapok();
        mapTool.put("time", System.currentTimeMillis());
        return mapTool;
    }

    public static LinkedHashMap<String, Object> objectToMap(Object o) {
        return stringToMap(json(o));
    }

    public static LinkedHashMap<String, Object> stringToMap(String str) {
        return new Gson().fromJson(str, new TypeToken<LinkedHashMap<String, Object>>() {
        }.getType());
    }

    public static String json(Object o) {
        return new Gson().toJson(o);
    }

    public static <T> T toObject(Object o, Class<T> dest) {
        return new Gson().fromJson(json(o), dest);
    }

    public static JsonObject toJsonObject(Object o) {
        return new Gson().fromJson(json(o), JsonObject.class);
    }

    public static JsonObject toJsonObject(String str) {
        return new Gson().fromJson(str, JsonObject.class);
    }

    public static <S, D> D assign(D dest, S src) {
        LinkedHashMap<String, Object> destMap = objectToMap(dest);
        destMap.putAll(objectToMap(src));
        return (D) new Gson().fromJson(new Gson().toJson(destMap), dest.getClass());
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
