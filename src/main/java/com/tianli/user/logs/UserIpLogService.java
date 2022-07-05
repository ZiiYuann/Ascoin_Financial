package com.tianli.user.logs;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.Constants;
import com.tianli.common.init.RequestInitService;
import com.tianli.common.init.RequestRiskManagementInfo;
import com.tianli.role.annotation.GrcCheckModular;
import com.tianli.user.UserService;
import com.tianli.user.logs.mapper.UserIpLog;
import com.tianli.user.logs.mapper.UserIpLogMapper;
import com.tianli.user.mapper.User;
import com.tianli.user.userinfo.UserInfoService;
import com.tianli.user.userinfo.mapper.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class UserIpLogService extends ServiceImpl<UserIpLogMapper, UserIpLog> {

    @Transactional(propagation= Propagation.REQUIRES_NEW)
    public void addWithNewT(GrcCheckModular modular) {
        add(modular);
    }

    public UserIpLog add(GrcCheckModular modular) {
        UserIpLog saveOne = UserIpLog.save(modular, requestInitService, this);
        List<RequestRiskManagementInfo.IpLogsContext> ipLogsContext = requestInitService.getRisk().getIpLogsContext();
        ipLogsContext.add(RequestRiskManagementInfo.IpLogsContext.builder()
                .modular(modular)
                .logId(saveOne.getId())
                .build());
        return saveOne;
    }

    private static final ThreadPoolExecutor USER_LOG_ASYNC_EXECUTOR = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(512), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("USER_LOG_ASYNC_EXECUTOR"+System.currentTimeMillis());
            return thread;
        }
    }, new ThreadPoolExecutor.CallerRunsPolicy());

    public void updateBehaviorId(GrcCheckModular modular, Long bid) {
        RequestRiskManagementInfo risk = requestInitService.getRisk();
        Long uid = requestInitService._uid();
        USER_LOG_ASYNC_EXECUTOR.execute(() -> updateBehaviorIdSync(modular, bid, risk, uid));
    }

    public Boolean isCommonIp(Long uid, String ip) {
        int count = this.count(Wrappers.lambdaQuery(UserIpLog.class).eq(UserIpLog::getUid, uid));
        if (count == 0) {
            return Boolean.TRUE;
        }
        return this.count(Wrappers.lambdaQuery(UserIpLog.class)
                .eq(UserIpLog::getUid, uid)
                .eq(UserIpLog::getIp,ip)) > 0;
    }

    public void updateBehaviorIdSync(GrcCheckModular modular, Long bid, RequestRiskManagementInfo risk, Long uid) {
        if(Objects.isNull(bid) || Objects.isNull(modular)){
            return;
        }
        String username = risk.getUsername();
        String nick = risk.getNick();
        if(Objects.isNull(uid)){
            // 登录请求
            User user = userService._getByUsername(username);
            int retry = 0;
            while (Objects.isNull(user) && retry < 3){
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException ignored) {
                }
                user = userService._getByUsername(username);
                retry ++;
            }
            if(Objects.nonNull(user)){
                uid = user.getId();
                username = user.getUsername();
                UserInfo userInfo = userInfoService.getOrSaveById(user.getId());
                nick = (Objects.isNull(userInfo) ? Constants.defaultUserNick : userInfo.getNick());
            }
        }
        List<RequestRiskManagementInfo.IpLogsContext> ipLogsContext = risk.getIpLogsContext();
        if(CollectionUtils.isEmpty(ipLogsContext)){
            return;
        }
        Long id = null;
        for (RequestRiskManagementInfo.IpLogsContext c : ipLogsContext){
            if(Objects.equals(c.getModular(), modular)){
                id = c.getLogId();
                break;
            }
        }
        if(Objects.isNull(id)){
            return;
        }
        update(Wrappers.lambdaUpdate(UserIpLog.class)
                .set(UserIpLog::getBehavior_id, bid)
                .set(StringUtils.isNotBlank(username), UserIpLog::getUsername, username)
                .set(StringUtils.isNotBlank(nick), UserIpLog::getNick, nick)
                .set(Objects.nonNull(uid), UserIpLog::getUid, uid)
                .set(UserIpLog::getUpdate_time, LocalDateTime.now())
                .eq(UserIpLog::getId, id));
        System.out.println(String.format("执行了ipLog更新!!! uid:%s, username:%s, nick:%s", uid, username, nick));
    }

    @Resource
    private RequestInitService requestInitService;

    @Resource
    private UserService userService;

    @Resource
    private UserInfoService userInfoService;
}
