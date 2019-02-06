package com.loganalyzer.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loganalyzer.model.Log;
import com.loganalyzer.model.Rule;
import com.loganalyzer.model.RuleCriteria;
import com.loganalyzer.model.SearchCriteria;
import com.loganalyzer.receiver.LogEventsGenerator;
import com.loganalyzer.util.JsonDateDeSerializer;
import com.loganalyzer.util.Utility;
import com.loganalyzer.util.XMLRulesParser;
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
import javax.rmi.CORBA.Util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
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

    private List<Log> logs = new ArrayList<>();
    private List<Rule> rules = new ArrayList<>();
    private Map<Integer, String> varIndexMap = new HashMap<>();
    private Map<String, String> varValueMap = new HashMap<>();
    boolean sortedFlag = false;

    @Autowired
    ApplicationArguments appArgs;

    private String aroundDate;

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

    private void populateXMLRules() throws Exception{

        XMLRulesParser xmlRulesParser = new XMLRulesParser();
        rules = xmlRulesParser.readConfig("/rules.xml");

    }

    /*
    private void populateNewRules() throws Exception{

        XSSFWorkbook myWorkBook;
        InputStream in = getClass().getResourceAsStream("/rules2.xlsx");
        if (in != null){
            myWorkBook = new XSSFWorkbook(in);
        }else {
            String path = "./rules2.xlsx";
            FileInputStream fis;
            try {
                fis = new FileInputStream(path);
            } catch (FileNotFoundException e) {
                throw new Exception("rules2.xlsx not found");
            }


            try {
                myWorkBook = new XSSFWorkbook(fis);
            } catch (IOException e) {
                throw new Exception("format of rules.xlsx is not correct");
            }
        }
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
*/

    // This will populate logs only around the time stamp given in the input. By default it is 4 hours around that time stamp, otherwise the argument is mentioned in input.
    private void populateLogs() throws Exception {

        try {
            aroundDate = appArgs.getNonOptionArgs().get(0) + " " +  appArgs.getNonOptionArgs().get(1) + " " + appArgs.getNonOptionArgs().get(2);
        }catch (Exception e){
            throw new Exception("Please mentioned a date as an argument to the application");
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-dd EEE HH:mm:ss.SSS",Locale.ENGLISH);
        java.util.Date parsedDate = null;
        try {
            parsedDate = dateFormat.parse(aroundDate);
        } catch (ParseException e) {
            throw new Exception("Please mentioned date in this format yyyy-MMM-dd DAY HH:mm:ss.SSS");
        }

        Long millisecs = null;
        if (appArgs.getNonOptionArgs().size() > 3){
            millisecs = Long.parseLong(appArgs.getNonOptionArgs().get(3));
        }else {
            millisecs = 120L;
        }

        SearchCriteria criteria = new SearchCriteria();
        criteria.setStarting(parsedDate.getTime() - (millisecs));
        criteria.setEnding(parsedDate.getTime() + (millisecs));

        List<File> list = new ArrayList<File>();
        String[] compressedList = new String[] {"zip", "gz"};
        Collection<File> compressedFiles = FileUtils.listFiles(new File("."), compressedList, true);
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
        Collection<File> array = FileUtils.listFiles(new File("."), extensions, true);

        String format;
        for (File file : array){

            String fileName = Utility.getFileName(file.getPath());
            boolean found = filesWithFormatPatternNoLocation
                    .stream()
                    .filter((string) -> fileName.contains(string)).findFirst().isPresent();
            if (found){
                generator(formatPatternNoLocation, file, criteria);
            }else {
                found = filesWithFormatPattern
                        .stream()
                        .filter((string) -> fileName.contains(string)).findFirst().isPresent();
                if (found) {
                    generator(formatPattern, file, criteria);
                }
            }
        }
    }

    @PostConstruct
    public void initialize() throws Exception {

        ListUtils.predicatedList(logs, PredicateUtils.notNullPredicate());
        populateLogs();
        populateXMLRules();
    }

    private void generator(String format, File file, SearchCriteria criteria){
        LogEventsGenerator receiver = new LogEventsGenerator(logs, criteria);
        receiver.setTimestampFormat(timestamp);
        receiver.setLogFormat(format);
        receiver.setFileURL("file:///" + file.getAbsolutePath());
        receiver.setTailing(false);
        receiver.activateOptions();
    }

    @Override
    public List<Log> getAllLogs() {
        if (!sortedFlag){
            logs.removeAll(Collections.singleton(null));
            Collections.sort(logs);
        }
        return logs;
    }

    @Override
    public List<Rule> getAllRules(){
        if (!sortedFlag){
            logs.removeAll(Collections.singleton(null));
            Collections.sort(logs);
        }
        return rules;
    }

    public Map<String, String> checkAllRules() throws Exception {

        Map<String, String> rulesResponse = new HashMap<>();
        Map<String, SearchCriteria> mapSearchCriteria;
        List<Log> originalLogs = logs;
        List<Log> logsA;
        List<Log> logsB;
        List<String> postfix = new ArrayList<>();

        Stack<List<Log>> stack = new Stack<>();
        for (Rule rule : rules){

            varIndexMap = new HashMap<>();
            varValueMap = new HashMap<>();
            stack = new Stack<>();
            mapSearchCriteria = new HashMap<>();
            logsA = new ArrayList<>();
            logsB = new ArrayList<>();

            try {
                postfix = Utility.infixToPostfixXML(rule.getQuery());
            } catch (Exception e) {
                throw e;
            }

            for (String s: postfix){
                if ("&".equals(s)){
                    logsA = stack.pop();
                    logsA.retainAll(stack.pop());
                    stack.add(logsA);
                }else if ("|".equals(s)){
                    logsA = stack.pop();
                    logsB = stack.pop();
                    List<Log> copy = new ArrayList<>(logsB);
                    copy.removeAll(logsA);
                    logsA.addAll(copy);
                    stack.add(logsA);
                }else if (s.contains("&")){
                    Long interval = Long.parseLong(s.substring(1, s.length()));
                    if (interval > 0){
                        logsB = stack.pop();
                        logsA = stack.pop();
                    }else if (interval < 0){
                        logsA = stack.pop();
                        logsB = stack.pop();
                        interval = Math.abs(interval);
                    }else {
                        logsA = stack.pop();
                        logsA.retainAll(stack.pop());
                        stack.add(logsA);
                        continue;
                    }
                    // till now it looks like : logsA AND AFTER 7890L logsB
                    if (logsA.isEmpty() || logsB.isEmpty()
                            || ( logsA.get(logsA.size() - 1).getLogTimeStamp() > logsB.get(0).getLogTimeStamp()
                                && logsA.get(logsA.size() - 1).getLogTimeStamp() - logsB.get(0).getLogTimeStamp() > interval)){
                        stack.add(new ArrayList<>());
                    }else if (logsB.get(0).getLogTimeStamp() - logsA.get(logsA.size() - 1).getLogTimeStamp() <= interval){
                        List<Log> newList = new ArrayList<>();
                        newList.add(logsA.get(logsA.size() - 1));
                        newList.add(logsB.get(0));
                        stack.add(newList);
                    }else {

                        Log searchFor = logsB.get(0).clone();
                        searchFor.setLogTimeStamp(searchFor.getLogTimeStamp() - interval);
                        int x = Collections.binarySearch(logsA, searchFor);

                        if (x > 0){
                            logsA = logsA.subList(x, logsA.size() - 1);
                        }else if (x < 0 && (Math.abs(x) > 0 && Math.abs(x) < logsA.size() - 1)){
                            logsA = logsA.subList(Math.abs(x) - 1, logsA.size() - 1);
                        }

                        searchFor = logsA.get(logsA.size() - 1).clone();
                        searchFor.setLogTimeStamp(searchFor.getLogTimeStamp() + interval);
                        x = Collections.binarySearch(logsB, searchFor);
                        if (x > 0){
                            logsB = logsB.subList(0, x);
                        }else if (x < 0 && (Math.abs(x) > 0 && Math.abs(x) < logsB.size())){
                            logsB = logsB.subList(0, Math.abs(x));
                        }

                        List<Log> newList = new ArrayList<>();
                        for (Log a : logsA){
                            for (Log b: logsB){
                                if (a.getLogTimeStamp() > b.getLogTimeStamp() && a.getLogTimeStamp() - b.getLogTimeStamp() <= interval){
                                    newList.add(a);
                                    newList.add(b);
                                }
                            }
                        }
                        stack.add(newList);
                    }
                } else {

                    logsA = getLogsWithCriteria(originalLogs, rule.getConditionbyName(s).mapToSearchCriteria());
                    stack.add(logsA);
                }
            }

            if (!stack.pop().isEmpty()){
                rulesResponse.put(rule.getRuleName(), rule.getActions());
            }
        }

        return rulesResponse;
    }

    @Override
    public List<Log> getLogsWithCriteria(SearchCriteria searchCriteria){

        if (!sortedFlag){
            logs.removeAll(Collections.singleton(null));
            Collections.sort(logs);
        }
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

        if (list == null || list.isEmpty()){
            return list;
        }

        Log searchLog = new Log();
        searchLog.setLogTimeStamp(staringDate);

        int x = Collections.binarySearch(list, searchLog);

        if (x > 0){
            list = list.subList(x, list.size());
        }else if (x < 0){
            x = Math.abs(x) - 1;
            list = list.subList(x, list.size());
        }

        return list;

    }

    public List<Log> getLogsFilteredByEndingDate(List<Log> list, Long endingDate){

        if (list == null || list.isEmpty()){
            return list;
        }
        Log searchLog = new Log();
        searchLog.setLogTimeStamp(endingDate);

        int x = Collections.binarySearch(list, searchLog);

        if (x > 0){
            list = list.subList(0, x);
        }else if (x < 0){
            x = Math.abs(x) - 1;
            list = list.subList(0, x);
        }

        return list;

    }


    public List<Log> getLogsFilteredByLogLevel(List<Log> list, String logLevel){

        if (list == null || list.isEmpty()){
            return list;
        }
         return list
                 .stream()
                 .filter((log) -> log.getLevel().toLowerCase().contains(logLevel.toLowerCase()))
                 .collect(Collectors.toList());

    }

    public List<Log> getLogsFilteredByLogFile(List<Log> list, String logFile){

        if (list == null || list.isEmpty()){
            return list;
        }
        return list
                .stream()
                .filter((log) -> log.getLogFile().toLowerCase().contains(logFile.toLowerCase()))
                .collect(Collectors.toList());

    }

    public List<Log> getLogsFilteredByMethodName(List<Log> list, String methodName){

        if (list == null || list.isEmpty()){
            return list;
        }
        return list
                .stream()
                .filter((log) -> log.getMethodName().toLowerCase().contains(methodName.toLowerCase()))
                .collect(Collectors.toList());

    }

    public List<Log> getLogsFilteredByClassFile(List<Log> list, String classFile){

        if (list == null || list.isEmpty()){
            return list;
        }
        return list
                .stream()
                .filter((log) -> log.getClassFile().toLowerCase().contains(classFile.toLowerCase()))
                .collect(Collectors.toList());

    }

    public List<Log> getLogsFilteredByLine(List<Log> list, String line){

        if (list == null || list.isEmpty()){
            return list;
        }
        return list
                .stream()
                .filter((log) -> log.getLine().toLowerCase().contains(line.toLowerCase()))
                .collect(Collectors.toList());

    }

    public List<Log> getLogsFilteredByClassName(List<Log> list, String classname){

        if (list == null || list.isEmpty()){
            return list;
        }
        return list
                .stream()
                .filter((log) -> log.getClassName().toLowerCase().contains(classname.toLowerCase()))
                .collect(Collectors.toList());

    }

    public List<Log> getLogsFilteredByMessage(List<Log> list, String tokens){

        Long start = new Date().getTime();
        if (list == null || list.isEmpty()){
            return list;
        }

        String regex  = "";
        int count = 0;
        String[] splitted = tokens.split(", ");
        for (int i = 0; i < splitted.length; i++){

            String s = splitted[i];
            System.out.println(s);
            if (s.charAt(0) == 'T') {
                regex = regex + ".*" + s.substring(s.indexOf(':') + 1, s.length()).trim();
            }else if (s.charAt(0) == 'R'){
                regex = regex + ".*(" + s.substring(s.indexOf(':')+1, s.length()).trim() + ")";
            }else if (s.charAt(0) == 'V'){
                String varName = s.substring(s.indexOf(':')+1, s.length()).trim();
                if (varName.contains("{")){
                    String varValue = varValueMap.get(varName.replaceAll("[{}]", ""));
                    if (varValue == null){
                        return new ArrayList<>();
                    }else {
                        regex = regex + ".*" + varValueMap.get(varName.replaceAll("[{}]", "")).trim();
                    }
                }else {
                    String tempVarName = s.substring(s.indexOf(':')+1, s.length()).trim();
                    if (varIndexMap.values().contains(tempVarName)){
                        throw new RuntimeException("Sorry! " + tempVarName + "Variable name is used already!");
                    }
                    varIndexMap.put(++count, tempVarName);
                }

            }else throw new RuntimeException("Rule is not in expected format!");
        }

        if (regex.charAt(0) == '.'){
            regex = regex.substring(2, regex.length());
        }
        String finalRegex = regex;
        Pattern pattern = Pattern.compile(finalRegex);

        List<Log> returnedList = list
                .stream()
                .filter((log) -> {

                    Matcher matcher = pattern.matcher(log.getMessage());
                    if (matcher.find()) {
                        for (Integer integer : varIndexMap.keySet()){
                            try{
                                varValueMap.put(varIndexMap.get(integer), matcher.group(integer));
                            }catch (Exception e){}
                        }
                        return true;
                    }else {
                        return false;
                    }

                })
                .collect(Collectors.toList());
        Long end = new Date().getTime();
        System.out.println(" REGEX: " + finalRegex);
        System.out.println(" Time Taken: " + (end - start));
        return returnedList;

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

