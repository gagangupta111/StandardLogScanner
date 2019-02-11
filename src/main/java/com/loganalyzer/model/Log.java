package com.loganalyzer.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.loganalyzer.util.JsonDateDeSerializer;
import com.loganalyzer.util.JsonDateSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@JsonAutoDetect
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Log implements Comparable<Log>{

    private String id;
    @JsonFormat(pattern="yyyy-MMM-dd EEE HH:mm:ss.SSS")
    private Long logTimeStamp;
    private String level;
    private String className;
    private String methodName;
    private String classFile;
    private String line;
    private String logFile;
    private String message;

    public Log() {
    }

    public Log(String id, Long timestamp, String level, String className, String methodName,
               String classFile, String line, String logFile, String message) {
        this.id = id;
        this.logTimeStamp = timestamp;
        this.level = level;
        this.className = className;
        this.methodName = methodName;
        this.classFile = classFile;
        this.line = line;
        this.logFile = logFile;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonSerialize(using=JsonDateSerializer.class)
    public Long getLogTimeStamp() {
        return logTimeStamp;
    }

    @JsonDeserialize(using=JsonDateDeSerializer.class)
    public void setLogTimeStamp(Long logTimeStamp) {
        this.logTimeStamp = logTimeStamp;
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

    public String getClassFile() {
        return classFile;
    }

    public void setClassFile(String classFile) {
        this.classFile = classFile;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getLogFile() {
        return logFile;
    }

    public void setLogFile(String logFile) {
        this.logFile = logFile;
    }

    public Map<String, Object> map(){
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("logTimeStamp", logTimeStamp);
        map.put("level", level);
        map.put("className", className);
        map.put("methodName", methodName);
        map.put("classFile", classFile);
        map.put("line", line);
        map.put("logFile", logFile);
        map.put("message", message);
        return map;
    }

    public static Log mapToLog(Map<String, Object> map){

        Log log = new Log();
        log.setId(map.get("id").toString());
        log.setLogTimeStamp(Long.valueOf(map.get("logTimeStamp").toString()));
        log.setLevel(map.get("level").toString());
        log.setClassName(map.get("className").toString());
        log.setMethodName(map.get("methodName").toString());
        log.setClassFile(map.get("classFile").toString());
        log.setLine(map.get("line").toString());
        log.setLogFile(map.get("logFile").toString());
        log.setMessage(map.get("message").toString());
        return log;
    }

    @Override
    public String toString() {
        return "Log{" +
                "id='" + id + '\'' +
                ", logTimeStamp=" + logTimeStamp +
                ", level='" + level + '\'' +
                ", className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", classFile='" + classFile + '\'' +
                ", line='" + line + '\'' +
                ", logFile='" + logFile + '\'' +
                ", message='" + message + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Log log = (Log) o;
        return  (getId() == log.getId() ? true : getId().equals(log.getId())) &&
                (getLogTimeStamp() == log.getLogTimeStamp() ? true : getLogTimeStamp().equals(log.getLogTimeStamp())) &&
                (getLevel() == log.getLevel() ? true : getLevel().equals(log.getLevel())) &&
                (getClassName() == log.getClassName() ? true : getClassName().equals(log.getClassName())) &&
                (getMethodName() == log.getMethodName() ? true : getMethodName().equals(log.getMethodName()) &&
                (getClassFile() == log.getClassFile() ? true : getClassFile().equals(log.getClassFile()))) &&
                (getLine() == log.getLine() ? true : getLine().equals(log.getLine())) &&
                (getLogFile() == log.getLogFile() ? true : getLogFile().equals(log.getLogFile())) &&
                (getMessage() == log.getMessage() ? true : getMessage().equals(log.getMessage()));
    }

    @Override
    public int hashCode() {

        return Objects.hash(getId(), getLogTimeStamp(), getLevel(), getClassName(), getMethodName(), getClassFile(), getLine(), getLogFile(), getMessage());
    }

    @Override
    public int compareTo(Log o) {
        if (o == null){
            return 1;
        }
        Long time1 = this.getLogTimeStamp();
        Long time2 = o.getLogTimeStamp();
        if (time1 > time2) {
            return 1;
        }else if (time1 < time2){
            return -1;
        }else return 0;

    }

    public Log clone(){
        return new Log(this.id, this.logTimeStamp, this.level, this.className, this.methodName, this.classFile, this.line, this.logFile, this.message);
    }
}
