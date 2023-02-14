package com.tianli.rpc;

import cn.hutool.json.JSONUtil;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.mconfig.ConfigService;
import com.tianli.rpc.dto.InviteDTO;
import com.tianli.rpc.dto.UserInfoDTO;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

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

    public InviteDTO inviteRpc(Long chatId) {
        String walletNewsServerUrl = configService.getOrDefault(WALLET_NEWS_SERVER_URL
                , "https://wallet-news.giantdt.com") + "/api/agent/invite/uids?chatId=" + chatId;
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(walletNewsServerUrl);
        httpGet.setHeader("Content-Type", "application/json; charset=UTF-8");
        try {
            HttpResponse httpResponse = client.execute(httpGet);
            String s = EntityUtils.toString(httpResponse.getEntity());
            return JSONUtil.parseObj(s).getJSONObject("data").toBean(InviteDTO.class);
        } catch (IOException e) {
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

}
