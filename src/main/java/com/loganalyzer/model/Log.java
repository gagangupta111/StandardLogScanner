package com.loganalyzer.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.loganalyzer.util.JsonDateSerializer;

import java.util.Date;

@JsonAutoDetect
public class Log {

    private Date timestamp;
    private String level;
    private String className;
    private String methodName;
    private String fileName;
    private String line;
    private String message;

    public Log() {
    }

    public Log(Date timestamp, String level, String className, String methodName, String fileName, String line, String message) {
        this.timestamp = timestamp;
        this.level = level;
        this.className = className;
        this.methodName = methodName;
        this.fileName = fileName;
        this.line = line;
        this.message = message;
    }

    @JsonSerialize(using=JsonDateSerializer.class)
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
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

    @Override
    public String toString() {
        return "Log{" +
                "timestamp=" + timestamp +
                ", level='" + level + '\'' +
                ", className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", fileName='" + fileName + '\'' +
                ", line='" + line + '\'' +
                ", message='" + message + '\'' +
                '}';
    }

}
