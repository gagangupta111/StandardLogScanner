package com.loganalyzer.main;

import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.varia.LogFilePatternReceiver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Date;

public class Main {

    public static void main(String[] args){

        Date date = new Date();
        long dateGetTime = date.getTime();
        System.out.println(date);
        System.out.println(new Timestamp(dateGetTime));
        String fileName = "WebCR-2018-Nov-23-1.log";
        Main main = new Main();
        File file = main.getFile(fileName);

        // LogFilePatternLayoutBuilder builder = new LogFilePatternLayoutBuilder();

        FileReader fr = null;
        try {
            fr = new FileReader(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        };

        String logFormat = "%d{yyyy-MMM-dd EEE HH:mm:ss.SSS} %-5level - %c - %method(%file:%line) -" +
                "            %msg%xEx%n";
        String adLogFormat = "TIMESTAMP LEVEL - CLASS - METHOD(FILE:LINE) -*";
        LogFilePatternReceiver receiver = new MyEventReceiver();
        receiver.setTimestampFormat("yyyy-MMM-dd EEE HH:mm:ss.SSS");
        receiver.setLogFormat(adLogFormat);
        receiver.setFileURL("file:///" + file.getAbsolutePath());
        receiver.setTailing(false);
        receiver.activateOptions();

        BufferedReader br = new BufferedReader(fr);

        // receiver.process(br);

    }

    private File getFile(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(fileName).getFile());
    }

}


class MyEventReceiver extends LogFilePatternReceiver {

    public void process(BufferedReader reader) {
        try {
            super.process(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void doPost(LoggingEvent event) {
        System.out.println(event);
    }
}
