package com.tianli.common.webhook;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.json.JSONObject;
import com.google.common.base.MoreObjects;
import com.tianli.common.CommonFunction;
import com.tianli.common.ConfigConstants;
import com.tianli.common.async.AsyncService;
import com.tianli.exception.ExceptionMsg;
import com.tianli.exception.ExceptionMsgMapper;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.webhook.DingDingUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author chenb
 * @apiNote
 * @since 2022-09-02
 **/
@Slf4j
@Component
public class WebHookService {

    @Resource
    private AsyncService asyncService;
    @Resource
    private ConfigService configService;
    @Resource
    private ExceptionMsgMapper exceptionMsgMapper;

    public void dingTalkSend(String msg, Exception e) {
        log.error(msg + ":" + e.getMessage());
        asyncService.async(() -> this.dingTalkSendOperation(msg, e));
    }

    public void dingTalkSend(String msg) {
        asyncService.async(() -> this.dingTalkSendOperation(msg, null, null));
    }

    /**
     * 基金相关的通知，以后优化
     */
    public void fundSend(String msg) {
        asyncService.async(() -> this.dingTalkSendOperation(msg, "e9ba9212c2bfebc948d844dc7d5ba72a82acf79be36cbc46d5e507a8fa13c"
                , "SEC46e08bbc82c43cfa0a35aba643eef004cfe78ac774790eba341a21b7079b6d03"));
//        asyncService.async(() -> this.dingTalkSendOperation(msg,null,null));
    }

    private void dingTalkSendOperation(String msg, Exception e) {
        boolean openWebHook = Boolean.parseBoolean(configService.getOrDefaultNoCache(ConfigConstants.OPEN_WEBHOOK_EXCEPTION_PUSH, "false"));
        if (!openWebHook) {
            return;
        }
        String urlPre = configService.get(ConfigConstants.SYSTEM_URL_PATH_PREFIX);
        String dev = configService._get("dev");
        Long id = CommonFunction.generalId();
        exceptionMsgMapper.insert(new ExceptionMsg(id, ExceptionUtil.stacktraceToString(e, 5000), LocalDateTime.now()));

        JSONObject jb = new JSONObject();
        jb.putOnce("title", MoreObjects.firstNonNull(dev, "") + (StringUtils.isBlank(msg) ? "异常信息" : msg));
        jb.putOnce("messageUrl", urlPre + "/api/errMmp/" + id);
        jb.putOnce("text", ExceptionUtil.getMessage(e));
        DingDingUtil.linkType(jb, "1a1216a39f18e8022b6795014424a9fcf5d62a5f00d3666c11127b21841eb718"
                , "SEC52152f460aaf1c4c77592f46674aadf9592fcca6d99974b0b7fb74cd66f20be3");
    }

    private void dingTalkSendOperation(String msg, String token, String secret) {
        boolean openWebHook = Boolean.parseBoolean(configService.getOrDefaultNoCache(ConfigConstants.OPEN_WEBHOOK_EXCEPTION_PUSH, "false"));
        if (!openWebHook) {
            return;
        }
        String dev = configService._get("dev");

        if (StringUtils.isBlank(token) || StringUtils.isBlank(secret)) {
            DingDingUtil.textType(MoreObjects.firstNonNull(dev, "") + msg,
                    "1a1216a39f18e8022b6795014424a9fcf5d62a5f00d3666c11127b21841eb718"
                    , "SEC52152f460aaf1c4c77592f46674aadf9592fcca6d99974b0b7fb74cd66f20be3");
            return;
        }
        DingDingUtil.textType(msg, token, secret);

    }

}
