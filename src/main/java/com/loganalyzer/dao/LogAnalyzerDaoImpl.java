package com.loganalyzer.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loganalyzer.model.Log;
import com.loganalyzer.model.Rule;
import com.loganalyzer.model.RuleCriteria;
import com.loganalyzer.model.SearchCriteria;
import com.loganalyzer.receiver.LogEventsGenerator;
import com.loganalyzer.util.Utility;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.PredicateUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Repository
@Qualifier("InitializedLogs")
@PropertySource("classpath:application.properties")
public class LogAnalyzerDaoImpl implements LogAnalyzerDao{

    private List<Log> logs = new ArrayList<>();
    private List<Rule> rules = new ArrayList<>();
    private List<Rule> newRules = new ArrayList<>();

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

    private void populateNewRules() throws Exception {

        // Finds the workbook instance for XLSX file
        XSSFWorkbook myWorkBook;
        myWorkBook = new XSSFWorkbook(getClass().getResourceAsStream("/rules2.xlsx"));

        // Return first sheet from the XLSX workbook
        XSSFSheet mySheet = myWorkBook.getSheetAt(0);

        // Get iterator to all the rows in current sheet
        Iterator<Row> rowIterator = mySheet.iterator();

        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        String ruleName;
        String desc;
        String conditions;
        String actions;

        // Traversing over each row of XLSX file
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Rule rule = new Rule();
            // For each row, iterate through each columns
            Iterator<Cell> cellIterator = row.cellIterator();
            if (cellIterator.hasNext()){
                ruleName = cellIterator.next().getStringCellValue();
                if ("".equals(ruleName)){
                    continue;
                }
                rule.setRuleName(ruleName);
            }
            if (cellIterator.hasNext()){
                desc = cellIterator.next().getStringCellValue();
                if (desc == null){
                    continue;
                }
                rule.setDesc(desc);
            }
            if (cellIterator.hasNext()){
                conditions = cellIterator.next().getStringCellValue();
                if (conditions == null){
                    continue;
                }
                rule.setConditions(conditions);
            }
            if (cellIterator.hasNext()){
                actions = cellIterator.next().getStringCellValue();
                if (actions == null){
                    continue;
                }
                rule.setActions(actions);
            }
            rules.add(rule);
        }

    }

    private void populateLogs(){

        try {
            logsPath = appArgs.getNonOptionArgs().get(0);
        }catch (Exception e){
            logsPath = ".";
        }

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

    @PostConstruct
    public void initialize() throws Exception {

        ListUtils.predicatedList(logs, PredicateUtils.notNullPredicate());
        populateLogs();
        populateNewRules();
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
    public List<Log> getAllLogs() {
        Collections.sort(logs);
        return logs;
    }

    @Override
    public List<Rule> getAllRules(){
        return rules;
    }

    public Map<String, String> checkAllRules(RuleCriteria ruleCriteria) throws IOException {

        Map<String, String> rulesResponse = new HashMap<>();
        SearchCriteria searchCriteria = new SearchCriteria();
        if (ruleCriteria.getRange() != null) {
            searchCriteria.setStarting(ruleCriteria.getDate() - ruleCriteria.getRange());
            searchCriteria.setEnding(ruleCriteria.getDate() + ruleCriteria.getRange());
        }else {
            searchCriteria.setStarting(ruleCriteria.getDate() - 100);
            searchCriteria.setEnding(ruleCriteria.getDate() + 100);
        }

        List<Log> logList = getLogsWithCriteria(searchCriteria);

        ObjectMapper mapper = new ObjectMapper();
        for (Rule rule : rules){
            List<Log> newLogList = logList;
            String Conditions = rule.getConditions();
            List<SearchCriteria> list;
                list = mapper.readValue(Conditions, new TypeReference<List<SearchCriteria>>(){});

            for (SearchCriteria criteria : list){
                newLogList = getLogsWithCriteria(newLogList, criteria);
                if (newLogList != null && !newLogList.isEmpty()){
                    continue;
                }else break;
            }
            if (newLogList != null && !newLogList.isEmpty()) {
                rulesResponse.put(rule.getRuleName(), rule.getActions());
            }
        }

        return rulesResponse;
    }

    @Override
    public List<Log> getLogsWithCriteria(SearchCriteria searchCriteria){

        List<Log> newLogs = logs;

        if (searchCriteria.getStarting() != null) {
            newLogs  = getLogsFilteredByStartingDate(newLogs, searchCriteria.getStarting());
        }

        if (searchCriteria.getEnding() != null) {
            newLogs  = getLogsFilteredByEndingDate(newLogs, searchCriteria.getEnding());
        }

        if (searchCriteria.getLevel()!= null){
            newLogs = getLogsFilteredByLogLevel(newLogs, searchCriteria.getLevel());
        }

        if (searchCriteria.getLogFile()!= null){
            newLogs = getLogsFilteredByLogFile(newLogs, searchCriteria.getLogFile());
        }

        if (searchCriteria.getMethodName()!= null){
            newLogs = getLogsFilteredByMethodName(newLogs, searchCriteria.getMethodName());
        }

        if (searchCriteria.getClassFile()!= null){
            newLogs = getLogsFilteredByClassFile(newLogs, searchCriteria.getClassFile());
        }

        if (searchCriteria.getLine()!= null){
            newLogs = getLogsFilteredByLine(newLogs, searchCriteria.getLine());
        }

        if (searchCriteria.getClassName()!= null){
            newLogs = getLogsFilteredByClassName(newLogs, searchCriteria.getClassName());
        }

        if (searchCriteria.getMessage()!= null){
            newLogs = getLogsFilteredByMessage(newLogs, searchCriteria.getMessage());
        }

        return newLogs;
    }

    public List<Log> getLogsWithCriteria(List<Log> logs, SearchCriteria searchCriteria){

        List<Log> newLogs = logs;

        if (searchCriteria.getStarting() != null) {
            newLogs  = getLogsFilteredByStartingDate(newLogs, searchCriteria.getStarting());
        }

        if (searchCriteria.getEnding() != null) {
            newLogs  = getLogsFilteredByEndingDate(newLogs, searchCriteria.getEnding());
        }

        if (searchCriteria.getLevel()!= null){
            newLogs = getLogsFilteredByLogLevel(newLogs, searchCriteria.getLevel());
        }

        if (searchCriteria.getLogFile()!= null){
            newLogs = getLogsFilteredByLogFile(newLogs, searchCriteria.getLogFile());
        }

        if (searchCriteria.getMethodName()!= null){
            newLogs = getLogsFilteredByMethodName(newLogs, searchCriteria.getMethodName());
        }

        if (searchCriteria.getClassFile()!= null){
            newLogs = getLogsFilteredByClassFile(newLogs, searchCriteria.getClassFile());
        }

        if (searchCriteria.getLine()!= null){
            newLogs = getLogsFilteredByLine(newLogs, searchCriteria.getLine());
        }

        if (searchCriteria.getClassName()!= null){
            newLogs = getLogsFilteredByClassName(newLogs, searchCriteria.getClassName());
        }

        if (searchCriteria.getMessage()!= null){
            newLogs = getLogsFilteredByMessage(newLogs, searchCriteria.getMessage());
        }

        return newLogs;
    }

    public List<Log> getLogsFilteredByStartingDate(List<Log> list, Long staringDate){

        return list
                .stream()
                .filter((log) -> log.getLogTimeStamp()>=staringDate)
                .collect(Collectors.toList());

    }

    public List<Log> getLogsFilteredByEndingDate(List<Log> list, Long endingDate){

        return list
                .stream()
                .filter((log) -> log.getLogTimeStamp()<=endingDate)
                .collect(Collectors.toList());

    }


    public List<Log> getLogsFilteredByLogLevel(List<Log> list, String logLevel){

         return list
                 .stream()
                 .filter((log) -> log.getLevel().toLowerCase().contains(logLevel.toLowerCase()))
                 .collect(Collectors.toList());

    }

    public List<Log> getLogsFilteredByLogFile(List<Log> list, String logFile){

        return list
                .stream()
                .filter((log) -> log.getLogFile().toLowerCase().contains(logFile.toLowerCase()))
                .collect(Collectors.toList());

    }

    public List<Log> getLogsFilteredByMethodName(List<Log> list, String methodName){

        return list
                .stream()
                .filter((log) -> log.getMethodName().toLowerCase().contains(methodName.toLowerCase()))
                .collect(Collectors.toList());

    }

    public List<Log> getLogsFilteredByClassFile(List<Log> list, String classFile){

        return list
                .stream()
                .filter((log) -> log.getClassFile().toLowerCase().contains(classFile.toLowerCase()))
                .collect(Collectors.toList());

    }

    public List<Log> getLogsFilteredByLine(List<Log> list, String line){

        return list
                .stream()
                .filter((log) -> log.getLine().toLowerCase().contains(line.toLowerCase()))
                .collect(Collectors.toList());

    }

    public List<Log> getLogsFilteredByClassName(List<Log> list, String classname){

        return list
                .stream()
                .filter((log) -> log.getClassName().toLowerCase().contains(classname.toLowerCase()))
                .collect(Collectors.toList());

    }

    public List<Log> getLogsFilteredByMessage(List<Log> list, String tokens){

        Set<String> tokenSet = new HashSet<String>(Arrays.asList(tokens.split("[^a-z^A-Z^0-9]")));
        return list
                .stream()
                .filter((log) -> {
                    Set<String> messageSet = new HashSet<String>(Arrays.asList(log.getMessage().split("[^a-z^A-Z^0-9]")));
                    if (messageSet.containsAll(tokenSet)){
                        return true;
                    }else return false;
                })
                .collect(Collectors.toList());

    }

    public List<Log> getPercentageMatchesOfMessage(List<Log> list, String tokens){

        Set<String> tokenSet = new HashSet<String>(Arrays.asList(tokens.split("[^a-z^A-Z^0-9]")));
        return list
                .stream()
                .filter((log) -> {
                    Set<String> messageSet = new HashSet<String>(Arrays.asList(log.getMessage().split("[^a-z^A-Z^0-9]")));
                    if (messageSet.containsAll(tokenSet)){
                        return true;
                    }else return false;
                })
                .collect(Collectors.toList());

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

