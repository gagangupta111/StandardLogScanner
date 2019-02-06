package com.loganalyzer.model;

import java.util.LinkedHashMap;

public class Message {

    private LinkedHashMap<String, String> message = new LinkedHashMap<>();

    public Message() {
    }

    public LinkedHashMap<String, String> getMessage() {
        return message;
    }

    public String getMessage(String key) {
        return message.get(key);
    }

    public void addMessage(String key, String value) {
        this.message.put(key, value);
    }
}
