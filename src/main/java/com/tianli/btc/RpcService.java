package com.tianli.btc;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.tool.MapBuilder;
import com.tianli.tool.MapTool;
import com.tianli.tool.http.HttpHandler;
import com.tianli.tool.http.HttpRequest;
import com.tianli.tool.judge.JsonObjectTool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Author cs
 * @Date 2022-01-18 9:51 上午
 */
//@Service
public class RpcService {

    @Value("${rpc.url}")
    private String url;
    @Value("${rpc.username}")
    private String username;
    @Value("${rpc.password}")
    private String password;

    public RawTransaction getrawtransaction(String txid) {
        JsonObject jsonObject = httpJson("getrawtransaction", txid, 1);
        JsonObject result = JsonObjectTool.getAsJsonObject(jsonObject, "result");
        if (result == null) ErrorCodeEnum.NETWORK_ERROR.throwException();
        return new Gson().fromJson(result.toString(), RawTransaction.class);
    }

    public Long getblockheaderForHeight(String blockhash) {
        JsonObject jsonObject = httpJson("getblockheader", blockhash);
        Long height = JsonObjectTool.getAsLong(jsonObject, "result.height");
        if (height == null) ErrorCodeEnum.NETWORK_ERROR.throwException();
        return height;
    }

    public long getblockcount() {
        JsonObject jsonObject = httpJson("getblockcount");
        Long result = JsonObjectTool.getAsLong(jsonObject, "result");
        if (result == null) ErrorCodeEnum.NETWORK_ERROR.throwException();
        return result;
    }

    public String getblockhash(long blockcount) {
        JsonObject jsonObject = httpJson("getblockhash", blockcount);
        String result = JsonObjectTool.getAsString(jsonObject, "result");
        if (result == null) ErrorCodeEnum.NETWORK_ERROR.throwException();
        return result;
    }

    public List<String> getblock(String blockhash) {
        JsonObject jsonObject = httpJson("getblock", blockhash);
        JsonArray result = JsonObjectTool.getAsJsonArray(jsonObject, "result.tx");
        if (result == null) ErrorCodeEnum.NETWORK_ERROR.throwException();
        List<String> tx = new ArrayList<>();
        for (JsonElement jsonElement : result) {
            tx.add(jsonElement.getAsString());
        }
        return tx;
    }

    public JsonObject omni_gettransaction(String txid) {
        JsonObject jsonObject = httpJson("omni_gettransaction", txid);
        if (JsonObjectTool.getAsInt(jsonObject, "error.code") != null) {
            return null;
        }
        JsonObject result = JsonObjectTool.getAsJsonObject(jsonObject, "result");
        if (result == null) ErrorCodeEnum.NETWORK_ERROR.throwException();
        return result;
    }

    public String sendrawtransaction(String hexstring) {
        JsonObject jsonObject = httpJson("sendrawtransaction", hexstring);
        String result = JsonObjectTool.getAsString(jsonObject, "result");
        String errorMsg = JsonObjectTool.getAsString(jsonObject, "error.message");
        if (StringUtils.isEmpty(result)) {
            if ("transaction already in block chain".equals(errorMsg)) return null;
            ErrorCodeEnum.NETWORK_ERROR.throwException();
        }
        return result;
    }

    public String createrawtransaction(List<SendDTO.Vin> vinList, JsonObject vout) {
        JsonObject jsonObject = httpJson("createrawtransaction", vinList, vout);
        String result = JsonObjectTool.getAsString(jsonObject, "result");
        if (StringUtils.isEmpty(result)) ErrorCodeEnum.NETWORK_ERROR.throwException();
        return result;
    }

    public RawTransaction getrawtransactionNull(String txid) {
        JsonObject jsonObject = httpJson("getrawtransaction", txid, 1);
        JsonObject result = JsonObjectTool.getAsJsonObject(jsonObject, "result");
        if (result == null) return null;
        return new Gson().fromJson(result.toString(), RawTransaction.class);
    }

    public RawTransaction decoderawtransaction(String hexstring) {
        JsonObject jsonObject = httpJson("decoderawtransaction", hexstring);
        JsonObject result = JsonObjectTool.getAsJsonObject(jsonObject, "result");
        if (result == null) ErrorCodeEnum.NETWORK_ERROR.throwException();
        return new Gson().fromJson(result.toString(), RawTransaction.class);
    }

    public String signrawtransaction(String hexstring, String... privateKey) {
        JsonObject jsonObject = httpJson("signrawtransactionwithkey", hexstring, Arrays.asList(privateKey));
        Boolean complete = JsonObjectTool.getAsBool(jsonObject, "result.complete");
        String result = JsonObjectTool.getAsString(jsonObject, "result.hex");
        if (complete == null || StringUtils.isEmpty(result) || !complete) ErrorCodeEnum.NETWORK_ERROR.throwException();
        return result;
    }

    private JsonObject httpJson(String method, Object... params) {
        return new Gson().fromJson(http(method, params), JsonObject.class);
    }

    private String http(String method, Object... params) {
        long id = ThreadLocalRandom.current().nextLong(Long.MAX_VALUE);
        return HttpHandler.execute(new HttpRequest().setUrl(url).setMethod(HttpRequest.Method.POST).setRequestHeader(
                        MapBuilder.Map().put("Authorization", "Basic " + Base64.getEncoder().encodeToString(Utf8.encode(username + ":" + password))).build()
                ).setJsonObject(MapTool.Map().put("method", method).put("id", id).put("jsonrpc", "2.0").put("params", params)))
                .getStringResult();
    }
}
