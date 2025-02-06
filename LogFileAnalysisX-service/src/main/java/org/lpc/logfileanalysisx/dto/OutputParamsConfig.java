package org.lpc.logfileanalysisx.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class OutputParamsConfig {

    private List<String> lineRegexMatcherAnd = new ArrayList<>();

    private Map<String, String> tableHeader = new HashMap<>();

    private List<String> resultShowColumnAndOrder = new ArrayList<>();

    private String outputFilePath;

    private Map<String, String> tableColumnDealMethodConfig = new HashMap<>();

    private Map<String, Function<ThreadContext, String>> tableColumnDealMethod = new HashMap<>();


    private List<String> tableHeaderList = new ArrayList<>();
    private List<String> tableColumnDealMethodConfigList = new ArrayList<>();


    public List<String> getLineRegexMatcherAnd() {
        return lineRegexMatcherAnd;
    }

    public void setLineRegexMatcherAnd(List<String> lineRegexMatcherAnd) {
        this.lineRegexMatcherAnd = lineRegexMatcherAnd;
    }

    public Map<String, String> getTableHeader() {
        return tableHeader;
    }

    public void setTableHeader(Map<String, String> tableHeader) {
        this.tableHeader = tableHeader;
    }

    public List<String> getResultShowColumnAndOrder() {
        return resultShowColumnAndOrder;
    }

    public void setResultShowColumnAndOrder(List<String> resultShowColumnAndOrder) {
        this.resultShowColumnAndOrder = resultShowColumnAndOrder;
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }

    public void setOutputFilePath(String outputFilePath) {
        this.outputFilePath = outputFilePath;
    }

    public Map<String, String> getTableColumnDealMethodConfig() {
        return tableColumnDealMethodConfig;
    }

    public void setTableColumnDealMethodConfig(Map<String, String> tableColumnDealMethodConfig) {
        this.tableColumnDealMethodConfig = tableColumnDealMethodConfig;
    }

    public Map<String, Function<ThreadContext, String>> getTableColumnDealMethod() {
        return tableColumnDealMethod;
    }

    public void setTableColumnDealMethod(Map<String, Function<ThreadContext, String>> tableColumnDealMethod) {
        this.tableColumnDealMethod = tableColumnDealMethod;
    }

    public List<String> getTableHeaderList() {
        return tableHeaderList;
    }

    public void setTableHeaderList(List<String> tableHeaderList) {
        this.tableHeaderList = tableHeaderList;
    }

    public List<String> getTableColumnDealMethodConfigList() {
        return tableColumnDealMethodConfigList;
    }

    public void setTableColumnDealMethodConfigList(List<String> tableColumnDealMethodConfigList) {
        this.tableColumnDealMethodConfigList = tableColumnDealMethodConfigList;
    }
}