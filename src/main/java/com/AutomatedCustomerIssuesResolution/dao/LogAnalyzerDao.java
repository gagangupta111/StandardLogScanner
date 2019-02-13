package com.AutomatedCustomerIssuesResolution.dao;

import com.AutomatedCustomerIssuesResolution.model.Log;
import com.AutomatedCustomerIssuesResolution.model.Rule;
import com.AutomatedCustomerIssuesResolution.model.SearchCriteria;

import java.util.List;
import java.util.Map;

public interface LogAnalyzerDao {

    public List<Log> getAllLogs();
    public List<Rule> getAllRules();
    public Map<String, String> checkAllRules() throws Exception;
    public List<Log> getLogsWithCriteria(SearchCriteria searchCriteria);

    }
