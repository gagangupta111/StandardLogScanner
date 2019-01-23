package com.loganalyzer.service;

import com.loganalyzer.dao.LogAnalyzerDao;
import com.loganalyzer.model.Log;
import com.loganalyzer.model.Rule;
import com.loganalyzer.model.RuleCriteria;
import com.loganalyzer.model.SearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class LogAnalyzerService {

    @Autowired
    @Qualifier("InitializedLogs")
    private LogAnalyzerDao logAnalyzerDao;

    public List<Log> getAllLogs() {
        return logAnalyzerDao.getAllLogs();
    }

    public List<Rule> getAllRules() {
        return logAnalyzerDao.getAllRules();
    }

    public  Map<String, String> checkAllRules() throws Exception {
        return logAnalyzerDao.checkAllRules();
    }

    public List<Log> getLogsWithCriteria(SearchCriteria searchCriteria){
        return logAnalyzerDao.getLogsWithCriteria(searchCriteria);
    }

}
