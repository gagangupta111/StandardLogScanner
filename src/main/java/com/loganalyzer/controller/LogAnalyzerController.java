package com.loganalyzer.controller;

import com.loganalyzer.model.Log;
import com.loganalyzer.service.LogAnalyzerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/logs")
public class LogAnalyzerController {

    @Autowired
    private LogAnalyzerService logAnalyzerService;

    @RequestMapping(method = RequestMethod.GET)
    public Map<String, List<Log>> getAllLogs() {
        return logAnalyzerService.getAllLogs();
    }

}
