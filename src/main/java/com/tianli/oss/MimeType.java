package com.tianli.oss;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangqiyun on 2018/7/31.
 */
public class MimeType {

    public static final Map<String, String> MIME_TYPE = new HashMap<>();

    static {
        MIME_TYPE.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx");
        MIME_TYPE.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx");
        MIME_TYPE.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx");
        MIME_TYPE.put("application/msword", "doc");
        MIME_TYPE.put("application/vnd.ms-excel", "xls");
        MIME_TYPE.put("application/vnd.ms-powerpoint", "ppt");
        MIME_TYPE.put("application/pdf", "pdf");
        MIME_TYPE.put("video/mp4", "mp4");
        MIME_TYPE.put("video/3gpp", "3gp");
        MIME_TYPE.put("video/mpeg", "mpeg");
        MIME_TYPE.put("video/webm", "webm");
        MIME_TYPE.put("video/x-flv", "flv");
        MIME_TYPE.put("video/x-m4v", "m4v");
        MIME_TYPE.put("video/x-ms-wmv", "wmv");
        MIME_TYPE.put("video/x-msvideo", "avi");
        MIME_TYPE.put("audio/mp3", "mp3");
        MIME_TYPE.put("audio/midi", "mid");
        MIME_TYPE.put("audio/ogg", "ogg");
        MIME_TYPE.put("audio/x-m4a", "m4a");
        MIME_TYPE.put("audio/m4a", "m4a");
        MIME_TYPE.put("audio/mpeg", "mp3");
        MIME_TYPE.put("audio/x-realaudio", "ra");
        MIME_TYPE.put("text/html", "html");
        MIME_TYPE.put("text/plain", "txt");
        MIME_TYPE.put("application/rtf", "rtf");
        MIME_TYPE.put("application/x-7z-compressed", "7z");
        MIME_TYPE.put("application/x-rar-compressed", "rar");
        MIME_TYPE.put("application/zip", "zip");
        MIME_TYPE.put("application/x-zip-compressed", "zip");
        MIME_TYPE.put("application/x-gzip", "gz");

        MIME_TYPE.put("image/jpeg", "jpg");
        MIME_TYPE.put("image/jpg", "jpg");
        MIME_TYPE.put("image/png", "png");
        MIME_TYPE.put("image/gif", "gif");
        MIME_TYPE.put("image/tiff", "tif");
        MIME_TYPE.put("image/vnd.wap.wbmp", "wbmp");
        MIME_TYPE.put("image/x-icon", "ico");
        MIME_TYPE.put("image/x-jng", "jng");
        MIME_TYPE.put("image/x-ms-bmp", "bmp");
        MIME_TYPE.put("image/svg+xml", "svg");
        MIME_TYPE.put("image/webp", "webp");
    }
}
