package com.loganalyzer.dao;

import com.loganalyzer.model.Log;
import com.loganalyzer.model.SearchCriteria;

import java.util.List;
import java.util.Map;

public interface LogAnalyzerDao {

    public List<Log> getAllLogs();
    public List<Log> getLogsWithCriteria(SearchCriteria searchCriteria);

    }
