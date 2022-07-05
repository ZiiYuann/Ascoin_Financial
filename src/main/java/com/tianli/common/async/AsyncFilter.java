package com.tianli.common.async;

import com.tianli.tool.ApplicationContextTool;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

/**
 * @Author wangqiyun
 * @Date 2019/2/28 4:18 PM
 */

@WebFilter(urlPatterns = "/*", filterName = "asyncFilter", asyncSupported = true)
public class AsyncFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        AsyncService asyncService = ApplicationContextTool.getBean(AsyncService.class);
        if (asyncService != null)
            asyncService.clear();
        filterChain.doFilter(servletRequest, servletResponse);
        if (asyncService != null)
            asyncService.postRun();
    }

    @Override
    public void destroy() {

    }
}
