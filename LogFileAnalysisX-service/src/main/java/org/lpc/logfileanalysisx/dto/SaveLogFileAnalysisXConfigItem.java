package org.lpc.logfileanalysisx.dto;

public class SaveLogFileAnalysisXConfigItem {

    private String id;

    private String name;

    private LogFileAnalysisXConfig config;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LogFileAnalysisXConfig getConfig() {
        return config;
    }

    public void setConfig(LogFileAnalysisXConfig config) {
        this.config = config;
    }
}
