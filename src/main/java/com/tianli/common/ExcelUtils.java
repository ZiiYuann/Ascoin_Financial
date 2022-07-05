package com.tianli.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class ExcelUtils {

    public static void export(Workbook workbook, HttpServletResponse response, String fileName) throws IOException {
        OutputStream out = null;
        try {
            fileName = URLEncoder.encode(fileName + ".xls", StandardCharsets.UTF_8);
            response.reset();
            response.setContentType("application/octet-stream;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + fileName);
            response.setHeader("Access-Control-Expose-Headers", "Content-disposition, Content-Type,Cache-control");
            out = response.getOutputStream();
            workbook.write(out);
            out.flush();
        } catch (IOException e) {
            log.error("导出Excel出现严重异常，异常信息：", e);
            e.printStackTrace();
        } finally {
            if (null != out) {
                out.close();
            }
        }
    }
}
