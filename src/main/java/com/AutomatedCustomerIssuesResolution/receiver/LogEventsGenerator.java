package com.AutomatedCustomerIssuesResolution.receiver;

import com.AutomatedCustomerIssuesResolution.model.Log;
import com.AutomatedCustomerIssuesResolution.model.SearchCriteria;
import com.AutomatedCustomerIssuesResolution.util.Utility;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.varia.LogFilePatternReceiver;

import java.util.List;

public class LogEventsGenerator extends LogFilePatternReceiver {

    private List<Log> logs;
    private SearchCriteria criteria;

    public LogEventsGenerator(List<Log> logs) {
        super();
        this.logs = logs;
    }

    public LogEventsGenerator(List<Log> logs, SearchCriteria criteria) {

        super();
        this.logs = logs;
        this.criteria = criteria;

    }

    public void doPost(LoggingEvent event) {

        Log log = new Log();
        log.setLogTimeStamp(event.getTimeStamp());
        log.setLevel(event.getLevel().toString());
        log.setClassName(event.getLocationInformation().getClassName().trim());
        log.setMethodName(event.getLocationInformation().getMethodName().trim());
        log.setClassFile(event.getLocationInformation().getFileName().trim());
        log.setLine(event.getLocationInformation().getLineNumber().trim());
        log.setLogFile(Utility.shortFileName(Utility.getFileName(event.getMDC("application").toString())));
        String message = event.getMessage().toString().trim();
        for(String string: event.getThrowableInformation().getThrowableStrRep()){
            message += string;
        }
        log.setMessage(message);

        if (log!= null && log.getLogTimeStamp() != null){
            if (criteria != null){
                if (criteria.getStarting() < log.getLogTimeStamp() && criteria.getEnding() > log.getLogTimeStamp()){
                    this.logs.add(log);
                }
            }else {
                this.logs.add(log);
            }
        }
    }
}

