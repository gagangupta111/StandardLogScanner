package com.loganalyzer.dao;

import com.loganalyzer.model.Log;
import com.loganalyzer.model.SearchCriteria;
import com.loganalyzer.receiver.LogEventsGenerator;
import com.loganalyzer.util.Utility;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Repository
@Qualifier("InitializedLogs")
@PropertySource("classpath:application.properties")
public class LogAnalyzerDaoImpl implements LogAnalyzerDao{

    private Map<String, List<Log>> logs = new HashMap<String, List<Log>>();

    @Autowired
    ApplicationArguments appArgs;

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

        logsPath = appArgs.getNonOptionArgs().get(0);

        List<File> list = new ArrayList<File>();
        String[] compressedList = new String[] {"zip", "gz"};
        Collection<File> compressedFiles = FileUtils.listFiles(new File(logsPath), compressedList, true);
        for (File file1 : compressedFiles){
            String ext = FilenameUtils.getExtension(file1.getPath());
            if (compressedList[0].equals(ext)){
                String inputFile = file1.getAbsolutePath();
                String outputFolder = inputFile.substring(0, inputFile.lastIndexOf("\\"));
                unZipIt(inputFile, outputFolder);
            }else if (compressedList[1].equals(ext)){
                String inputFile = file1.getAbsolutePath();
                String outputFile = inputFile.substring(0, inputFile.lastIndexOf("."));
                gunzipIt(inputFile, outputFile);
            }
        }

        String[] extensions = new String[] { "log" };
        Collection<File> array = FileUtils.listFiles(new File(logsPath), extensions, true);

        String format;
        for (File file : array){

            String fileName = Utility.getFileName(file.getPath());
            boolean found = filesWithFormatPatternNoLocation
                    .stream()
                    .filter((string) -> fileName.contains(string)).findFirst().isPresent();
            if (found){
                generator(formatPatternNoLocation, file);
            }else {
                found = filesWithFormatPattern
                    .stream()
                    .filter((string) -> fileName.contains(string)).findFirst().isPresent();
                if (found) {
                    generator(formatPattern, file);
                }
            }
        }

    }

    private void generator(String format, File file){
        LogEventsGenerator receiver = new LogEventsGenerator(logs);
        receiver.setTimestampFormat(timestamp);
        receiver.setLogFormat(format);
        receiver.setFileURL("file:///" + file.getAbsolutePath());
        receiver.setTailing(false);
        receiver.activateOptions();
    }

    @Override
    public Map<String, List<Log>> getAllLogs() {
        return logs;
    }

    @Override
    public Map<String, List<Log>> getLogsWithCriteria(SearchCriteria searchCriteria){

        Map<String, List<Log>> newMap = logs;
        if (searchCriteria.getFileName()!= null){
            newMap = getLogsFilteredByFileName(newMap, searchCriteria.getFileName());
        }

        if (searchCriteria.getLevel()!= null){
            newMap =  getLogsFilteredByLogLevel(newMap, searchCriteria.getLevel());
        }

        if (searchCriteria.getStarting()!= null && searchCriteria.getEnding() != null){
            newMap = getLogsFilteredByTimeStamp(newMap, searchCriteria.getStarting(), searchCriteria.getEnding());
        }

        if (searchCriteria.getMessage() != null){
            newMap = getLogsFilteredByMessage(newMap, searchCriteria.getMessage());
        }

        return newMap;
    }

    public Map<String, List<Log>> getLogsFilteredByTimeStamp(Map<String, List<Log>> map, Timestamp starting, Timestamp ending){

        Map<String, List<Log>> newMap = new HashMap<>();

        for (String key : map.keySet()){

            List<Log> list = map.get(key)
                    .stream()
                    .filter((log) -> starting.compareTo(log.getTimestamp()) <= 0 && ending.compareTo(log.getTimestamp()) >= 0).collect(Collectors.toList());
            newMap.put(key, list);
        }
        return newMap;
    }

    public Map<String, List<Log>> getLogsFilteredByMessage(Map<String, List<Log>> map, String message){

        Map<String, List<Log>> newMap = new HashMap<>();

        for (String key : map.keySet()){

            List<Log> list = map.get(key)
                    .stream()
                    .filter((log) -> log.getMessage().contains(message)).collect(Collectors.toList());
            newMap.put(key, list);
        }
        return newMap;
    }

    public Map<String, List<Log>> getLogsFilteredByLogLevel(Map<String, List<Log>> map, String logLevel){

        Map<String, List<Log>> newMap = new HashMap<>();

        for (String key : map.keySet()){

            List<Log> list = map.get(key)
                            .stream()
                            .filter((log) -> logLevel.equals(log.getLevel())).collect(Collectors.toList());
            newMap.put(key, list);
        }
        return newMap;
    }

    public Map<String, List<Log>> getLogsFilteredByFileName(Map<String, List<Log>> map, String fileName){

        Map<String, List<Log>> newMap = new HashMap<>();
        newMap.put(fileName, map.get(fileName));
        return newMap;

    }

    public String getWhiteListedFileName(String fileName){

        Optional<String> name = filesWithFormatPatternNoLocation
                .stream()
                .filter((string) -> fileName.contains(string)).findFirst();
        if (name.isPresent()){
            return name.get();
        }else {
            name = filesWithFormatPattern
                    .stream()
                    .filter((string) -> fileName.contains(string)).findFirst();
            if (name.isPresent()){
                return name.get();
            }else {
                return fileName.substring(0, Utility.indexOf(Pattern.compile("-"), fileName));
            }
        }
    }

    public void gunzipIt(String inputFile, String outputFile){

        byte[] buffer = new byte[1024];

        try{

            GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(inputFile));
            FileOutputStream out = new FileOutputStream(outputFile);

            int len;
            while ((len = gzis.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }

            gzis.close();
            out.close();

            System.out.println("Done");

        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    public void unZipIt(String zipFile, String outputFolder){

        byte[] buffer = new byte[1024];

        try{

            //create output directory is not exists
            File folder = new File(outputFolder);
            if(!folder.exists()){
                folder.mkdir();
            }

            //get the zip file content
            ZipInputStream zis =
                    new ZipInputStream(new FileInputStream(zipFile));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while(ze!=null){

                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);

                System.out.println("file unzip : "+ newFile.getAbsoluteFile());

                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

            System.out.println("Done");

        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

}

