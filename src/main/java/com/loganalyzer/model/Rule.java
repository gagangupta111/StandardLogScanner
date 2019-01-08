package com.loganalyzer.model;

public class Rule {

    private String ruleName;
    private String keywords;
    private String actions;

    public Rule() {
    }

    public Rule(String ruleName, String keywords, String actions) {
        this.ruleName = ruleName;
        this.keywords = keywords;
        this.actions = actions;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getActions() {
        return actions;
    }

    public void setActions(String actions) {
        this.actions = actions;
    }
}
