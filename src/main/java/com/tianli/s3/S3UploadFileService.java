package com.tianli.s3;

import com.google.gson.Gson;
import com.tianli.common.Constants;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.MapTool;
import com.tianli.tool.crypto.Base64;
import com.tianli.tool.crypto.Crypto;
import org.bouncycastle.crypto.util.DigestFactory;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

/**
 * Created by wangqiyun on 2018/7/31.
 */
@Service
public class S3UploadFileService {


    public Map ossUpload(String type) {
        if (type == null || !MimeType.MIME_TYPE.containsKey(type)) ErrorCodeEnum.TYPE_ERROR.throwException();
        String file_name = "file/" + UUID.randomUUID().toString() + "." + MimeType.MIME_TYPE.get(type);
        long now = System.currentTimeMillis();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("+00:00"));
        String date = Constants.dateFormatter.format(Instant.ofEpochMilli(now).atZone(ZoneId.of("+00:00")));
        String policy = Base64.encode(getPolicy(file_name, type, now));
        String signature = new String(Hex.encode(HmacSHA256(policy,
                getSignatureKey(AWSSecretAccessKey(), date, region(), "s3"))));
        return MapTool.Map().put("Policy", policy).put("key", file_name).put("Content-Type", type)
                .put("x-amz-credential", AWSAccessKeyId() + "/" + date + "/" + region() + "/s3/aws4_request")
                .put("x-amz-algorithm", "AWS4-HMAC-SHA256")
                .put("x-amz-date", simpleDateFormat.format(new Date(now)))
                .put("x-amz-signature", signature);

    }

    private String getPolicy(String key, String type, long now) {
        List<Object> conditions = new ArrayList<>(), content_length_range = new ArrayList<>();
        String date = Constants.dateFormatter.format(Instant.ofEpochMilli(now).atZone(ZoneId.of("+00:00")));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("+00:00"));
        SimpleDateFormat dateSimpleDateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        dateSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("+00:00"));
        content_length_range.add("content-length-range");
        content_length_range.add(0);
        content_length_range.add(1048576000);
        conditions.add(MapTool.Map().put("key", key));
        conditions.add(content_length_range);
        conditions.add(MapTool.Map().put("Content-Type", type));
        conditions.add(MapTool.Map().put("bucket", bucketName()));
        conditions.add(MapTool.Map().put("x-amz-algorithm", "AWS4-HMAC-SHA256"));
        conditions.add(MapTool.Map().put("x-amz-credential", AWSAccessKeyId() + "/" + date + "/" + region() + "/s3/aws4_request"));
        conditions.add(MapTool.Map().put("x-amz-date", dateSimpleDateFormat.format(new Date(now))));
        Map put = MapTool.Map().put("expiration", simpleDateFormat.format(new Date(now + 12000000L))).put("conditions", conditions);
        return new Gson().toJson(put);
    }

    public String region() {
        return configService.get("s3_region");
    }

    public String url() {
        return configService.get("s3_url");
    }

    private String bucketName() {
        return configService.get("s3_bucketName");
    }

    private String AWSAccessKeyId() {
        return configService.get("s3_AWSAccessKeyId");
    }

    private String AWSSecretAccessKey() {
        return configService.getAndDecrypt("s3_AWSSecretAccessKey");
    }

    static byte[] HmacSHA256(String data, byte[] key) {
        return Crypto.hmac(DigestFactory.createSHA256(), key, data);
    }

    static byte[] getSignatureKey(String key, String dateStamp, String regionName, String serviceName) {
        byte[] kSecret = Utf8.encode("AWS4" + key);
        byte[] kDate = HmacSHA256(dateStamp, kSecret);
        byte[] kRegion = HmacSHA256(regionName, kDate);
        byte[] kService = HmacSHA256(serviceName, kRegion);
        return HmacSHA256("aws4_request", kService);
    }

    @Resource
    private ConfigService configService;
}
