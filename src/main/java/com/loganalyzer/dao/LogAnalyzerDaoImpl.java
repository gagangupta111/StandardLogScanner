package com.loganalyzer.dao;

import com.loganalyzer.main.Main;
import com.loganalyzer.model.Log;
import com.loganalyzer.receiver.LogEventsGenerator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.File;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Qualifier("InitializedLogs")
public class LogAnalyzerDaoImpl implements LogAnalyzerDao{

    public static Map<String, List<Log>> logs = new HashMap<String, List<Log>>();

    private File getFile(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(fileName).getFile());
    }

    @PostConstruct
    public void initialize() {
        Date date = new Date();
        long dateGetTime = date.getTime();
        System.out.println(date);
        System.out.println(new Timestamp(dateGetTime));
        // WebCR_Ignite.log WebCR-2018-Nov-23-1.log
        String fileName = "WebCRLessData.log";
        Main main = new Main();
        File file = getFile(fileName);

        String logFormat = "%d{yyyy-MMM-dd EEE HH:mm:ss.SSS} %-5level - %c - %method(%file:%line) -" +
                "            %msg%xEx%n";
        String adLogFormat = "TIMESTAMP LEVEL - CLASS - METHOD(FILE:LINE) -";
        LogEventsGenerator receiver = new LogEventsGenerator();
        receiver.setTimestampFormat("yyyy-MMM-dd EEE HH:mm:ss.SSS");
        receiver.setLogFormat(adLogFormat);
        receiver.setFileURL("file:///" + file.getAbsolutePath());
        receiver.setTailing(false);
        receiver.activateOptions();
    }

    @Override
    public Map<String, List<Log>> getAllLogs() {
        return logs;
    }
}
