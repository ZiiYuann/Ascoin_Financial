package com.tianli.accountred.vo;

import com.tianli.accountred.enums.RedEnvelopeChannel;
import com.tianli.common.Constants;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.ApplicationContextTool;
import com.tianli.tool.crypto.PBE;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

import static com.tianli.sso.service.SSOService.WALLET_NEWS_SERVER_URL;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-19
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedEnvelopeGiveVO {

    /**
     * 钱包id
     */
    private Long id;

    /**
     * 站外url
     */
    private RedEnvelopeChannel channel;

    private String externUrl;


    public RedEnvelopeGiveVO(Long id, RedEnvelopeChannel channel) {
        this.id = id;
        this.channel = channel;
    }

    public String getExternUrl() {
        if (!RedEnvelopeChannel.EXTERN.equals(channel)) {
            return null;
        }

        ConfigService bean = ApplicationContextTool.getBean(ConfigService.class);
        Optional.ofNullable(bean).orElseThrow(ErrorCodeEnum.SYSTEM_ERROR::generalException);
        return bean
                .getOrDefault(WALLET_NEWS_SERVER_URL, "https://wallet-news.giantdt.com")
                + "/openapi/red/extern/get?content="
                + PBE.encryptBase64(Constants.RED_SALT, Constants.RED_SECRET_KEY, id + "").replace("+","%2B");
    }

}
