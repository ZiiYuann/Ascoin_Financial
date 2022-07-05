package com.tianli.common.init;

import com.tianli.common.IpTool;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.tool.ApplicationContextTool;
import com.tianli.user.UserService;
import com.tianli.user.mapper.User;
import com.tianli.user.mapper.UserStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * @Author wangqiyun
 * @Date 2019/2/28 4:27 PM
 */
@Service
public class RequestInitService {


    public LocalDateTime now() {
        return get().getNow();
    }

    public Long now_ms() {
        return get().getNow_ms();
    }

    public Long _uid() {
        return get().getUid();
    }

    public Long uid() {
        Long uid = _uid();
        if (uid == null) ErrorCodeEnum.UNLOIGN.throwException();
        User u;
        if ((u = userService._get(uid)) == null) ErrorCodeEnum.UNLOIGN.throwException();
        if (u.getStatus() != UserStatus.enable) ErrorCodeEnum.ACCOUNT_BAND.throwException();
        return uid;
    }

    public String imei() {
        return get().getImei();
    }

    public String deviceType() {
        return get().getDeviceType();
    }

    public Double lat() {
        return get().getLat();
    }

    public Double lng() {
        return get().getLng();
    }

    public String requestId() {
        return get().getRequestId();
    }

    public String ip() {
        return get().getIp();
    }

    public void init(RequestInit requestInit) {
        REQUEST_INIT.set(requestInit);
    }

    public RequestInit get() {
        RequestInit requestInit = REQUEST_INIT.get();
        if (requestInit == null) return new RequestInit();
        return requestInit;
    }
    public RequestRiskManagementInfo getRisk() {
        RequestRiskManagementInfo info = REQUEST_RISK_INIT.get();
        if (info == null) return new RequestRiskManagementInfo();
        return info;
    }

    public void init(HttpServletRequest httpServletRequest) {
        RequestInit requestInit = new RequestInit();

        String imei = httpServletRequest.getHeader("IMEI");
        if (imei == null) imei = "";
        requestInit.setImei(imei);

        /**
         * 设备信息
         */
        String device_type = httpServletRequest.getHeader("DeviceType");
        if (device_type == null) device_type = "";
        requestInit.setDeviceType(device_type);
        String device_info = httpServletRequest.getHeader("deviceInfo");
        if (device_info == null) device_info = "";
        requestInit.setDeviceInfo(device_info);
        /**
         * 解析用户信息
         */
        requestInit.setUid(requestInitToken.currentUserId(httpServletRequest));
        requestInit.setIp(ApplicationContextTool.getBean(IpTool.class).getIp(httpServletRequest));
        String lat = httpServletRequest.getHeader("LAT");
        if (!StringUtils.isEmpty(lat)) {
            try {
                Double value = Double.valueOf(lat);
                requestInit.setLat(value);
            } catch (Exception e) {
            }
        }


        String lng = httpServletRequest.getHeader("LNG");
        if (!StringUtils.isEmpty(lng)) {
            try {
                Double value = Double.valueOf(lng);
                requestInit.setLng(value);
            } catch (Exception e) {
            }
        }

        REQUEST_INIT.set(requestInit);
    }
    public void initRiskInfo(HttpServletRequest httpServletRequest) {
        RequestRiskManagementInfo riskInfo = new RequestRiskManagementInfo();
        // 增加部分接口的谷歌人机校验逻辑
        requestInitToken.googleCheck(httpServletRequest, riskInfo);
        REQUEST_RISK_INIT.set(riskInfo);
    }

    public void destroy() {
        REQUEST_INIT.remove();
    }

    public void destroyRisk() {
        REQUEST_RISK_INIT.remove();
    }

    public void init() {
        RequestInit requestInit = new RequestInit();
        REQUEST_INIT.set(requestInit);
    }

    public void setUid(long uid) {
        RequestInit requestInit = get();
        requestInit.setUid(uid);
    }

    /**
     * 必须要进行谷歌人机校验
     */
    public void grcOk() {
        RequestRiskManagementInfo risk = getRisk();
        if (!risk.isGrc()) ErrorCodeEnum.SYSTEM_ERROR.throwException();
    }


    @Resource
    private RequestInitToken requestInitToken;

    @Resource
    private UserService userService;

    private final ThreadLocal<RequestInit> REQUEST_INIT = new ThreadLocal<>();
    private final ThreadLocal<RequestRiskManagementInfo> REQUEST_RISK_INIT = new ThreadLocal<>();
}
