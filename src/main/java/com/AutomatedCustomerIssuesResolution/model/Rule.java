package com.AutomatedCustomerIssuesResolution.model;

import java.util.List;

public class Rule {

    private String ruleName;
    private boolean enable;
    private String desc;
    private List<Condition> conditions;
    private String actions;
    private String query;

    public Rule() {
    }

    public Rule(String ruleName, String desc, List<Condition> conditions, String actions, String query) {
        this.ruleName = ruleName;
        this.desc = desc;
        this.conditions = conditions;
        this.actions = actions;
        this.query = query;
    }

    public Condition getConditionbyName(String name){
        for (Condition condition : conditions){
            if (condition.getName().equals(name)){
                return condition;
            }
        }
        return null;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public String getActions() {
        return actions;
    }

    public void setActions(String actions) {
        this.actions = actions;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
