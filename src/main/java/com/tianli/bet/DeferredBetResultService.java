package com.tianli.bet;

import com.tianli.exception.Result;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class DeferredBetResultService {
    private static ConcurrentHashMap resultMap = new ConcurrentHashMap();

    public void doPush(DeferredResult<Result> result, long uid){
        // 获取数据库结果 有结果直接返回 / 没结果则等待

    }

    public void doNotice(long uid){

    }

}
