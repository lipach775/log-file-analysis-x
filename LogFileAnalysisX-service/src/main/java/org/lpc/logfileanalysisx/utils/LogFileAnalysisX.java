package org.lpc.logfileanalysisx.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import org.lpc.logfileanalysisx.dto.InputParamsConfig;
import org.lpc.logfileanalysisx.dto.LogFileAnalysisXConfig;
import org.lpc.logfileanalysisx.dto.OutputParamsConfig;
import org.lpc.logfileanalysisx.dto.ThreadContext;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日志文件分析，转表格
 *
 * @author lipancheng
 * @date 2025-01-20 16:10:12
 */
public class LogFileAnalysisX {

    /**
     * todo 兼容多线程上下文：修改为rhreadlocal
     * threadContext.inputFilecontent （ArraryList<string>）：读取到的日志文件
     * threadContext.readLineIndex （Integer）；当前读取到的日志文件行号下标
     *  threadContext.resultLineIndex （Integer）：处理结果写入行下标
     * threadContext.result （ArraryList<Map<String,String>>）：处理结果
     * threadContext.inputParamsConfig（HashMap）：读取日志文件配置
     * threadContext.outputParamsConfig（HashMap）；写人结果文件配置
     */
    public static ThreadContext threadContext = new ThreadContext();

    public static void dealFileFromInput(LogFileAnalysisXConfig config) {
        try {
            InputParamsConfig inputParamsConfig = config.getInputParamsConfig();
            OutputParamsConfig outputParamsConfig = config.getOutputParamsConfig();
            threadContext.setResult(new ArrayList<>());
            threadContext.setResultLineIndex(0);
            threadContext.setInputParamsConfig(inputParamsConfig);
            threadContext.setOutputParamsConfig(outputParamsConfig);

            List<String> inputFilePaths = inputParamsConfig.getInputFilePaths();
            List<String> inputFileRegexMatcherAnd = inputParamsConfig.getInputFileRegexMatcherAnd();
            List<String> inputFileRegexMatcherOr = inputParamsConfig.getInputFileRegexMatcherOr();

            String outputFilePath = outputParamsConfig.getOutputFilePath();

            List<String> inputFileContent = new ArrayList<>();
            for (int i = 0; i < inputFilePaths.size(); i++) {
                List<String> lines = readBaseFromFile(inputFilePaths.get(i), inputFileRegexMatcherAnd, inputFileRegexMatcherOr);
                inputFileContent.addAll(lines);
            }
            threadContext.setInputFileContent(inputFileContent);

            for (int i = 0; i < inputFileContent.size(); i++) {
                threadContext.setInputLineIndex(i);

                String line = inputFileContent.get(i);
                // 行匹配规则
                boolean matcherLineResult = lineRegexMatchAnd(line, outputParamsConfig.getLineRegexMatcherAnd());

                if (matcherLineResult) {
                    Map<String, Function<ThreadContext, String>> tableColumnDealMethod = outputParamsConfig.getTableColumnDealMethod();
                    for (int j = 0; j < tableColumnDealMethod.size(); j++) {
                        Function<ThreadContext, String> function = tableColumnDealMethod.get(j + "");
                        String res = function.apply(threadContext);
                    }
                    threadContext.setResultLineIndex(threadContext.getResultLineIndex() + 1);
                }
            }

            // todo 可自定义其他数据格式转换方法，例如：使用easyExcel生产excel
            if (outputFilePath.endsWith(".xlsx")) {
                formatResultToExcel(outputFilePath);
            } else {
                formatResultToText(outputFilePath);
            }

            System.out.println("文件保存成功：" + outputFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void tableColumnDealMethodFill(Map<String, Function<ThreadContext, String>> tableColumnDealMethod, OutputParamsConfig outputParamsConfig) {
        Map<String, String> tableColumnDealMethodConfig = outputParamsConfig.getTableColumnDealMethodConfig();

        tableColumnDealMethodConfig.forEach((finalIcc, value) -> {
            String methodName = value;
            if ("获取日志行内容".equals(methodName)) {
                tableColumnDealMethod.put(finalIcc + "", (threadContext) -> {
                    List<String> inputFileContent = threadContext.getInputFileContent();
                    int i = threadContext.getResultLineIndex();
                    String line = inputFileContent.get(i);
                    List<Map<String, String>> result = threadContext.getResult();
                    int resultLineIndex = threadContext.getResultLineIndex();
                    if (result.size() <= resultLineIndex) {
                        result.add(new HashMap<>());
                    }
                    Map<String, String> thisResultLine = result.get(resultLineIndex);

                    // 获取日志行内容
                    String lineLogResult = line;

                    lineLogResult = dealResult(methodName, thisResultLine, finalIcc, lineLogResult);
                    return lineLogResult;
                });
            } else if (methodName.startsWith("计算两个时间之间的差值(ms)")) {
                tableColumnDealMethod.put(finalIcc + "", (threadContext) -> {
                    List<String> inputFileContent = threadContext.getInputFileContent();
                    int i = threadContext.getResultLineIndex();
                    String line = inputFileContent.get(i);
                    List<Map<String, String>> result = threadContext.getResult();
                    int resultLineIndex = threadContext.getResultLineIndex();
                    if (result.size() <= resultLineIndex) {
                        result.add(new HashMap<>());
                    }
                    Map<String, String> thisResultLine = result.get(resultLineIndex);

                    // 计算两个时间之间的差值(ms)
                    //   :Date1[${0}]/:Date1
                    //   :Date2[${0}]/:Date2
                    //   :Format[yyyy-MM-dd HH:mm:ss:SSS]/:Format
                    String firstDateColumnIndex = getValueByTag(methodName, ":Date1");
                    String secondDateColumnIndex = getValueByTag(methodName, ":Date2");
                    String format = getValueByTag(methodName, ":Format");

                    String reslutStr = "";
                    String date1 = getAndFillDaole(firstDateColumnIndex, thisResultLine);
                    String date2 = getAndFillDaole(secondDateColumnIndex, thisResultLine);
                    if (!StringUtils.isEmpty(date1) && !StringUtils.isEmpty(date2)) {
                        reslutStr = DateUtils.getDateDiff(date1, date2, format).toString();
                    }

                    reslutStr = dealResult(methodName, thisResultLine, finalIcc, reslutStr);
                    return reslutStr;
                });
            } else if ("获取线程名".equals(methodName)) {
                tableColumnDealMethod.put(finalIcc + "", (threadContext) -> {
                    List<String> inputFileContent = threadContext.getInputFileContent();
                    int i = threadContext.getResultLineIndex();
                    String line = inputFileContent.get(i);
                    List<Map<String, String>> result = threadContext.getResult();
                    int resultLineIndex = threadContext.getResultLineIndex();
                    if (result.size() <= resultLineIndex) {
                        result.add(new HashMap<>());
                    }
                    Map<String, String> thisResultLine = result.get(resultLineIndex);

                    // 获取日志行线程名称
                    String reslutStr = line.substring(line.indexOf("[") + 1, line.indexOf("]"));

                    reslutStr = dealResult(methodName, thisResultLine, finalIcc, reslutStr);
                    return reslutStr;
                });
            } else if (methodName.startsWith("获取当前行之后，下一个满足条件的日志行")) {
                tableColumnDealMethod.put(finalIcc + "", (threadContext) -> {
                    List<String> inputFileContent = threadContext.getInputFileContent();
                    int i = threadContext.getResultLineIndex();
                    String line = inputFileContent.get(i);
                    List<Map<String, String>> result = threadContext.getResult();
                    int resultLineIndex = threadContext.getResultLineIndex();
                    if (result.size() <= resultLineIndex) {
                        result.add(new HashMap<>());
                    }
                    Map<String, String> thisResultLine = result.get(resultLineIndex);

                    // 获取当前行之后，下一个满足条件的日志行
                    //   :And[${0},output]/:And
                    //   :Or[]/:Or
                    List<String> andConditionList = new ArrayList<>();
                    String andConditionStr = getValueByTag(methodName, ":And");
                    if (!StringUtils.isEmpty(andConditionStr)) {
                        andConditionList.addAll(Arrays.asList(andConditionStr.split(",")));
                        if (!CollectionUtils.isEmpty(andConditionList)) {
                            for (String condition : andConditionList) {
                                condition = getAndFillDaole(condition, thisResultLine);
                            }
                        }
                    }

                    List<String> orConditionList = new ArrayList<>();
                    String orConditionStr = getValueByTag(methodName, ":Or");
                    if (!StringUtils.isEmpty(orConditionStr)) {
                        orConditionList.addAll(Arrays.asList(orConditionStr.split(",")));
                        if (!CollectionUtils.isEmpty(orConditionList)) {
                            for (String condition : orConditionList) {
                                condition = getAndFillDaole(condition, thisResultLine);
                            }
                        }
                    }

                    String reslutStr = "";
                    String resultLine = getNextResultLineAndOr(i, inputFileContent, andConditionList, orConditionList);
                    if (!StringUtils.isEmpty(resultLine)) {
                        reslutStr = resultLine;
                    }

                    reslutStr = dealResult(methodName, thisResultLine, finalIcc, reslutStr);
                    return reslutStr;
                });
            } else if (methodName.startsWith("获取当前行之前，上一个满足条件的日志行")) {
                tableColumnDealMethod.put(finalIcc + "", (threadContext) -> {
                    List<String> inputFileContent = threadContext.getInputFileContent();
                    int i = threadContext.getResultLineIndex();
                    String line = inputFileContent.get(i);
                    List<Map<String, String>> result = threadContext.getResult();
                    int resultLineIndex = threadContext.getResultLineIndex();
                    if (result.size() <= resultLineIndex) {
                        result.add(new HashMap<>());
                    }
                    Map<String, String> thisResultLine = result.get(resultLineIndex);

                    // 获取当前行之前，上一个满足条件的日志行
                    //   :And[${0},output]/:And
                    //   :Or[]/:Or
                    List<String> andConditionList = new ArrayList<>();
                    String andConditionStr = getValueByTag(methodName, ":And");
                    if (!StringUtils.isEmpty(andConditionStr)) {
                        andConditionList.addAll(Arrays.asList(andConditionStr.split(",")));
                        if (!CollectionUtils.isEmpty(andConditionList)) {
                            for (String condition : andConditionList) {
                                condition = getAndFillDaole(condition, thisResultLine);
                            }
                        }
                    }

                    List<String> orConditionList = new ArrayList<>();
                    String orConditionStr = getValueByTag(methodName, ":Or");
                    if (!StringUtils.isEmpty(orConditionStr)) {
                        orConditionList.addAll(Arrays.asList(orConditionStr.split(",")));
                        if (!CollectionUtils.isEmpty(orConditionList)) {
                            for (String condition : orConditionList) {
                                condition = getAndFillDaole(condition, thisResultLine);
                            }
                        }
                    }

                    String reslutStr = "";
                    String resultLine = getBeforeResultLineAndOr(i, inputFileContent, andConditionList, orConditionList);
                    if (!StringUtils.isEmpty(resultLine)) {
                        reslutStr = resultLine;
                    }

                    reslutStr = dealResult(methodName, thisResultLine, finalIcc, reslutStr);
                    return reslutStr;
                });
            } else if (methodName.startsWith("按字符串开始结束截取字符串")) {
                tableColumnDealMethod.put(finalIcc + "", (threadContext) -> {
                    List<String> inputFileContent = threadContext.getInputFileContent();
                    int i = threadContext.getResultLineIndex();
                    String line = inputFileContent.get(i);
                    List<Map<String, String>> result = threadContext.getResult();
                    int resultLineIndex = threadContext.getResultLineIndex();
                    if (result.size() <= resultLineIndex) {
                        result.add(new HashMap<>());
                    }
                    Map<String, String> thisResultLine = result.get(resultLineIndex);

                    // 按字符串开始结束截取字符串
                    //   :Input[${0}]/:Input
                    //   :StartWithString[]/:StartWithString
                    //   :EndWithString[]/:EndWithString
                    String inputValue = getValueByTag(methodName, ":Input");
                    String inputValueFinal = getAndFillDaole(inputValue, thisResultLine);

                    String startWithStringValue = getValueByTag(methodName, ":StartWithString");
                    String endWithStringValue = getValueByTag(methodName, ":EndWithString");

                    String reslutStr = "";
                    int startIndex = 0;
                    int endIndex = inputValueFinal.length();
                    if (!StringUtils.isEmpty(startWithStringValue)) {
                        startIndex = inputValueFinal.indexOf(startWithStringValue) + startWithStringValue.length();
                    }
                    if (!StringUtils.isEmpty(endWithStringValue)) {
                        endIndex = inputValueFinal.indexOf(endWithStringValue);
                    }
                    if (startIndex > -1 && endIndex > -1) {
                        if (!StringUtils.isEmpty(inputValueFinal)) {
                            reslutStr = inputValueFinal.substring(startIndex, endIndex);
                        }
                    }

                    reslutStr = dealResult(methodName, thisResultLine, finalIcc, reslutStr);
                    return reslutStr;
                });
            } else if (methodName.startsWith("按下标开始结束截取字符串")) {
                tableColumnDealMethod.put(finalIcc + "", (threadContext) -> {
                    List<String> inputFileContent = threadContext.getInputFileContent();
                    int i = threadContext.getResultLineIndex();
                    String line = inputFileContent.get(i);
                    List<Map<String, String>> result = threadContext.getResult();
                    int resultLineIndex = threadContext.getResultLineIndex();
                    if (result.size() <= resultLineIndex) {
                        result.add(new HashMap<>());
                    }
                    Map<String, String> thisResultLine = result.get(resultLineIndex);

                    // 按下标开始结束截取字符串
                    //   :Input[${0}]/:Input
                    //   :StartWithIndex[]/:StartWithIndex
                    //   :EndWithIndex[]/:EndWithIndex
                    String inputValue = getValueByTag(methodName, ":Input");
                    String inputValueFinal = getAndFillDaole(inputValue, thisResultLine);

                    String startWithIndexValue = getValueByTag(methodName, ":StartWithIndex");
                    String endWithIndexValue = getValueByTag(methodName, ":EndWithIndex");

                    String reslutStr = "";
                    int startIndex = 0;
                    int endIndex = inputValueFinal.length();
                    if (!StringUtils.isEmpty(startWithIndexValue)) {
                        startIndex = Integer.parseInt(startWithIndexValue);
                    }
                    if (!StringUtils.isEmpty(endWithIndexValue)) {
                        endIndex = Integer.parseInt(endWithIndexValue);
                    }
                    if (startIndex > -1 && endIndex > -1) {
                        if (!StringUtils.isEmpty(inputValueFinal)) {
                            reslutStr = inputValueFinal.substring(startIndex, endIndex);
                        }
                    }

                    reslutStr = dealResult(methodName, thisResultLine, finalIcc, reslutStr);
                    return reslutStr;
                });
            } else if (methodName.startsWith("JSONPath提取内容")) {
                tableColumnDealMethod.put(finalIcc + "", (threadContext) -> {
                    List<String> inputFileContent = threadContext.getInputFileContent();
                    int i = threadContext.getResultLineIndex();
                    String line = inputFileContent.get(i);
                    List<Map<String, String>> result = threadContext.getResult();
                    int resultLineIndex = threadContext.getResultLineIndex();
                    if (result.size() <= resultLineIndex) {
                        result.add(new HashMap<>());
                    }
                    Map<String, String> thisResultLine = result.get(resultLineIndex);

                    // JSONPath提取内容
                    //   :Input[${0}]/:Input
                    //   :JSONPath[]/:JSONPath
                    String inputValue = getValueByTag(methodName, ":Input");
                    String inputValueFinal = getAndFillDaole(inputValue, thisResultLine);

                    String reslutStr = "";
                    String jsonPath = getValueByTag(methodName, ":JSONPath");
                    try {
                        Object read = JSONPath.of(jsonPath).extract(JSONReader.of(inputValueFinal));
                        reslutStr = read.toString();
                    } catch (Exception e) {
                        System.out.println("jsonPath提取内容出错，跳过：inputValueFinal=" + inputValueFinal + ", jsonPath=" + jsonPath);
                    }

                    reslutStr = dealResult(methodName, thisResultLine, finalIcc, reslutStr);
                    return reslutStr;
                });
            } else if (methodName.startsWith("正则提取内容")) {
                tableColumnDealMethod.put(finalIcc + "", (threadContext) -> {
                    List<String> inputFileContent = threadContext.getInputFileContent();
                    int i = threadContext.getResultLineIndex();
                    String line = inputFileContent.get(i);
                    List<Map<String, String>> result = threadContext.getResult();
                    int resultLineIndex = threadContext.getResultLineIndex();
                    if (result.size() <= resultLineIndex) {
                        result.add(new HashMap<>());
                    }
                    Map<String, String> thisResultLine = result.get(resultLineIndex);

                    // 正则提取内容
                    //   :Input[${0}]/:Input
                    //   :Regex[]/:Regex
                    String inputValue = getValueByTag(methodName, ":Input");
                    String inputValueFinal = getAndFillDaole(inputValue, thisResultLine);

                    String reslutStr = "";
                    String regex = getValueByTag(methodName, ":Regex");
                    try {
                        Pattern pattern = Pattern.compile(regex);
                        Matcher matcher = pattern.matcher(inputValueFinal);
                        if (matcher.find()) {
                            // 此处如果匹配到多个只取第一个，如果全取修改if为while
                            reslutStr = matcher.group();
                        }
                    } catch (Exception e) {
                        System.out.println("正则提取内容出错，跳过：inputValueFinal=" + inputValueFinal + ", regex=" + regex);
                    }

                    reslutStr = dealResult(methodName, thisResultLine, finalIcc, reslutStr);
                    return reslutStr;
                });
            }
        });
    }

    public static String dealResult(String methodName, Map<String, String> thisResultLine, String finalIcc, String reslutStr) {
        reslutStr = doCommonTag(reslutStr, methodName);
        thisResultLine.put(finalIcc + "", reslutStr);
        return reslutStr;
    }

    // 处理公共Tag
    public static String doCommonTag(String reslutStr, String methodName) {
        if (StringUtils.isEmpty(reslutStr)) {
            return reslutStr;
        }
        // 移除换行符、制表符
        String removeNT = getValueByTag(methodName, ":RemoveNT");
        if ("true".equals(removeNT)) {
            reslutStr = reslutStr.replaceAll("\n", " ")
                    .replaceAll("\t", "");
        }

        return reslutStr;
    }

    public static String getValueByTag(String strContainTag, String tagStart) {
        String result = "";
        int startIndex = strContainTag.indexOf(tagStart);
        int endIndex = strContainTag.indexOf("/" + tagStart);
        if (startIndex > -1 && endIndex > -1) {
            result = strContainTag.substring(startIndex + tagStart.length() + 1, endIndex - 1);
        }
        return result;
    }

    // 处理${n}引用
    public static String getAndFillDaole(String str, Map<String, String> thisResultLine) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        if (str.startsWith("${") && str.endsWith("}")) {
            String result = thisResultLine.get(str.substring(2, str.length() - 1));
            return result;
        }
        return str;
    }

    public static void formatResultToText(String filePath) {
        OutputParamsConfig outputParamsConfig = threadContext.getOutputParamsConfig();
        Map<String, String> tableHeaderMap = outputParamsConfig.getTableHeader();
        List<String> resultShowColumnAndOrder = Optional.ofNullable(outputParamsConfig.getResultShowColumnAndOrder()).orElse(new ArrayList<>());

        if (CollectionUtils.isEmpty(resultShowColumnAndOrder)) {
            tableHeaderMap.forEach((k, v) -> resultShowColumnAndOrder.add(k));
        }

        String tableHeader = "";
        for (int i = 0; i < resultShowColumnAndOrder.size(); i++) {
            String orderKey = resultShowColumnAndOrder.get(i);
            tableHeader = tableHeader + tableHeaderMap.get(orderKey);
            if (i < resultShowColumnAndOrder.size() - 1) {
                tableHeader = tableHeader + "\t";
            }
        }
        System.out.println(tableHeader);

        StringBuilder sb = new StringBuilder();
        // 组装表头
        sb.append(tableHeader).append("\n");
        // 组装表内容
        List<Map<String, String>> finalResult = threadContext.getResult();
        for (Map<String, String> resultItem : finalResult) {
            for (int j = 0; j < resultShowColumnAndOrder.size(); j++) {
                String orderKey = resultShowColumnAndOrder.get(j);
                sb.append(resultItem.get(orderKey));
                if (j < resultShowColumnAndOrder.size() - 1) {
                    sb.append("\t");
                }
            }
            sb.append("\n");
        }

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void formatResultToExcel(String filePath) {
        OutputParamsConfig outputParamsConfig = threadContext.getOutputParamsConfig();
        Map<String, String> tableHeaderMap = outputParamsConfig.getTableHeader();
        List<String> resultShowColumnAndOrder = Optional.ofNullable(outputParamsConfig.getResultShowColumnAndOrder()).orElse(new ArrayList<>());

        // 如果未指定显示列及顺序，默认按照表头顺序
        if (resultShowColumnAndOrder.isEmpty()) {
            tableHeaderMap.forEach((k, v) -> resultShowColumnAndOrder.add(k));
        }

        // 动态构建表头
        List<List<String>> tableHeaders = createDynamicHead(resultShowColumnAndOrder, tableHeaderMap);

        // 准备数据
        List<Map<String, String>> finalResult = threadContext.getResult();
        List<List<Object>> rowDataList = new ArrayList<>();
        for (Map<String, String> resultItem : finalResult) {
            List<Object> rowData = new ArrayList<>();
            for (String key : resultShowColumnAndOrder) {
                rowData.add(resultItem.get(key));
            }
            rowDataList.add(rowData);
        }

        // 写入 Excel 文件
        writeExcel(filePath, tableHeaders, rowDataList);
    }

    private static void writeExcel(String filePath, List<List<String>> headers, List<List<Object>> data) {
        // 创建文件对象
        File file = new File(filePath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        // 使用 EasyExcel 写入
        EasyExcel.write(filePath)
                .head(headers)
                .sheet("Sheet1")
                .doWrite(data);
    }

    private static List<List<String>> createDynamicHead(List<String> resultShowColumnAndOrder, Map<String, String> tableHeaderMap) {
        List<List<String>> headList = new ArrayList<>();
        for (String key : resultShowColumnAndOrder) {
            List<String> headColumn = new ArrayList<>();
            headColumn.add(tableHeaderMap.get(key)); // 将动态表头内容放入
            headList.add(headColumn);
        }
        return headList;
    }

    public static Boolean lineRegexMatchAnd(String str, List<String> regexs) {
        if (CollectionUtils.isEmpty(regexs)) {
            return true;
        }
        if (StringUtils.isEmpty(str)) {
            return false;
        }

        Boolean result = true;
        for (int i = 0; i < regexs.size(); i++) {
            Pattern pattern = Pattern.compile(regexs.get(i));
            Matcher matcher = pattern.matcher(str);
            result = result && matcher.find();
        }

        return result;
    }

    public static Boolean lineRegexMatchOr(String str, List<String> regexs) {
        if (CollectionUtils.isEmpty(regexs)) {
            return true;
        }
        if (StringUtils.isEmpty(str)) {
            return false;
        }

        Boolean result = false;
        for (int i = 0; i < regexs.size(); i++) {
            Pattern pattern = Pattern.compile(regexs.get(i));
            Matcher matcher = pattern.matcher(str);
            result = result && matcher.find();
            if (result) {
                return result;
            }
        }

        return result;
    }

    public static String getBeforeResultLineAndOr(int index, List<String> inputFileContent, List<String> andConditionList, List<String> orConditionList) {
        String result = "";
        int myIndex = index - 1;
        while (myIndex >= 0) {
            String resultLine = inputFileContent.get(myIndex);
            if (lineRegexMatchAnd(resultLine, andConditionList) && lineRegexMatchOr(resultLine, orConditionList)) {
                result = resultLine;
                break;
            }
            myIndex--;
        }
        return result;
    }

    public static String getNextResultLineAndOr(int index, List<String> inputFileContent, List<String> andConditionList, List<String> orConditionList) {
        String result = "";
        int myIndex = index + 1;
        while (inputFileContent.size() > myIndex) {
            String resultLine = inputFileContent.get(myIndex);
            if (lineRegexMatchAnd(resultLine, andConditionList) && lineRegexMatchOr(resultLine, orConditionList)) {
                result = resultLine;
                break;
            }
            myIndex++;
        }
        return result;
    }

    public static List<String> readBaseFromFile(String filePath, List<String> inputFileRegexMatcherAnd, List<String> inputFileRegexMatcherOr) {
        List<String> fileLines = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(new File(filePath))) {
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (lineRegexMatchAnd(line, inputFileRegexMatcherAnd) && lineRegexMatchOr(line, inputFileRegexMatcherOr)) {
                    fileLines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileLines;
    }

    public static void main(String[] args) {
        // 输入
        InputParamsConfig inputParamsConfig = new InputParamsConfig();
        // 输入文件内容，多个会按顺序拼接
        inputParamsConfig.getInputFilePaths().add("/Users/lipancheng/Desktop/code/LogFileAnalysisX/src/main/java/org/lpc/logfileanalysisx/testLogFile/test1.log");
        // 输入文件基础过滤条件（减少内存占用），支持正则
//        inputParamsConfig.getInputFileRegexMatcherAnd.add("abc");
//        inputParamsConfig.getInputFileRegexMatcherOr().add("abc");

        // 输出
        OutputParamsConfig outputParamsConfig = new OutputParamsConfig();
        // 日志主行匹配规则，多个规则需同时成立，支持正则
        outputParamsConfig.getLineRegexMatcherAnd().add("日志");
        outputParamsConfig.getLineRegexMatcherAnd().add("input");
        // 分析后的表头，按顺序：0，1，2...
        outputParamsConfig.getTableHeader().put("0", "发送请求日志");
        outputParamsConfig.getTableHeader().put("1", "线程名");
        outputParamsConfig.getTableHeader().put("2", "结束日志");
        outputParamsConfig.getTableHeader().put("3", "请求开始时间");
        outputParamsConfig.getTableHeader().put("4", "请求结束时间");
        outputParamsConfig.getTableHeader().put("5", "请求耗时(ms)");
        // 分析后的表头输出顺序，不需要输出的中间变量可以不输出，为空时，默认按tableHeader顺序全部输出
        outputParamsConfig.getResultShowColumnAndOrder().add("1");
        outputParamsConfig.getResultShowColumnAndOrder().add("3");
        outputParamsConfig.getResultShowColumnAndOrder().add("4");
        outputParamsConfig.getResultShowColumnAndOrder().add("5");
        outputParamsConfig.getResultShowColumnAndOrder().add("2");
        // 分析后结果输出位置
        outputParamsConfig.setOutputFilePath("/Users/lipancheng/Desktop/code/LogFileAnalysisX/src/main/java/org/lpc/logfileanalysisx/testLogFile/结果.txt");

        /**
         * 列内容解析方法配置，和分析后的表头数量一致
         *
         * 通用参数：
         * * :RemoveNT[true]/:RemoveNT  移除结果列中的换行符、制表符
         *
         * 公共解析方法名称及参数：
         * * 获取日志行内容
         * * 获取线程名
         * * 计算两个时间之间的差值(ms)
         *      :Date1[${0}]/:Date1
         *      :Date2[${1}]/:Date2
         *      :Format[yyyy-MM-dd HH:mm:ss:SSS]/:Format
         * * 获取当前行之后，下一个满足条件的日志行
         *      :And[${0},output]/:And
         *      :Or[]/:Or
         * * 获取当前行之前，上一个满足条件的日志行
         *      :And[${0},output]/:And
         *      :Or[]/:Or
         * * 按字符串开始结束截取字符串
         *      :Input[${0}]/:Input
         *      :StartWithString[abc]/:StartWithString
         *      :EndWithString[efg]/:EndWithString
         * * 按下标开始结束截取字符串
         *      :Input[${0}]/:Input
         *      :StartWithIndex[2]/:StartWithIndex
         *      :EndWithIndex[5]/:EndWithIndex
         * * JSONPath提取内容
         *      :Input[${0}]/:Input
         *      :JSONPath[$.a]/:JSONPath
         * * 正则提取内容
         *      :Input[${0}]/:Input
         *      :Regex[TN.*\\(String\\)]/:Regex
         */

        outputParamsConfig.getTableColumnDealMethodConfig().put("0", "获取日志行内容");
        outputParamsConfig.getTableColumnDealMethodConfig().put("1", "获取线程名");
        outputParamsConfig.getTableColumnDealMethodConfig().put("2", "获取当前行之后，下一个满足条件的日志行" +
                ":And[${1},output]/:And" +
                ":Or[]/:Or"
        );
        outputParamsConfig.getTableColumnDealMethodConfig().put("3", "按下标开始结束截取字符串" +
                ":Input[${0}]/:Input" +
                ":StartWithIndex[0]/:StartWithIndex" +
                ":EndWithIndex[23]/:EndWithIndex");
        outputParamsConfig.getTableColumnDealMethodConfig().put("4", "按下标开始结束截取字符串" +
                ":Input[${2}]/:Input" +
                ":StartWithIndex[0]/:StartWithIndex" +
                ":EndWithIndex[23]/:EndWithIndex");
        outputParamsConfig.getTableColumnDealMethodConfig().put("5", "计算两个时间之间的差值(ms)" +
                ":Date1[${3}]/:Date1" +
                ":Date2[${4}]/:Date2" +
                ":Format[yyyy-MM-dd HH:mm:ss:SSS]/:Format");
        // 列内容分析方法配置，和表头数量一致
        Map<String, Function<ThreadContext, String>> tableColumnDealMethod = new HashMap<>();
        // 类内容公共解析方法转换为代码
        tableColumnDealMethodFill(tableColumnDealMethod, outputParamsConfig);

        // todo 这里可以自定义列内容解析方法，示例：
//        tableColumnDealMethod.put("5", (threadContext) -> {
//            List<String> inputFileContent = (List<String>) threadContext.get("inputFileContent");
//            int i = (int) threadContext.get("resultLineIndex");
//            String line = inputFileContent.get(i);
//            List<Map<String, String>> result = (List<Map<String, String>>) threadContext.get("result");
//            int resultLineIndex = (int) threadContext.get("resultLineIndex");
//            if (result.size() <= resultLineIndex) {
//                result.add(new HashMap<>());
//            }
//            Map<String, String> thisResultLine = result.get(resultLineIndex);
//
//            // 自定义处理逻辑：如：获取日志行内容
//            String lineLogResult = line;
//
//            thisResultLine.put("5", lineLogResult);
//            return lineLogResult;
//        });

        outputParamsConfig.setTableColumnDealMethod(tableColumnDealMethod);
        LogFileAnalysisXConfig config = new LogFileAnalysisXConfig();
        config.setInputParamsConfig(inputParamsConfig);
        config.setOutputParamsConfig(outputParamsConfig);

        System.out.println(JSON.toJSONString(config));
        dealFileFromInput(config);

        // 打开文件
//        try {
//            Desktop.getDesktop().open(new File(outputFilePath));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }


}
