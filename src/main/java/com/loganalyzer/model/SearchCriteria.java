package com.loganalyzer.model;

import java.sql.Timestamp;

public class SearchCriteria {

    private Timestamp starting;
    private Timestamp ending;
    private String level;
    private String className;
    private String methodName;
    private String fileName;
    private String line;
    private String message;

    public SearchCriteria() {
    }

    public Timestamp getStarting() {
        return starting;
    }

    public void setStarting(Timestamp starting) {
        this.starting = starting;
    }

    public Timestamp getEnding() {
        return ending;
    }

    public void setEnding(Timestamp ending) {
        this.ending = ending;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
