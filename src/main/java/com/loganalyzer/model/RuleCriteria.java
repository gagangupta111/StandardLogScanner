package com.loganalyzer.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.loganalyzer.util.JsonDateDeSerializer;

public class RuleCriteria {

    private Long date;
    private Long range;

    public RuleCriteria() {
    }

    public RuleCriteria(Long date, Long range) {
        this.date = date;
        this.range = range;
    }

    public Long getDate() {
        return date;
    }

    @JsonDeserialize(using=JsonDateDeSerializer.class)
    public void setDate(Long date) {
        this.date = date;
    }

    public Long getRange() {
        return range;
    }

    public void setRange(Long range) {
        this.range = range;
    }
}
