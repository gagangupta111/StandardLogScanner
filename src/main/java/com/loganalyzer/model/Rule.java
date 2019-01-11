package com.loganalyzer.model;

public class Rule {

    private String ruleName;
    private String conditions;
    private String actions;

    public Rule() {
    }

    public Rule(String ruleName, String conditions, String actions) {
        this.ruleName = ruleName;
        this.conditions = conditions;
        this.actions = actions;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public String getActions() {
        return actions;
    }

    public void setActions(String actions) {
        this.actions = actions;
    }
}
