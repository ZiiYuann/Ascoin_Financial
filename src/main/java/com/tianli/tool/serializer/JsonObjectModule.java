package com.tianli.tool.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gson.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Author wangqiyun
 * @Date 2019/7/31 11:28
 */
public class JsonObjectModule extends SimpleModule {
    public JsonObjectModule() {
        this.addSerializer(JsonObject.class, new JsonSerializer<JsonObject>() {
            @Override
            public void serialize(JsonObject jsonObject, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                if (jsonObject == null)
                    jsonGenerator.writeNull();
                else {
                    jsonObject.addProperty("@class", "com.google.gson.JsonObject");
                    jsonGenerator.writeRawValue(gson.toJson(jsonObject));
                }
            }
        });
        this.addDeserializer(JsonObject.class, new com.fasterxml.jackson.databind.JsonDeserializer<JsonObject>() {
            @Override
            public JsonObject deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
                if (parser == null) return null;
                TreeNode treeNode = parser.readValueAsTree();
                if (treeNode == null) return null;
                return gson.fromJson(treeNode.toString(), JsonObject.class);
            }
        });
    }

    private static final Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, (com.google.gson.JsonSerializer<LocalDateTime>)
            (localDateTime, type, jsonSerializationContext) -> new JsonPrimitive(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
            .registerTypeAdapter(LocalDate.class, (com.google.gson.JsonSerializer<LocalDate>) (localDate, type, jsonSerializationContext) ->
                    new JsonPrimitive(localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (jsonElement, type, jsonDeserializationContext) -> {
                String datetime = jsonElement.getAsJsonPrimitive().getAsString();
                return LocalDateTime.parse(datetime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }).registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (jsonElement, type, jsonDeserializationContext) -> {
                String datetime = jsonElement.getAsJsonPrimitive().getAsString();
                return LocalDate.parse(datetime, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }).create();
}
