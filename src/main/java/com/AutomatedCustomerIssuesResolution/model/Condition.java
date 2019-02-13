package com.AutomatedCustomerIssuesResolution.model;

public class Condition {

    private String name;

    private String level;
    private String className;
    private String methodName;
    private String classFile;
    private String line;
    private String logFile;
    private Message message;

    public Condition() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public SearchCriteria mapToSearchCriteria(){
        SearchCriteria criteria = new SearchCriteria();
        criteria.setClassFile(this.classFile);
        criteria.setClassName(this.className);
        criteria.setLevel(this.level);
        criteria.setLine(this.line);
        criteria.setLogFile(this.logFile);
        criteria.setMethodName(this.methodName);
        String message = "";
        for (MessageKeyValuePair pair : this.message.getMessage()){
            message += pair.getKey() + ":" + pair.getValue() + ", ";
        }
        message = message.substring(0, message.length() - 2);
        criteria.setMessage(message);
        return criteria;
    }

}
