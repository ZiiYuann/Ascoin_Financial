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
        asyncService.async(() -> this.bugMsgSend(msg, e, null));
    }

    public void dingTalkSend(String msg, String url, Exception e) {
        log.error(msg + ":" + e.getMessage());
        asyncService.async(() -> this.bugMsgSend(msg, e, url));
    }

    public void dingTalkSend(String msg, WebHookToken webHookToken) {
        String dev = configService._get("dev");
        if (StringUtils.isBlank(dev)) {
            asyncService.async(() -> this.dingTalkSendOperation(MoreObjects.firstNonNull(dev, "") + msg, webHookToken));
        } else {
            asyncService.async(() -> this.dingTalkSendOperation(MoreObjects.firstNonNull(dev, "") + msg, WebHookToken.BUG_PUSH));
        }

    }

    public void dingTalkSend(String msg) {
        dingTalkSend(msg, WebHookToken.BUG_PUSH);
    }

    /**
     * 基金相关的通知，以后优化
     */
    public void fundSend(String msg) {
        String dev = configService._get("dev");
        if (StringUtils.isBlank(dev)) {
            asyncService.async(() -> this.dingTalkSendOperation(msg, WebHookToken.FUND_PRODUCT));
        } else {
            asyncService.async(() -> this.dingTalkSendOperation(msg, WebHookToken.BUG_PUSH));
        }
    }

    private void bugMsgSend(String msg, Exception e, String url) {
        boolean openWebHook = Boolean.parseBoolean(configService.getOrDefaultNoCache(ConfigConstants.OPEN_WEBHOOK_EXCEPTION_PUSH, "false"));
        if (!openWebHook) {
            return;
        }
        String urlPre = configService.get(ConfigConstants.SYSTEM_URL_PATH_PREFIX);
        String dev = configService._get("dev");
        Long id = CommonFunction.generalId();
        String s = ExceptionUtil.stacktraceToString(e, 5000);
        if (Objects.nonNull(url)) {
            s = "【" + url + "】" + s;
        }
        exceptionMsgMapper.insert(new ExceptionMsg(id, s, LocalDateTime.now()));
        JSONObject jb = new JSONObject();
        jb.putOnce("title", MoreObjects.firstNonNull(dev, "") + (StringUtils.isBlank(msg) ? "异常信息" : msg));
        jb.putOnce("messageUrl", urlPre + "/api/errMmp/" + id);
        jb.putOnce("text", s);
        DingDingUtil.linkType(jb, WebHookToken.BUG_PUSH.getToken(), WebHookToken.BUG_PUSH.getSecret());
    }

    private void dingTalkSendOperation(String msg, WebHookToken webHookToken) {
        boolean openWebHook = Boolean.parseBoolean(configService.getOrDefaultNoCache(ConfigConstants.OPEN_WEBHOOK_EXCEPTION_PUSH, "false"));
        if (!openWebHook) {
            return;
        }
        DingDingUtil.textType(msg, webHookToken.getToken(), webHookToken.getSecret());
    }

    public static void send(String msg) {
        DingDingUtil.textType(msg, WebHookToken.BUG_PUSH.getToken(), WebHookToken.BUG_PUSH.getSecret());
    }

}
