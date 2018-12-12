package com.loganalyzer.dao;

import com.loganalyzer.model.Log;
import com.loganalyzer.receiver.LogEventsGenerator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Qualifier("InitializedLogs")
@PropertySource("classpath:application.properties")
public class LogAnalyzerDaoImpl implements LogAnalyzerDao{

    private Map<String, List<Log>> logs = new HashMap<String, List<Log>>();

    @Value("${logs.path}")
    private String logsPath;

    @Value("${formatPattern}")
    private String formatPattern;

    @Value("${formatPatternNoLocation}")
    private String formatPatternNoLocation;

    @Value("${timestamp}")
    private String timestamp;

    @Value("#{'${filesWithFormatPattern}'.split(',')}")
    private List<String> filesWithFormatPattern;

    @Value("#{'${filesWithFormatPatternNoLocation}'.split(',')}")
    private List<String> filesWithFormatPatternNoLocation;

    private File getFile(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(fileName).getFile());
    }

    @PostConstruct
    public void initialize() {

        List<File> list = new ArrayList<File>();
        String[] extensions = new String[] { "log" };
        Collection<File> array = FileUtils.listFiles(new File(logsPath), extensions, true);

        String format;
        for (File file1 : array){

            String ext = FilenameUtils.getExtension(file1.getPath());
            String fileName = file1.getPath().substring(file1.getPath().lastIndexOf("/") + 1, file1.getPath().lastIndexOf("."));

            boolean found = filesWithFormatPatternNoLocation
                    .stream()
                    .filter((string) -> fileName.contains(string)).findFirst().isPresent();
            if (found){
                format = formatPatternNoLocation;
            }else {
                format = formatPattern;
            }

            if ("log".equals(ext)){

                LogEventsGenerator receiver = new LogEventsGenerator(logs);
                receiver.setTimestampFormat(timestamp);
                receiver.setLogFormat(format);
                receiver.setFileURL("file:///" + file1.getAbsolutePath());
                receiver.setTailing(false);
                receiver.activateOptions();
            }
        }

    }

    @Override
    public Map<String, List<Log>> getAllLogs() {
        return logs;
    }
}
