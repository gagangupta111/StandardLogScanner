package com.loganalyzer.dao;

import com.loganalyzer.model.Log;
import com.loganalyzer.model.Rule;
import com.loganalyzer.model.RuleCriteria;
import com.loganalyzer.model.SearchCriteria;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface LogAnalyzerDao {

    public List<Log> getAllLogs();
    public List<Rule> getAllRules();
    public Map<String, String> checkAllRules() throws IOException;
    public List<Log> getLogsWithCriteria(SearchCriteria searchCriteria);

    }
