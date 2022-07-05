package com.tianli.tool;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tianli.common.HttpUtils;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.mconfig.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class GoogleVerify {

    public static boolean check(String checkCode, double minScore) {
        JsonObject jsonObject = check_(checkCode);
        JsonElement success;
        if(Objects.isNull(jsonObject) || Objects.isNull((success = jsonObject.get("success"))) || !success.getAsBoolean()){
            return false;
        }
        JsonElement score = jsonObject.get("score");
        return Objects.nonNull(score) && score.getAsDouble() >= minScore;
    }

    public static JsonObject check_(String checkCode) {
        String check = check(checkCode);
        if(StringUtils.isBlank(check)){
            return null;
        }
        return new Gson().fromJson(check, JsonObject.class);
    }

    /**
     * 人机校验接口
     *
     * @param checkCode 校验码
     * @return response的参数String
     *
     * ex: response:
     * {
     * "success": true,
     * "challenge_ts": "2022-02-15T06:03:24Z",
     * "hostname": "localhost",
     * "score": 0.9,
     * "action": "test"
     * }
     */
    public static String check(String checkCode) {
        Map<String, String> querys = new HashMap<>();
        // 私钥
        ConfigService configService = ApplicationContextTool.getBean("configService", ConfigService.class);
        if (Objects.isNull(configService)){
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }
        String google_captcha_server_key = configService.getOrDefault("google_captcha_server_key", "6Le973seAAAAAPBJSO8tGAzoGs20ebo8sXHu9DQA");
        querys.put("secret", google_captcha_server_key);
        querys.put("response", checkCode);
        // https://www.google.com/recaptcha/api/siteverify
        String json = null;
        try {
            HttpResponse post = HttpUtils.doPost("https://www.recaptcha.net", "/recaptcha/api/siteverify", "POST", Map.of(), querys, Map.of());
            json = EntityUtils.toString(post.getEntity());
        } catch (Exception ignored) {
            log.warn("execute google_reCAPTCHA fail!");
        }
        return json;
    }

    public static void main(String[] args) {
        String qw = check("HFdGt3ZxJQaG52YWtCWkFTWwkqGRgqRxAENzAxOTZocwQqaAknKkBkMC5RZTFXNGByYipREBgEXV5EaxESWj4PdzkrRC8ka3AdKmgRMSkucDgzfTdxAD9rcR4hIxkTB2xaXGNBTCE1DAsyWTgwIncAc3wsEF1ZWQswM2hnCxVaB2ohQlZefw8EBwktbw");
        System.out.println(qw);
    }

}
