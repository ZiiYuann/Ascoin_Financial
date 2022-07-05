package com.tianli.tool.judge;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author wangqiyun
 * @Date 2019/7/18 15:27
 */
public class JsonObjectTool {


    public static Long getAsLong(JsonObject jsonObject, String text) {
        JsonElement jsonElement = get(jsonObject, text);
        if (jsonElement == null) return null;
        return jsonElement.getAsLong();
    }

    public static Integer getAsInt(JsonObject jsonObject, String text) {
        JsonElement jsonElement = get(jsonObject, text);
        if (jsonElement == null) return null;
        return jsonElement.getAsInt();
    }

    public static String getAsString(JsonObject jsonObject, String text) {
        JsonElement jsonElement = get(jsonObject, text);
        if (jsonElement == null) return null;
        return jsonElement.getAsString();
    }

    public static Double getAsDouble(JsonObject jsonObject, String text) {
        JsonElement jsonElement = get(jsonObject, text);
        if (jsonElement == null) return null;
        return jsonElement.getAsDouble();
    }

    public static Boolean getAsBool(JsonObject jsonObject, String text) {
        JsonElement jsonElement = get(jsonObject, text);
        if (jsonElement == null) return null;
        return jsonElement.getAsBoolean();
    }

    public static JsonObject getAsJsonObject(JsonObject jsonObject, String text) {
        JsonElement jsonElement = get(jsonObject, text);
        if (jsonElement == null) return null;
        return jsonElement.getAsJsonObject();
    }

    public static JsonArray getAsJsonArray(JsonObject jsonObject, String text) {
        JsonElement jsonElement = get(jsonObject, text);
        if (jsonElement == null) return null;
        return jsonElement.getAsJsonArray();
    }

    public static JsonElement get(JsonObject jsonObject, String text) {
        if (jsonObject == null || StringUtils.isEmpty(text)) return null;
        String[] split = text.split("\\.");
        JsonElement now = jsonObject;
        for (String str : split) {
            jsonObject = now.getAsJsonObject();
            Matcher matcher = pattern.matcher(str);
            if (matcher.matches()) {
                String first = matcher.group(1), second = matcher.group(2);
                now = jsonObject.get(first);
                if (now == null || now instanceof JsonNull) return null;
                Matcher matcher1 = arr.matcher(second);
                while (matcher1.find()) {
                    JsonArray jsonArray = now.getAsJsonArray();
                    int index = Integer.parseInt(matcher1.group(1));
                    if (jsonArray.size() <= index)
                        return null;
                    now = jsonArray.get(index);
                    if (now == null || now instanceof JsonNull) return null;
                }
            } else {
                now = jsonObject.get(str);
            }
            if (now == null || now instanceof JsonNull) return null;
        }
        return now;
    }

    private static final Pattern pattern = Pattern.compile("^([a-z|A-Z|0-9|_]+)((\\[[0-9]+])+)$"), arr = Pattern.compile("\\[([0-9]+)]");
}
