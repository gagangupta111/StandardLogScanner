package com.loganalyzer.dao;

import com.loganalyzer.model.Log;

import java.util.List;
import java.util.Map;

public interface LogAnalyzerDao {

    public Map<String, List<Log>> getAllLogs();

}
