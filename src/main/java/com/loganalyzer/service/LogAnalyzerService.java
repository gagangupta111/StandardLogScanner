package com.loganalyzer.service;

import com.loganalyzer.dao.LogAnalyzerDao;
import com.loganalyzer.model.Log;
import com.loganalyzer.model.SearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class LogAnalyzerService {

    @Autowired
    @Qualifier("InitializedLogs")
    private LogAnalyzerDao logAnalyzerDao;

    public Map<String, List<Log>> getAllLogs() {
        return logAnalyzerDao.getAllLogs();
    }

    public Map<String, List<Log>> getLogsWithCriteria(SearchCriteria searchCriteria){
        return logAnalyzerDao.getLogsWithCriteria(searchCriteria);
    }

}
