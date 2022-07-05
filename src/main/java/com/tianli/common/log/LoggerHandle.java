package com.tianli.common.log;

import com.tianli.common.init.RequestInitService;
import com.tianli.tool.MapTool;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Map;

/**
 * @Author wangqiyun
 * @Date 2019/3/4 10:34 AM
 */

@Component
@Aspect
public class LoggerHandle {

    @Around("@annotation(logs)")
    public Object logs(ProceedingJoinPoint pjp, Logs logs) throws Throwable {
        Object[] args = pjp.getArgs();
        String functionSignature = pjp.getSignature().toLongString();
        try {
            Object result = pjp.proceed(args);
            System.out.println(MapTool.Map()
                    .put("requestId", requestInitService.requestId())
                    .put("uid", requestInitService._uid() == null ? 0 : requestInitService._uid())
                    .put("ip", requestInitService.ip())
                    .put("deviceType", requestInitService.deviceType())
                    .put("imei", requestInitService.imei())
                    .put("now", requestInitService.now())
                    .put("note", logs.value())
                    .put("functionSignature", functionSignature)
                    .put("args", args)
                    .put("result", result)
            );
            return result;
        } catch (Exception e) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter(byteArrayOutputStream);
            e.printStackTrace(writer);
            writer.close();
            System.out.println(MapTool.Map()
                    .put("requestId", requestInitService.requestId())
                    .put("uid", requestInitService._uid() == null ? 0 : requestInitService._uid())
                    .put("ip", requestInitService.ip())
                    .put("deviceType", requestInitService.deviceType())
                    .put("imei", requestInitService.imei())
                    .put("now", requestInitService.now())
                    .put("note", logs.value())
                    .put("functionSignature", functionSignature)
                    .put("args", args)
                    .put("exception", Utf8.decode(byteArrayOutputStream.toByteArray()))
            );
            throw e;
        }
    }

    public void log(Map<String, Object> param) {
        MapTool mapTool = MapTool.Map()
                .put("requestId", requestInitService.requestId())
                .put("uid", requestInitService._uid() == null ? 0 : requestInitService._uid())
                .put("ip", requestInitService.ip())
                .put("deviceType", requestInitService.deviceType())
                .put("imei", requestInitService.imei())
                .put("now", requestInitService.now());
        mapTool.putAll(param);
        System.out.println(mapTool);
    }

    @Resource
    RequestInitService requestInitService;
}
