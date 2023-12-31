package com.tianli.sso.init;

import com.tianli.common.IpTool;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.sso.service.UserOssService;
import com.tianli.tool.ApplicationContextTool;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * @Author wangqiyun
 * @Date 2019/2/28 4:27 PM
 */
@Service
public class RequestInitService {

    public LocalDateTime now() {
        return get().getNow();
    }

    public LocalDateTime yesterday() {
        return LocalDateTime.of(get().getNow().plusDays(-1).toLocalDate(), LocalTime.MIN);
    }

    public Long now_ms() {
        return get().getNow_ms();
    }

    public Long _uid() {
        return get().getUid();
    }

    /**
     * 获取登录的uid
     */
    public Long uid() {
        Long uid = _uid();
        if (Objects.isNull(uid) && StringUtils.isNotBlank(get().getToken())) ErrorCodeEnum.UNLOIGN.throwException();
        if (Objects.isNull(uid) && StringUtils.isBlank(get().getToken()))
            ErrorCodeEnum.UNLOIGN_NO_TOKEN.throwException();
        return uid;
    }

    public String _address() {
        return get().getAddress();
    }

    /**
     * 获取登录的address
     */
    public String address() {
        String address = _address();
        if (address == null || address.trim().equals("")) ErrorCodeEnum.UNLOIGN.throwException();
        return address;
    }

    public SignUserInfo _userInfo() {
        return get().getUserInfo();
    }

    /**
     * 获取登录的userInfo详情
     */
    public SignUserInfo userInfo() {
        SignUserInfo userInfo = _userInfo();
        if (Objects.isNull(userInfo)) ErrorCodeEnum.UNLOIGN.throwException();
        return userInfo;
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
         * 请求链路id
         */
        String requestId = httpServletRequest.getHeader("REQ_ID");
        if (requestId != null) requestInit.setRequestId(requestId);

        /**
         * 设备信息
         */
        String device_type = httpServletRequest.getHeader("DeviceType");
        if (device_type == null) device_type = "";
        requestInit.setDeviceType(device_type);
        String device_info = httpServletRequest.getHeader("deviceInfo");
        if (device_info == null) device_info = "";
        requestInit.setDeviceInfo(device_info);

        String token = httpServletRequest.getHeader("token");

        /**
         * 解析用户信息
         */
        SignUserInfo ossUser = userOssService.loginUser();
        requestInit.setToken(token);
        requestInit.setUid(Objects.isNull(ossUser) ? null : ossUser.getUid());
        requestInit.setAddress(Objects.isNull(ossUser) ? null : ossUser.getSignAddress());
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

    private final ThreadLocal<RequestInit> REQUEST_INIT = new ThreadLocal<>();
}
