package com.tianli.rpc;

import cn.hutool.json.JSONUtil;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.mconfig.ConfigService;
import com.tianli.rpc.dto.InviteDTO;
import com.tianli.rpc.dto.LiquidateDTO;
import com.tianli.rpc.dto.LiquidateResponse;
import com.tianli.rpc.dto.UserInfoDTO;
import com.tianli.tool.JacksonUtils;
import com.tianli.tool.crypto.Crypto;
import com.tianli.tool.time.TimeTool;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.crypto.util.DigestFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.tianli.sso.service.SSOService.WALLET_NEWS_SERVER_URL;

/**
 * @author chenb
 * @apiNote
 * @since 2022-12-05
 **/
@Service
public class RpcService {

    @Resource
    private ConfigService configService;

    public InviteDTO inviteRpc(Long chatId, Long uid) {
        String walletNewsServerUrl = configService.getOrDefault(WALLET_NEWS_SERVER_URL
                , "https://wallet-news.giantdt.com") + "/api/agent/invite/uids";

        CloseableHttpClient client = HttpClients.createDefault();

        try {
            URIBuilder newBuilder = new URIBuilder(walletNewsServerUrl);
            Optional.ofNullable(chatId).ifPresent(id -> newBuilder.setParameter("chatId", id + ""));
            Optional.ofNullable(uid).ifPresent(id -> newBuilder.setParameter("uid", id + ""));

            HttpGet httpGet = new HttpGet(newBuilder.build().toString());
            httpGet.setHeader("Content-Type", "application/json; charset=UTF-8");
            HttpResponse httpResponse = client.execute(httpGet);
            String s = EntityUtils.toString(httpResponse.getEntity());
            return JSONUtil.parseObj(s).getJSONObject("data").toBean(InviteDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw ErrorCodeEnum.NETWORK_ERROR.generalException();
    }

    public UserInfoDTO userInfoDTO(Long uid) {
        String walletNewsServerUrl = configService.getOrDefault(WALLET_NEWS_SERVER_URL
                , "https://wallet-news.giantdt.com") + "/api/user/info/" + uid;
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(walletNewsServerUrl);
        httpGet.setHeader("Content-Type", "application/json; charset=UTF-8");
        try {
            HttpResponse httpResponse = client.execute(httpGet);
            String s = EntityUtils.toString(httpResponse.getEntity());
            return JSONUtil.parseObj(s).getJSONObject("data").toBean(UserInfoDTO.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw ErrorCodeEnum.NETWORK_ERROR.generalException();
    }

    public UserInfoDTO userInfoDTOChatId(Long chatId) {
        String walletNewsServerUrl = configService.getOrDefault(WALLET_NEWS_SERVER_URL
                , "https://wallet-news.giantdt.com") + "/api/user/info/chat/" + chatId;
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(walletNewsServerUrl);
        httpGet.setHeader("Content-Type", "application/json; charset=UTF-8");
        try {
            HttpResponse httpResponse = client.execute(httpGet);
            String s = EntityUtils.toString(httpResponse.getEntity());
            return JSONUtil.parseObj(s).getJSONObject("data").toBean(UserInfoDTO.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw ErrorCodeEnum.NETWORK_ERROR.generalException();
    }

    public String html(String url) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Content-Type", "application/json; charset=UTF-8");
        try {
            HttpResponse httpResponse = client.execute(httpGet);
            return EntityUtils.toString(httpResponse.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw ErrorCodeEnum.NETWORK_ERROR.generalException();
    }

    public String liquidate(LiquidateDTO liquidateDTO) {
        long l = TimeTool.toTimestamp(LocalDateTime.now());
        String sign = Crypto.hmacToString(DigestFactory.createSHA256(), "QVy2OcKI2DMX4m7VcREtbJDygCznE", l + "");
        String walletNewsServerUrl = configService.getOrDefault(WALLET_NEWS_SERVER_URL
                , "https://wallet-news.giantdt.com") + "/api/liquidate/upload";
        HttpPost httpPost = new HttpPost(walletNewsServerUrl);
        httpPost.setHeader("Content-Type", "application/json; charset=UTF-8");
        httpPost.setHeader("sign", sign);
        httpPost.setHeader("timestamp", l + "");
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            StringEntity stringEntity = new StringEntity(JacksonUtils.getObjectMapper().writeValueAsString(liquidateDTO));
            httpPost.setEntity(stringEntity);
            HttpResponse httpResponse = client.execute(httpPost);
            return JSONUtil.parse(EntityUtils.toString(httpResponse.getEntity())).getByPath("data.id", String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw ErrorCodeEnum.SYSTEM_ERROR.generalException();
    }

    public String liquidateResponse(Long recordId, String liquidateId) {
        String walletNewsServerUrl = configService.getOrDefault(WALLET_NEWS_SERVER_URL
                , "https://wallet-news.giantdt.com") + "/api/liquidate/query?record_id=" + recordId
                + "&liquidate_id=" + liquidateId;
        long l = TimeTool.toTimestamp(LocalDateTime.now());
        String sign = Crypto.hmacToString(DigestFactory.createSHA256(), "QVy2OcKI2DMX4m7VcREtbJDygCznE", l + "");
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(walletNewsServerUrl);
        httpGet.setHeader("Content-Type", "application/json; charset=UTF-8");
        httpGet.setHeader("sign", sign);
        httpGet.setHeader("timestamp", l + "");
        try {
            HttpResponse httpResponse = client.execute(httpGet);
            return EntityUtils.toString(httpResponse.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw ErrorCodeEnum.NETWORK_ERROR.generalException();
    }

    public static void main(String[] args) {
        long l = TimeTool.toTimestamp(LocalDateTime.now());
        String sign = Crypto.hmacToString(DigestFactory.createSHA256(), "QVy2OcKI2DMX4m7VcREtbJDygCznE", l + "");
        System.out.println(l);
        System.out.println(sign);
    }
}
