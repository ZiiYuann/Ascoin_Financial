package com.tianli.common;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.json.JSONObject;
import com.tianli.common.async.AsyncService;
import com.tianli.exception.ExceptionMsg;
import com.tianli.exception.ExceptionMsgMapper;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.DingDingUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

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

    public void dingTalkSend(String msg, String title) {

        asyncService.async(() -> this.dingTalkSendOperation(msg, title));
    }

    private void dingTalkSendOperation(String msg, Exception e) {
        boolean openWebHook = Boolean.parseBoolean(configService.getOrDefaultNoCache(ConfigConstants.OPEN_WEBHOOK_EXCEPTION_PUSH, "false"));
        if (!openWebHook) {
            return;
        }
        String urlPre = configService.get(ConfigConstants.SYSTEM_URL_PATH_PREFIX);
        Long id = CommonFunction.generalId();
        exceptionMsgMapper.insert(new ExceptionMsg(id, ExceptionUtil.stacktraceToString(e, 5000), LocalDateTime.now()));

        JSONObject jb = new JSONObject();
        jb.putOnce("title", StringUtils.isBlank(msg) ? "异常信息" : msg);
        jb.putOnce("messageUrl", urlPre + "/api/errMmp/" + id);
        jb.putOnce("text", ExceptionUtil.getMessage(e));
        DingDingUtil.postWithJson(jb);
    }

    private void dingTalkSendOperation(String msg, String title) {
        boolean openWebHook = Boolean.parseBoolean(configService.getOrDefaultNoCache(ConfigConstants.OPEN_WEBHOOK_EXCEPTION_PUSH, "false"));
        if (!openWebHook) {
            return;
        }
        String urlPre = configService.get(ConfigConstants.SYSTEM_URL_PATH_PREFIX);
        Long id = CommonFunction.generalId();
        exceptionMsgMapper.insert(new ExceptionMsg(id, title, LocalDateTime.now()));

        JSONObject jb = new JSONObject();
        jb.putOnce("title", StringUtils.isBlank(msg) ? "推送信息" : msg);
        jb.putOnce("messageUrl", urlPre + "/api/errMmp/" + id);
        jb.putOnce("text", title);
        DingDingUtil.postWithJson(jb);
    }
}
