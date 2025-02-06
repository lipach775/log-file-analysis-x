package org.lpc.logfileanalysisx.dto;

import java.util.List;
import java.util.Map;

public class ThreadContext {

    private List<Map<String, String>> result;

    private int resultLineIndex;

    private InputParamsConfig inputParamsConfig;

    private OutputParamsConfig outputParamsConfig;

    private List<String> inputFileContent;

    private int inputLineIndex;

    public List<Map<String, String>> getResult() {
        return result;
    }

    public void setResult(List<Map<String, String>> result) {
        this.result = result;
    }

    public int getResultLineIndex() {
        return resultLineIndex;
    }

    public void setResultLineIndex(int resultLineIndex) {
        this.resultLineIndex = resultLineIndex;
    }

    public InputParamsConfig getInputParamsConfig() {
        return inputParamsConfig;
    }

    public void setInputParamsConfig(InputParamsConfig inputParamsConfig) {
        this.inputParamsConfig = inputParamsConfig;
    }

    public OutputParamsConfig getOutputParamsConfig() {
        return outputParamsConfig;
    }

    public void setOutputParamsConfig(OutputParamsConfig outputParamsConfig) {
        this.outputParamsConfig = outputParamsConfig;
    }

    public List<String> getInputFileContent() {
        return inputFileContent;
    }

    public void setInputFileContent(List<String> inputFileContent) {
        this.inputFileContent = inputFileContent;
    }

    public int getInputLineIndex() {
        return inputLineIndex;
    }

    public void setInputLineIndex(int inputLineIndex) {
        this.inputLineIndex = inputLineIndex;
    }
}
