package com.tianli.currency_token.transfer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tianli.currency_token.mapper.ChainType;
import com.tianli.currency_token.transfer.dto.TransferDTO;
import com.tianli.management.ruleconfig.ConfigConstants;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.MapTool;
import com.tianli.tool.http.HttpHandler;
import com.tianli.tool.http.HttpRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransferRequestService {

    public long latestBlock(ChainType chainType){
        String chain = null;
        switch (chainType) {
            case bep20:
                chain = "BSC";
                break;
            case erc20:
                chain = "ETH";
                break;
            case trc20:
                chain = "TRON";
                break;
        }
        String url = configService.get(ConfigConstants.SYNC_TRANSFER_URL);
        String stringResult = HttpHandler.execute(new HttpRequest().setUrl(url + "/api/tx/blockNumber?chainType=" + chain)
                .setMethod(HttpRequest.Method.GET)).getStringResult();
        JsonObject jsonObject = new Gson().fromJson(stringResult, JsonObject.class);
        return jsonObject.get("data").getAsLong();
    }

    public List<TransferDTO> getTransferListByBlock(ChainType chainType, Long block, List<String> contracts){
        String chain = null;
        switch (chainType) {
            case bep20:
                chain = "BSC";
                break;
            case erc20:
                chain = "ETH";
                break;
            case trc20:
                chain = "TRON";
                break;
        }
        String url = configService.get(ConfigConstants.SYNC_TRANSFER_URL);

        String stringResult = HttpHandler.execute(new HttpRequest().setUrl(url + "/api/tx/list")
                .setMethod(HttpRequest.Method.POST).setJsonObject(MapTool.Map()
                        .put("chainType", chain)
                        .put("contracts", contracts)
                        .put("blockNumber", block)
                )
        ).getStringResult();
        JsonObject jsonObject = new Gson().fromJson(stringResult, JsonObject.class);
        List<TransferDTO> result = new ArrayList<>();
        JsonArray jsonArray = jsonObject.getAsJsonArray("data");
        for(JsonElement ji : jsonArray) {
            TransferDTO transferDTO = new Gson().fromJson(ji, TransferDTO.class);
            result.add(transferDTO);
        }
        return result;
    }

    public TransferDTO getTransferByTx(ChainType chainType, String tx){
        String chain = null;
        switch (chainType) {
            case bep20:
                chain = "BSC";
                break;
            case erc20:
                chain = "ETH";
                break;
            case trc20:
                chain = "TRON";
                break;
        }
        String url = configService.get(ConfigConstants.SYNC_TRANSFER_URL);
        String stringResult = HttpHandler.execute(new HttpRequest()
                .setUrl(url + "/api/tx/txDetail?" + "chainType=" + chain + "&txhash=" + tx)
                .setMethod(HttpRequest.Method.GET)).getStringResult();
        JsonObject jsonObject = new Gson().fromJson(stringResult, JsonObject.class);
        JsonArray jo = jsonObject.get("data").getAsJsonArray();
        if(jo.size() == 0) return null;
        return new Gson().fromJson(new Gson().toJson(jo.get(0)), TransferDTO.class);
    }

    @Resource
    private ConfigService configService;

}
