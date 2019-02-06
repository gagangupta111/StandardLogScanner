package com.loganalyzer.model;

import java.util.ArrayList;
import java.util.List;

public class Message {

    private List<MessageKeyValuePair> message = new ArrayList<>();

    public Message() {
    }

    public List<MessageKeyValuePair> getMessage() {
        return message;
    }

    public void addMessage(String key, String value) {
        this.message.add(new MessageKeyValuePair(key, value));
    }
}
