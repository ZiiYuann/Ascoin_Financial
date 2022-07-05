package com.tianli.captcha.phone.service;

import cn.hutool.core.util.StrUtil;
import com.tianli.common.CommonFunction;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.MapTool;
import com.tianli.tool.http.HttpHandler;
import com.tianli.tool.http.HttpRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;

/**
 * @author wangqiyun
 * @since 2021/7/27 18:05
 */

@Service
public class CmSMSService {
    @Resource
    ConfigService configService;

    public void sendSms(String phone, String text) {
        if (phone.startsWith("+660"))
            phone = "+66" + phone.substring(4);
        String cmToken = configService._get("cm_token");
        String cm_from = configService._get("cm_from");
        if (StrUtil.isNotBlank(cmToken) && StrUtil.isNotBlank(cm_from)) {
            MapTool params = MapTool.Map().put("messages", MapTool.Map()
                    .put("authentication", MapTool.Map().put("producttoken", cmToken))
                    .put("msg", Collections.singletonList(MapTool.Map()
                            .put("from", cm_from)
                            .put("to", Collections.singletonList(MapTool.Map().put("number", phone)))
                            .put("minimumNumberOfMessageParts", 1)
                            .put("maximumNumberOfMessageParts", 8)
                            .put("body", MapTool.Map().put("type", "AUTO").put("content", text))
                            .put("reference", "" + CommonFunction.generalId())
                    ))
            );
            String stringResult = HttpHandler.execute(new HttpRequest().setUrl("https://gw.cmtelecom.com/v1.0/message").setMethod(HttpRequest.Method.POST)
                    .setJsonObject(params)).getStringResult();
            System.out.println(stringResult);
        }
    }
}
