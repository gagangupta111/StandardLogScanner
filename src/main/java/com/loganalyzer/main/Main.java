package com.loganalyzer.main;

import com.loganalyzer.receiver.LogEventsGenerator;

import java.io.File;
import java.sql.Timestamp;
import java.util.Date;

public class Main {

    public static void main(String[] args){

        Date date = new Date();
        long dateGetTime = date.getTime();
        System.out.println(date);
        System.out.println(new Timestamp(dateGetTime));
        // WebCR_Ignite.log WebCR-2018-Nov-23-1.log
        String fileName = "WebCR-2018-Nov-23-1.log";
        Main main = new Main();
        File file = main.getFile(fileName);

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

    private File getFile(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(fileName).getFile());
    }

}
