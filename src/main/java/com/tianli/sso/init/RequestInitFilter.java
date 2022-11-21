package com.tianli.sso.init;

import com.google.gson.Gson;
import com.tianli.tool.ApplicationContextTool;
import com.tianli.tool.MapTool;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author wangqiyun
 * @Date 2019/2/28 4:40 PM
 */

@WebFilter(urlPatterns = "/*", filterName = "requestInitFilter", asyncSupported = true)
public class RequestInitFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        RequestInitService requestInitService = ApplicationContextTool.getBean(RequestInitService.class);
        requestInitService.init((HttpServletRequest) servletRequest);
        ContentCachingRequestWrapper httpServletRequest = new ContentCachingRequestWrapper((HttpServletRequest) servletRequest);
        ContentCachingResponseWrapper httpServletResponse = new ContentCachingResponseWrapper((HttpServletResponse) servletResponse);
        filterChain.doFilter(servletRequest, servletResponse);
        byte[] requestByte = httpServletRequest.getContentAsByteArray();
        try {
            String requestBody;
            if (requestByte.length == 0)
                requestBody = new Gson().toJson(httpServletRequest.getParameterMap());
            else
                requestBody = Utf8.decode(requestByte);
//            System.out.println(MapTool.Map()
//                    .put("uid", requestInitService._uid() == null ? 0 : requestInitService._uid())
//                    .put("requestBody", requestBody)
//                    .put("requestMethod", httpServletRequest.getMethod())
//                    .put("requestPath", httpServletRequest.getRequestURI())
//                    .put("responseStatus", httpServletResponse.getStatus())
//                    .put("responseBody", Utf8.decode(httpServletResponse.getContentAsByteArray()))
//                    .put("imei", requestInitService.imei())
//                    .put("deviceType", requestInitService.deviceType())
//                    .put("ip", requestInitService.ip())
//                    .put("requestId", requestInitService.requestId())
//                    .put("lat", requestInitService.lat())
//                    .put("lng", requestInitService.lng())
//            );
        } catch (IllegalArgumentException ignored) {
        }
        httpServletResponse.copyBodyToResponse();
        requestInitService.destroy();
    }

    @Override
    public void destroy() {

    }
}
