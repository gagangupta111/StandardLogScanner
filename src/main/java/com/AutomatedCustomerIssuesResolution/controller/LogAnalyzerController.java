package com.AutomatedCustomerIssuesResolution.controller;

import com.AutomatedCustomerIssuesResolution.model.Log;
import com.AutomatedCustomerIssuesResolution.model.Rule;
import com.AutomatedCustomerIssuesResolution.model.SearchCriteria;
import com.AutomatedCustomerIssuesResolution.service.LogAnalyzerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/logs")
public class LogAnalyzerController {

    @Autowired
    private LogAnalyzerService logAnalyzerService;

    @RequestMapping(method = RequestMethod.GET)
    public List<Log> getAllLogs() {
        return logAnalyzerService.getAllLogs();
    }

    @RequestMapping(value = "/execute", method = RequestMethod.GET)
    public Map<String, String> checkAllRules() throws Exception {
        return logAnalyzerService.checkAllRules();
    }

    @RequestMapping(value = "/rules", method = RequestMethod.GET)
    public List<Rule> getAllRules() throws IOException {
        return logAnalyzerService.getAllRules();
    }

    @RequestMapping(value = "/filter", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<Log> getLogsWithCriteria(@RequestBody SearchCriteria searchCriteria){
        return logAnalyzerService.getLogsWithCriteria(searchCriteria);
    }

}
