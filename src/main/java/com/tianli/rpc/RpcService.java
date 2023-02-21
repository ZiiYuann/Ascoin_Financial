package com.tianli.rpc;

import cn.hutool.json.JSONUtil;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.mconfig.ConfigService;
import com.tianli.rpc.dto.InviteDTO;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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

}
