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
            asyncService.async(() -> this.sendOperation(MoreObjects.firstNonNull(dev, "") + msg, webHookToken));
        } else {
            asyncService.async(() -> this.sendOperation(MoreObjects.firstNonNull(dev, "") + msg, WebHookToken.BUG_PUSH));
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
            asyncService.async(() -> this.sendOperation(msg, WebHookToken.FUND_PRODUCT));
        } else {
            asyncService.async(() -> this.sendOperation(msg, WebHookToken.BUG_PUSH));
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
        this.sendLinkOperation(jb, WebHookToken.BUG_PUSH);
    }

    private void sendOperation(Object msg, WebHookToken webHookToken) {
        boolean openWebHook = Boolean.parseBoolean(configService.getOrDefaultNoCache(ConfigConstants.OPEN_WEBHOOK_EXCEPTION_PUSH, "false"));
        String dev = configService._get("dev");

        if (StringUtils.isBlank(dev) && WebHookToken.BUG_PUSH.equals(webHookToken)) {
            webHookToken = WebHookToken.PRO_BUG_PUSH;
        }
        if (!openWebHook) {
            return;
        }
        DingDingUtil.textType(msg, webHookToken.getToken(), webHookToken.getSecret());
    }

    private void sendLinkOperation(Object msg, WebHookToken webHookToken) {
        boolean openWebHook = Boolean.parseBoolean(configService.getOrDefaultNoCache(ConfigConstants.OPEN_WEBHOOK_EXCEPTION_PUSH, "false"));
        String dev = configService._get("dev");

        if (StringUtils.isBlank(dev) && WebHookToken.BUG_PUSH.equals(webHookToken)) {
            webHookToken = WebHookToken.PRO_BUG_PUSH;
        }
        if (!openWebHook) {
            return;
        }
        DingDingUtil.linkType(msg, webHookToken.getToken(), webHookToken.getSecret());
    }

}
