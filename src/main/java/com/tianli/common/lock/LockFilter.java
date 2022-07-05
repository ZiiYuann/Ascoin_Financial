package com.tianli.common.lock;

import com.tianli.tool.ApplicationContextTool;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

/**
 * @Author wangqiyun
 * @Date 2019/2/1 3:22 PM
 */
@WebFilter(urlPatterns = "/*", filterName = "lockFilter", asyncSupported = true)
public class LockFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        filterChain.doFilter(servletRequest, servletResponse);
        ApplicationContextTool.getBean(RedisLock.class).unlock();
    }

    @Override
    public void destroy() {

    }
}
