package com.loganalyzer.model;

import java.util.Date;

public class Log {

    private Date date;
    private String level;
    private String className;
    private String methodName;
    private String fileName;
    private Integer line;
    private String logDetails;

    public Log() {
    }

    public Log(Date date, String level, String className, String methodName, String fileName, Integer line, String logDetails) {
        this.date = date;
        this.level = level;
        this.className = className;
        this.methodName = methodName;
        this.fileName = fileName;
        this.line = line;
        this.logDetails = logDetails;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
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

    public Integer getLine() {
        return line;
    }

    public void setLine(Integer line) {
        this.line = line;
    }

    public String getLogDetails() {
        return logDetails;
    }

    public void setLogDetails(String logDetails) {
        this.logDetails = logDetails;
    }



}
