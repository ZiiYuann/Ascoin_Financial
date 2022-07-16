package com.tianli.oss;

import com.tianli.common.log.Logs;
import com.tianli.exception.Result;
import com.tianli.tool.MapTool;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;

@Logs("OSS上传文件")
@RestController
@RequestMapping("/upload")
public class UploadFileController {
    @Resource
    UploadFileService uploadFileService;

    @PostMapping("/success")
    public Result success(String mimeType, String height, String width, String name) {
        return Result.instance().setData(MapTool.Map()
                .put("mimeType", mimeType)
                .put("height", height)
                .put("width", width)
                .put("name", name));
    }

    @RequestMapping("/ossUpload")
    public Result ossUpload(@NotBlank String type) {
        return Result.instance().setData(MapTool.Map().put("param", uploadFileService.ossUpload(type))
                .put("url", uploadFileService.getOssUrl()));
    }
}