package com.tianli.tool.serializer;

import com.google.gson.*;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.security.crypto.codec.Utf8;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Author wangqiyun
 * @Date 2019/7/31 10:43
 */
public class GsonRedisSerializer implements RedisSerializer<Object> {
    @Override
    public byte[] serialize(Object o) throws SerializationException {
        if (o == null)
            return new byte[0];
        String json = gson.toJson(o);
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        jsonObject.addProperty("@class", o.getClass().getName());
        return Utf8.encode(gson.toJson(jsonObject));
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0)
            return null;
        String json = Utf8.decode(bytes);
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        if (jsonObject == null) return null;
        if (jsonObject.get("@class") == null)
            return jsonObject;
        try {
            Class<?> aClass = Class.forName(jsonObject.get("@class").getAsString());
            return gson.fromJson(json, aClass);
        } catch (ClassNotFoundException e) {
            throw new SerializationException("不存在的类");
        }
    }

    private static final Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>)
            (localDateTime, type, jsonSerializationContext) -> new JsonPrimitive(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
            .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (localDate, type, jsonSerializationContext) ->
                    new JsonPrimitive(localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (jsonElement, type, jsonDeserializationContext) -> {
                String datetime = jsonElement.getAsJsonPrimitive().getAsString();
                return LocalDateTime.parse(datetime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }).registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (jsonElement, type, jsonDeserializationContext) -> {
                String datetime = jsonElement.getAsJsonPrimitive().getAsString();
                return LocalDate.parse(datetime, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }).create();
}
