package com.tianli.common;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.json.JSONObject;
import com.tianli.common.async.AsyncService;
import com.tianli.exception.ExceptionMsg;
import com.tianli.exception.ExceptionMsgMapper;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.DingDingUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-09-02
 **/
@Component
public class WebHookService {

    @Resource
    private AsyncService asyncService;
    @Resource
    private ConfigService configService;
    @Resource
    private ExceptionMsgMapper exceptionMsgMapper;

    public void dingTalkSend(String msg, Exception e) {
        asyncService.async(() -> this.dingTalkSendOperation(msg,e));
    }

    private void dingTalkSendOperation(String msg, Exception e){
        String urlPre = configService.get(ConfigConstants.SYSTEM_URL_PATH_PREFIX);
        Long id = CommonFunction.generalId();
        exceptionMsgMapper.insert(new ExceptionMsg(id,ExceptionUtil.stacktraceToString(e), LocalDateTime.now()));

        JSONObject jb = new JSONObject();
        jb.putOnce("title", StringUtils.isBlank(msg) ? "异常信息" : msg);
        jb.putOnce("messageUrl", urlPre + "/api/errMmp/" + id);
        jb.putOnce("text","异常信息");
        DingDingUtil.postWithJson(jb);
    }
}
