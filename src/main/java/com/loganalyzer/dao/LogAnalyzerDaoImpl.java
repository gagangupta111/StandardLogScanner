package com.loganalyzer.dao;

import com.loganalyzer.constants.Constants;
import com.loganalyzer.model.Log;
import com.loganalyzer.model.Rule;
import com.loganalyzer.model.SearchCriteria;
import com.loganalyzer.receiver.LogEventsGenerator;
import com.loganalyzer.util.Utility;
import com.loganalyzer.util.XMLRulesParser;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.PredicateUtils;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    private String logFilesPath;

    @Value("${range}")
    private Long range;

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
        rules = xmlRulesParser.readConfig(Constants.RULES_XML);

    }

    // This will populate logs only around the time stamp given in the input. By default it is 4 hours around that time stamp, otherwise the argument is mentioned in input.
    private void populateLogs() throws Exception {

        logFilesPath = appArgs.getNonOptionArgs().get(0);
        try {
            aroundDate = appArgs.getNonOptionArgs().get(1) + " " +  appArgs.getNonOptionArgs().get(2) + " " + appArgs.getNonOptionArgs().get(3);
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
        if (appArgs.getNonOptionArgs().size() > 4){
            millisecs = Long.parseLong(appArgs.getNonOptionArgs().get(4));
        }else {
            millisecs = range;
        }

        SearchCriteria criteria = new SearchCriteria();
        criteria.setStarting(parsedDate.getTime() - (millisecs));
        criteria.setEnding(parsedDate.getTime() + (millisecs));

        List<File> list = new ArrayList<File>();
        String[] compressedList = new String[] {"zip", "gz"};
        Collection<File> compressedFiles = FileUtils.listFiles(new File(logFilesPath), compressedList, true);
        for (File file1 : compressedFiles){
            String ext = FilenameUtils.getExtension(file1.getPath());
            if (compressedList[0].equals(ext)){
                String inputFile = file1.getAbsolutePath();
                String outputFolder = inputFile.substring(0, inputFile.lastIndexOf("\\"));
                Utility.unZipIt(inputFile, outputFolder);
            }else if (compressedList[1].equals(ext)){
                String inputFile = file1.getAbsolutePath();
                String outputFile = inputFile.substring(0, inputFile.lastIndexOf("."));
                Utility.gunzipIt(inputFile, outputFile);
            }
        }

        String[] extensions = new String[] { "log" };
        Collection<File> array = FileUtils.listFiles(new File(logFilesPath), extensions, true);

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
        Map<String, String> map;
        List<Log> originalLogs = logs;
        List<Log> logsA;
        List<Log> logsB;
        List<String> postfix = new ArrayList<>();

        Stack<List<Log>> stack = new Stack<>();
        for (Rule rule : rules){

            varIndexMap = new HashMap<>();
            varValueMap = new HashMap<>();
            stack = new Stack<>();
            map = new HashMap<>();
            logsA = new ArrayList<>();
            logsB = new ArrayList<>();

            try {
                postfix = Utility.infixToPostfixXML(rule.getQuery(), map);
            } catch (Exception e) {
                throw e;
            }

            for (String s: postfix){
                if ("&".equals(s)){
                    logsA = stack.pop();
                    logsB = stack.pop();
                    List<Log> copy = new ArrayList<>(logsA);
                    copy.retainAll(logsB);
                    stack.add(copy);
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
                    logsA = getLogsWithCriteria(originalLogs, rule.getConditionbyName(map.get(s)).mapToSearchCriteria());
                    stack.add(logsA);
                }
            }

            if (!stack.pop().isEmpty()){
                rulesResponse.put(rule.getRuleName(), rule.getActions());
            }
        }

        if (rulesResponse.isEmpty()){
            rulesResponse.put("NO_RULE_MATCHED", Constants.NO_RULE_MATCHED);
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

            }else throw new RuntimeException("Please mention message text in this format ! TOKEN: tokenstring, REGEX: regex, VAR:var1");
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
        return returnedList;

    }

}

