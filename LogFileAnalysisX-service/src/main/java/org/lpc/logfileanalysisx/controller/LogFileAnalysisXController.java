package org.lpc.logfileanalysisx.controller;

import com.alibaba.fastjson2.JSON;
import org.lpc.logfileanalysisx.dto.LogFileAnalysisXConfig;
import org.lpc.logfileanalysisx.dto.QueryLogFileAnalysisXConfig;
import org.lpc.logfileanalysisx.dto.SaveLogFileAnalysisXConfigItem;
import org.lpc.logfileanalysisx.service.LogFileAnalysisXService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api")
public class LogFileAnalysisXController {

    @Autowired
    private LogFileAnalysisXService logFileAnalysisXService;

    /**
     * 保存配置
     * @param request
     * @return
     */
    @PostMapping("/save")
    public List<SaveLogFileAnalysisXConfigItem> saveConfig(@RequestBody SaveLogFileAnalysisXConfigItem request) {

        System.out.println("保存配置：" + JSON.toJSONString(request));
        return logFileAnalysisXService.saveConfig(request);
    }

    /**
     * 运行配置
     * @param request
     * @return
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submit(@RequestBody LogFileAnalysisXConfig request) {
        System.out.println("运行配置：" + JSON.toJSONString(request));

        String submit = logFileAnalysisXService.submit(request);
        if ("success".equals(submit)) {
            String filePath = request.getOutputParamsConfig().getOutputFilePath();
            File file = new File(filePath);

            if (!file.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("文件未找到: " + filePath);
            }

            try {
                // 对文件名进行 URL 编码以支持非 ASCII 字符
                String encodedFileName = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8.toString());

                InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                        .body(resource);

            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("文件读取失败: " + e.getMessage());
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("提交失败");
    }

    /**
     * 查询保存的配置
     * @param request
     * @return
     */
    @PostMapping("/queryConfig")
    public List<SaveLogFileAnalysisXConfigItem> queryConfigList(@RequestBody QueryLogFileAnalysisXConfig request) {

        System.out.println("查询配置：" + JSON.toJSONString(request));
        return logFileAnalysisXService.queryConfigList();
    }

    /**
     * 删除保存的配置
     * @param request
     * @return
     */
    @PostMapping("/deleteConfig")
    public List<SaveLogFileAnalysisXConfigItem> deleteConfig(@RequestBody QueryLogFileAnalysisXConfig request) {

        System.out.println("删除配置：" + JSON.toJSONString(request));
        return logFileAnalysisXService.deleteConfig(request.getId());
    }

}
