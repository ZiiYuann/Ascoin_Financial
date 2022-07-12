package com.tianli.common.async;

import com.tianli.sso.init.RequestInit;
import com.tianli.sso.init.RequestInitService;
import com.tianli.common.lock.RedisLock;
import com.tianli.common.log.LoggerHandle;
import com.tianli.tool.MapTool;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @Author wangqiyun
 * @Date 2019/2/28 4:18 PM
 */
@Service
public class AsyncService {

    public Future async(Runnable runnable) {
        runnable = handle(runnable);
        return EXECUTOR_SERVICE.submit(runnable);
    }

    public void asyncSuccessRequest(Runnable runnable) {
        runnable = handle(runnable);
        List<Runnable> runnableList = successRun.get();
        if (runnableList == null) {
            runnableList = new ArrayList<>();
            successRun.set(runnableList);
        }
        runnableList.add(runnable);
    }

    public void asyncFailRequest(Runnable runnable) {
        runnable = handle(runnable);
        List<Runnable> runnableList = failRun.get();
        if (runnableList == null) {
            runnableList = new ArrayList<>();
            failRun.set(runnableList);
        }
        runnableList.add(runnable);
    }

    public void cancel() {
        successRun.remove();
        fail.set(true);
    }

    public void clear() {
        successRun.remove();
        failRun.remove();
        fail.remove();
    }

    public void postRun() {
        List<Runnable> runnableList = successRun.get();
        if (runnableList != null) {
            for (Runnable runnable : runnableList) {
                EXECUTOR_SERVICE.submit(runnable);
            }
        }
        Boolean hasFail = fail.get();
        if (hasFail != null && hasFail) {
            runnableList = failRun.get();
            if (runnableList != null) {
                for (Runnable runnable : runnableList) {
                    EXECUTOR_SERVICE.submit(runnable);
                }
            }
        }
        clear();
    }

    private Runnable handle(Runnable runnable) {
        RequestInit requestInit = requestInitService.get();
        if (requestInit == null) requestInit = new RequestInit();
        final RequestInit requestInitTmp = requestInit;
        return () -> {
            requestInitService.init(requestInitTmp);
            asyncService.clear();
            try {
                runnable.run();
            } catch (RuntimeException e) {
                asyncService.cancel();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                PrintWriter writer = new PrintWriter(byteArrayOutputStream);
                e.printStackTrace(writer);
                writer.close();
                loggerHandle.log(MapTool.Map().put("exception", Utf8.decode(byteArrayOutputStream.toByteArray())));
            } finally {
                redisLock.unlock();
                requestInitService.destroy();
                asyncService.postRun();
            }
        };
    }

    @Resource
    private RequestInitService requestInitService;
    @Resource
    private RedisLock redisLock;
    @Resource
    private AsyncService asyncService;
    @Resource
    private LoggerHandle loggerHandle;

    private final ThreadLocal<List<Runnable>> successRun = new ThreadLocal<>();

    private final ThreadLocal<List<Runnable>> failRun = new ThreadLocal<>();
    private final ThreadLocal<Boolean> fail = new ThreadLocal<>();

    private final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
}
