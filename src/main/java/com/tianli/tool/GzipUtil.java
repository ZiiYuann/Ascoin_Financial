package com.tianli.tool;

import com.tianli.tool.crypto.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @Author wangqiyun
 * @Date 2018/12/6 11:08 AM
 */
public class GzipUtil {
    public static String compress(String str) {
        if (str == null || str.length() == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(str.getBytes(StandardCharsets.UTF_8));
            gzip.close();
        } catch (IOException e) {
        }
        return Base64.encode(out.toByteArray());
//        return new String(Hex.encode(out.toByteArray()));
    }

    public static String uncompress(String str) {
//        byte[] bytes = Hex.decode(str);
        byte[] bytes = Base64.decodeToByte(str);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        try {
            GZIPInputStream ungzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n;
            while ((n = ungzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
        } catch (IOException e) {
        }
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }
}
