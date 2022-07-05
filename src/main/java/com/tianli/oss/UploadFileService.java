package com.tianli.oss;

import com.google.gson.Gson;
import com.tianli.common.init.RequestInitService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.MapTool;
import com.tianli.tool.crypto.Base64;
import com.tianli.tool.crypto.Crypto;
import com.tianli.tool.crypto.UrlEncode;
import org.bouncycastle.crypto.util.DigestFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
public class UploadFileService {


    public Map ossUpload(String type) {
        if (type == null || !MimeType.MIME_TYPE.containsKey(type)) ErrorCodeEnum.TYPE_ERROR.throwException();
        String file_name = "file/" + UUID.randomUUID().toString() + "." + MimeType.MIME_TYPE.get(type);
        String callback = Base64.encode(new Gson().toJson(MapTool.Map().put("callbackUrl", getCallbackUrl())
                .put("callbackBody", "mimeType=${mimeType}&height=${imageInfo.height}&width=${imageInfo.width}&name=" + UrlEncode.encode(file_name))));
        String policy = getPolicy(file_name, callback, type);
        return MapTool.Map().put("policy", policy).put("key", file_name).put("OSSAccessKeyId", getOSSAccessKeyId())
                .put("Signature", Base64.encode(Crypto.hmac(DigestFactory.createSHA1(), getOSSAccessKeySecret(), policy)))
                .put("callback", callback).put("Content-Type", type);
    }

    private String getPolicy(String key, String callback, String type) {
        List<Object> conditions = new ArrayList<>(), content_length_range = new ArrayList<>();
        content_length_range.add("content-length-range");
        content_length_range.add(0);
        content_length_range.add(1048576000);
        conditions.add(MapTool.Map().put("key", key));
        conditions.add(content_length_range);
        conditions.add(MapTool.Map().put("Content-Type", type));
        conditions.add(MapTool.Map().put("callback", callback));
        Map put = MapTool.Map().put("expiration", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .format(new Date(System.currentTimeMillis() + 120000L))).put("conditions", conditions);
        String s = new Gson().toJson(put);
        return Base64.encode(s);
    }

    public String getOssUrl() {
        return configService.get("oss_url");
    }


    public String getOSSAccessKeyId() {
        return configService.get("oss_access_key_id");
    }

    public String getOSSAccessKeySecret() {
        return configService.get("oss_access_key_secret");
    }

    public String getCallbackUrl() {
        return configService.get("url") + "/upload/success";
    }


    @Resource
    private ConfigService configService;
    @Resource
    private RequestInitService requestInitService;
}
