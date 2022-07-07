package com.tianli.common.init;

import com.google.gson.JsonObject;
import com.tianli.common.IpTool;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.sso.service.OssService;
import com.tianli.sso.service.UserOssService;
import com.tianli.tool.ApplicationContextTool;
import com.tianli.tool.judge.JsonObjectTool;
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
        JsonObject ossUser = userOssService.loginUser();
        requestInit.setUid(JsonObjectTool.getAsLong(ossUser, "uid"));
        requestInit.setUserInfo(ossUser);
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

    public void destroy() {
        REQUEST_INIT.remove();
    }

    public void init() {
        RequestInit requestInit = new RequestInit();
        REQUEST_INIT.set(requestInit);
    }

    public void setUid(long uid) {
        RequestInit requestInit = get();
        requestInit.setUid(uid);
    }


    @Resource
    private UserOssService userOssService;

    @Resource
    private UserService userService;

    private final ThreadLocal<RequestInit> REQUEST_INIT = new ThreadLocal<>();
}
