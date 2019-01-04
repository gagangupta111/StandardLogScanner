package com.loganalyzer.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.loganalyzer.util.JsonDateDeSerializer;

import java.sql.Timestamp;

@JsonAutoDetect
public class SearchCriteria {

    private Long starting;
    private Long ending;
    private String level;
    private String className;
    private String methodName;
    private String fileName;
    private String line;
    private String message;

    public SearchCriteria() {
    }

    public Long getStarting() {
        return starting;
    }

    @JsonDeserialize(using=JsonDateDeSerializer.class)
    public void setStarting(Long starting) {
        this.starting = starting;
    }

    public Long getEnding() {
        return ending;
    }

    @JsonDeserialize(using=JsonDateDeSerializer.class)
    public void setEnding(Long ending) {
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
