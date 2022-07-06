package com.tianli.exception;

import com.tianli.common.init.RequestInitService;
import com.tianli.tool.ApplicationContextTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

@Slf4j
public class ExceptionUtils {

    public static void printStackTrace(Throwable e) {
        try {
            RequestInitService service = ApplicationContextTool.getBean(RequestInitService.class);
            String requestId = "null";
            if (Objects.nonNull(service)) {
                requestId = service.requestId();
            }
            printStackTrace_(e, requestId);
        } catch (Throwable ignored) {
        }
    }

    public static void printStackTrace_(Throwable e, String reqId) {
        printStackTrace_(null, e, reqId);
    }

    public static void printStackTrace_(String errMsg, Throwable e, String reqId) {
        StringBuilder errorMsg = new StringBuilder();

        if (StringUtils.isNotBlank(errMsg)){
            errorMsg.append("System msg: ").append(errMsg).append("\t");
        }

        errorMsg.append("RequestId: ").append(reqId).append("\t");
        // Print cause, if any
        Throwable ourCause = e.getCause();
        if (ourCause != null)
            errorMsg.append("\tCaused by: ").append(ourCause);

        // Print our stack trace
        errorMsg.append(e.toString());
        StackTraceElement[] trace = e.getStackTrace();
        for (StackTraceElement traceElement : trace)
            errorMsg.append("\tat ").append(traceElement);
        log.warn(errorMsg.toString());
    }
}
