package org.lpc.logfileanalysisx.dto;

import java.util.ArrayList;
import java.util.List;

public class InputParamsConfig {

    private List<String> inputFilePaths = new ArrayList<>();

    private List<String> inputFileRegexMatcherAnd = new ArrayList<>();

    private List<String> inputFileRegexMatcherOr = new ArrayList<>();

    public List<String> getInputFileRegexMatcherOr() {
        return inputFileRegexMatcherOr;
    }

    public void setInputFileRegexMatcherOr(List<String> inputFileRegexMatcherOr) {
        this.inputFileRegexMatcherOr = inputFileRegexMatcherOr;
    }

    public List<String> getInputFileRegexMatcherAnd() {
        return inputFileRegexMatcherAnd;
    }

    public void setInputFileRegexMatcherAnd(List<String> inputFileRegexMatcherAnd) {
        this.inputFileRegexMatcherAnd = inputFileRegexMatcherAnd;
    }

    public List<String> getInputFilePaths() {
        return inputFilePaths;
    }

    public void setInputFilePaths(List<String> inputFilePaths) {
        this.inputFilePaths = inputFilePaths;
    }
}
