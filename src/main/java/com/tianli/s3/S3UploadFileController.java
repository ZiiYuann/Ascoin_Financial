package com.tianli.s3;

import com.tianli.common.log.Logs;
import com.tianli.exception.Result;
import com.tianli.tool.MapTool;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;

/**
 * Created by wangqiyun on 2018/7/31.
 */
@Logs("S3上传文件")
@RestController
@RequestMapping("/upload")
public class S3UploadFileController {
    @Resource
    S3UploadFileService s3UploadFileService;
    @Resource
    HttpServletResponse httpServletResponse;

    @RequestMapping("/s3Upload")
    public Result s3Upload(@NotBlank String type) {
        httpServletResponse.addHeader("Access-Control-Allow-Origin", "*");
        return Result.instance().setData(MapTool.Map().put("param", s3UploadFileService.ossUpload(type))
                .put("url", s3UploadFileService.url()));
    }
}
