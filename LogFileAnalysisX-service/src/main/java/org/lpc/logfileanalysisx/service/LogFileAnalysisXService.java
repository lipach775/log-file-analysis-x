package org.lpc.logfileanalysisx.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import org.lpc.logfileanalysisx.dto.*;
import org.lpc.logfileanalysisx.utils.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.lpc.logfileanalysisx.utils.LogFileAnalysisX.dealFileFromInput;
import static org.lpc.logfileanalysisx.utils.LogFileAnalysisX.tableColumnDealMethodFill;

@Service
public class LogFileAnalysisXService {

    @Value("${config.save.path}")
    private String configSavePath;

    /**
     * 运行
     * @param request
     * @return
     */
    public String submit(LogFileAnalysisXConfig request) {

        // 类内容公共解析方法转换为代码
        Map<String, Function<ThreadContext, String>> tableColumnDealMethod = new HashMap<>();
        tableColumnDealMethodFill(tableColumnDealMethod, request.getOutputParamsConfig());
        request.getOutputParamsConfig().setTableColumnDealMethod(tableColumnDealMethod);

        dealFileFromInput(request);

        return "success";
    }

    /**
     * 简易持久化
     * @param request
     * @return
     */
    public List<SaveLogFileAnalysisXConfigItem> saveConfig(SaveLogFileAnalysisXConfigItem request) {

        List<SaveLogFileAnalysisXConfigItem> result = queryConfigList();
        result.add(request);

        FileUtils.writeStringToFile(configSavePath, JSON.toJSONString(result));

        return result;
    }

    /**
     * 简易查询保存的配置
     * @return
     */
    public List<SaveLogFileAnalysisXConfigItem> queryConfigList() {

        List<SaveLogFileAnalysisXConfigItem> result = new ArrayList<>();
        String s = FileUtils.readFileToString(configSavePath);
        if (!StringUtils.isEmpty(s)) {
            result = JSONArray.parseArray(s, SaveLogFileAnalysisXConfigItem.class);
        }

        return result;
    }

    /**
     * 简易删除保存的配置
     * @return
     */
    public List<SaveLogFileAnalysisXConfigItem> deleteConfig(String id) {

        List<SaveLogFileAnalysisXConfigItem> oldResult = new ArrayList<>();
        String s = FileUtils.readFileToString(configSavePath);
        if (!StringUtils.isEmpty(s)) {
            oldResult = JSONArray.parseArray(s, SaveLogFileAnalysisXConfigItem.class);
        }

        List<SaveLogFileAnalysisXConfigItem> result = oldResult.stream().filter(a -> !id.equals(a.getId())).collect(Collectors.toList());
        FileUtils.writeStringToFile(configSavePath, JSON.toJSONString(result));

        return result;
    }

}
