package com.loganalyzer.receiver;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.loganalyzer.dao.LogAnalyzerDaoImpl;
import com.loganalyzer.model.Log;
import com.loganalyzer.util.Utility;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.rule.ExpressionRule;
import org.apache.log4j.rule.Rule;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.apache.log4j.varia.LogFilePatternReceiver;

public class LogEventsGenerator extends LogFilePatternReceiver {

    private final List keywords = new ArrayList();
    private static final String PROP_START = "PROP(";
    private static final String PROP_END = ")";
    private static final String LOGGER = "LOGGER";
    private static final String MESSAGE = "MESSAGE";
    private static final String TIMESTAMP = "TIMESTAMP";
    private static final String NDC = "NDC";
    private static final String LEVEL = "LEVEL";
    private static final String THREAD = "THREAD";
    private static final String CLASS = "CLASS";
    private static final String FILE = "FILE";
    private static final String LINE = "LINE";
    private static final String METHOD = "METHOD";
    private static final String NEWLINE = "(NL)";
    private static final String DEFAULT_HOST = "file";
    private static final String EXCEPTION_PATTERN = "^\\s+at.*";
    private static final String REGEXP_DEFAULT_WILDCARD = ".*?";
    private static final String REGEXP_GREEDY_WILDCARD = ".*";
    private static final String PATTERN_WILDCARD = "*";
    private static final String NOSPACE_GROUP = "(\\s*?\\S*?\\s*?)";
    private static final String DEFAULT_GROUP = "(.*?)";
    private static final String GREEDY_GROUP = "(.*)";
    private static final String MULTIPLE_SPACES_REGEXP = "[ ]+";
    private static final String NEWLINE_REGEXP = "\n";
    private final String newLine = System.getProperty("line.separator");
    private final String[] emptyException = new String[]{""};
    private SimpleDateFormat dateFormat;
    private String timestampFormat;
    private String logFormat;
    private String customLevelDefinitions;
    private String fileURL;
    private String host;
    private String path;
    private boolean tailing;
    private String filterExpression;
    private long waitMillis = 2000L;
    private String group;
    private static final String VALID_DATEFORMAT_CHARS = "GyYMwWDdFEuaHkKhmsSzZX";
    private static final String VALID_DATEFORMAT_CHAR_PATTERN = "[GyYMwWDdFEuaHkKhmsSzZX]";
    private Rule expressionRule;
    private Map currentMap;
    private List additionalLines;
    private List matchingKeywords;
    private String regexp;
    private Reader reader;
    private Pattern regexpPattern;
    private Pattern exceptionPattern;
    private String timestampPatternText;
    private boolean useCurrentThread;
    public static final int MISSING_FILE_RETRY_MILLIS = 10000;
    private boolean appendNonMatches;
    private final Map customLevelDefinitionMap = new HashMap();
    private int lineCount = 1;

    private List<Log> logs;

    public LogEventsGenerator(List<Log> logs) {
        this.keywords.add("TIMESTAMP");
        this.keywords.add("LOGGER");
        this.keywords.add("LEVEL");
        this.keywords.add("THREAD");
        this.keywords.add("CLASS");
        this.keywords.add("FILE");
        this.keywords.add("LINE");
        this.keywords.add("METHOD");
        this.keywords.add("MESSAGE");
        this.keywords.add("NDC");

        this.logs = logs;

        try {
            this.exceptionPattern = Pattern.compile("^\\s+at.*");
        } catch (PatternSyntaxException var2) {
            ;
        }

    }

    public String getFileURL() {
        return this.fileURL;
    }

    public void setFileURL(String fileURL) {
        this.fileURL = fileURL;
    }

    public void setCustomLevelDefinitions(String customLevelDefinitions) {
        this.customLevelDefinitions = customLevelDefinitions;
    }

    public String getCustomLevelDefinitions() {
        return this.customLevelDefinitions;
    }

    public boolean isAppendNonMatches() {
        return this.appendNonMatches;
    }

    public void setAppendNonMatches(boolean appendNonMatches) {
        this.appendNonMatches = appendNonMatches;
    }

    public String getFilterExpression() {
        return this.filterExpression;
    }

    public void setFilterExpression(String filterExpression) {
        this.filterExpression = filterExpression;
    }

    public boolean isTailing() {
        return this.tailing;
    }

    public void setTailing(boolean tailing) {
        this.tailing = tailing;
    }

    public String getLogFormat() {
        return this.logFormat;
    }

    public void setLogFormat(String logFormat) {
        this.logFormat = logFormat;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroup() {
        return this.group;
    }

    public void setTimestampFormat(String timestampFormat) {
        this.timestampFormat = timestampFormat;
    }

    public String getTimestampFormat() {
        return this.timestampFormat;
    }

    public long getWaitMillis() {
        return this.waitMillis;
    }

    public void setWaitMillis(long waitMillis) {
        this.waitMillis = waitMillis;
    }

    private int getExceptionLine() {
        for(int i = 0; i < this.additionalLines.size(); ++i) {
            Matcher exceptionMatcher = this.exceptionPattern.matcher((String)this.additionalLines.get(i));
            if (exceptionMatcher.matches()) {
                return i;
            }
        }

        return -1;
    }

    private String buildMessage(String firstMessageLine, int exceptionLine) {
        if (this.additionalLines.size() == 0) {
            return firstMessageLine;
        } else {
            StringBuffer message = new StringBuffer();
            if (firstMessageLine != null) {
                message.append(firstMessageLine);
            }

            int linesToProcess = exceptionLine == -1 ? this.additionalLines.size() : exceptionLine;

            for(int i = 0; i < linesToProcess; ++i) {
                message.append(this.newLine);
                message.append(this.additionalLines.get(i));
            }

            return message.toString();
        }
    }

    private String[] buildException(int exceptionLine) {
        if (exceptionLine == -1) {
            return this.emptyException;
        } else {
            String[] exception = new String[this.additionalLines.size() - exceptionLine - 1];

            for(int i = 0; i < exception.length; ++i) {
                exception[i] = (String)this.additionalLines.get(i + exceptionLine);
            }

            return exception;
        }
    }

    private LoggingEvent buildEvent() {
        if (this.currentMap.size() != 0) {
            int exceptionLine = this.getExceptionLine();
            String[] exception = this.buildException(exceptionLine);
            if (this.additionalLines.size() > 0 && exception.length > 0) {
                this.currentMap.put("MESSAGE", this.buildMessage((String)this.currentMap.get("MESSAGE"), exceptionLine));
            }

            LoggingEvent event = this.convertToEvent(this.currentMap, exception);
            this.currentMap.clear();
            this.additionalLines.clear();
            return event;
        } else {
            if (this.additionalLines.size() > 0) {
                Iterator iter = this.additionalLines.iterator();

                while(iter.hasNext()) {
                    this.getLogger().debug("found non-matching line: " + iter.next());
                }
            }

            this.additionalLines.clear();
            return null;
        }
    }

    protected void process(BufferedReader bufferedReader) throws IOException {
        String line;
        LoggingEvent event;
        while((line = bufferedReader.readLine()) != null) {
            for(int i = 1; i < this.lineCount; ++i) {
                String thisLine = bufferedReader.readLine();
                if (thisLine != null) {
                    line = line + this.newLine + thisLine;
                }
            }

            Matcher eventMatcher = this.regexpPattern.matcher(line);
            if (!line.trim().equals("")) {
                Matcher exceptionMatcher = this.exceptionPattern.matcher(line);
                if (eventMatcher.matches()) {
                    event = this.buildEvent();
                    if (event != null && this.passesExpression(event)) {
                        this.doPost(event);
                    }

                    this.currentMap.putAll(this.processEvent(eventMatcher.toMatchResult()));
                } else if (exceptionMatcher.matches()) {
                    this.additionalLines.add(line);
                } else if (this.appendNonMatches) {
                    String lastTime = (String)this.currentMap.get("TIMESTAMP");
                    if (this.currentMap.size() > 0) {
                        LoggingEvent anotherEvent = this.buildEvent();
                        if (anotherEvent != null && this.passesExpression(anotherEvent)) {
                            this.doPost(anotherEvent);
                        }
                    }

                    if (lastTime != null) {
                        this.currentMap.put("TIMESTAMP", lastTime);
                    }

                    this.currentMap.put("MESSAGE", line);
                } else {
                    this.additionalLines.add(line);
                }
            }
        }

        event = this.buildEvent();
        if (event != null && this.passesExpression(event)) {
            this.doPost(event);
        }

    }

    protected void createPattern() {
        this.regexpPattern = Pattern.compile(this.regexp);
    }

    private boolean passesExpression(LoggingEvent event) {
        return event != null && this.expressionRule != null ? this.expressionRule.evaluate(event, (Map)null) : true;
    }

    private Map processEvent(MatchResult result) {
        Map map = new HashMap();

        for(int i = 1; i < result.groupCount() + 1; ++i) {
            Object key = this.matchingKeywords.get(i - 1);
            Object value = result.group(i);
            map.put(key, value);
        }

        return map;
    }

    private String convertTimestamp() {
        String result = "";
        if (this.timestampFormat != null) {
            result = this.timestampFormat.replaceAll(Pattern.quote("+"), "[+]");
            result = result.replaceAll("[GyYMwWDdFEuaHkKhmsSzZX]", "\\\\S+");
            result = result.replaceAll(Pattern.quote("."), "\\\\.");
        }

        return result;
    }

    protected void setHost(String host) {
        this.host = host;
    }

    protected void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }

    protected void initialize() {

        if (this.host == null && this.path == null) {
            try {
                URL url = new URL(this.fileURL);
                this.host = url.getHost();
                this.path = url.getPath();
            } catch (MalformedURLException var11) {
                var11.printStackTrace();
            }
        }

        if (this.host == null || this.host.trim().equals("")) {
            this.host = "file";
        }

        if (this.path == null || this.path.trim().equals("")) {
            this.path = this.fileURL;
        }

        this.currentMap = new HashMap();
        this.additionalLines = new ArrayList();
        this.matchingKeywords = new ArrayList();
        if (this.timestampFormat != null) {
            this.dateFormat = new SimpleDateFormat(this.quoteTimeStampChars(this.timestampFormat));
            this.timestampPatternText = this.convertTimestamp();
        }

        this.updateCustomLevelDefinitionMap();

        try {
            if (this.filterExpression != null) {
                this.expressionRule = ExpressionRule.getRule(this.filterExpression);
            }
        } catch (Exception var10) {
            this.getLogger().warn("Invalid filter expression: " + this.filterExpression, var10);
        }

        List buildingKeywords = new ArrayList();
        String newPattern = this.logFormat;
        int index = 0;

        while(index > -1) {
            index = newPattern.indexOf("(NL)");
            if (index > -1) {
                ++this.lineCount;
                newPattern = this.singleReplace(newPattern, "(NL)", "\n");
            }
        }

        String current = newPattern;
        List propertyNames = new ArrayList();
        index = 0;

        while(true) {
            String buildingInt;
            while(index > -1) {
                if (current.indexOf("PROP(") > -1 && current.indexOf(")") > -1) {
                    index = current.indexOf("PROP(");
                    String longPropertyName = current.substring(current.indexOf("PROP("), current.indexOf(")") + 1);
                    buildingInt = this.getShortPropertyName(longPropertyName);
                    buildingKeywords.add(buildingInt);
                    propertyNames.add(longPropertyName);
                    current = current.substring(longPropertyName.length() + 1 + index);
                    newPattern = this.singleReplace(newPattern, longPropertyName, (new Integer(buildingKeywords.size() - 1)).toString());
                } else {
                    index = -1;
                }
            }

            Iterator iter = this.keywords.iterator();

            int i;
            while(iter.hasNext()) {
                buildingInt = (String)iter.next();
                i = newPattern.indexOf(buildingInt);
                if (i > -1) {
                    buildingKeywords.add(buildingInt);
                    newPattern = this.singleReplace(newPattern, buildingInt, (new Integer(buildingKeywords.size() - 1)).toString());
                }
            }

            buildingInt = "";

            String keyword;
            for(i = 0; i < newPattern.length(); ++i) {
                keyword = String.valueOf(newPattern.substring(i, i + 1));
                if (this.isInteger(keyword)) {
                    buildingInt = buildingInt + keyword;
                } else {
                    if (this.isInteger(buildingInt)) {
                        this.matchingKeywords.add(buildingKeywords.get(Integer.parseInt(buildingInt)));
                    }

                    buildingInt = "";
                }
            }

            if (this.isInteger(buildingInt)) {
                this.matchingKeywords.add(buildingKeywords.get(Integer.parseInt(buildingInt)));
            }

            newPattern = this.replaceMetaChars(newPattern);
            newPattern = newPattern.replaceAll("[ ]+", "[ ]+");
            newPattern = newPattern.replaceAll(Pattern.quote("*"), ".*?");

            for(i = 0; i < buildingKeywords.size(); ++i) {
                keyword = (String)buildingKeywords.get(i);
                if (i == buildingKeywords.size() - 1) {
                    newPattern = this.singleReplace(newPattern, String.valueOf(i), "(.*)");
                } else if ("TIMESTAMP".equals(keyword)) {
                    newPattern = this.singleReplace(newPattern, String.valueOf(i), "(" + this.timestampPatternText + ")");
                } else if (!"LOGGER".equals(keyword) && !"LEVEL".equals(keyword)) {
                    newPattern = this.singleReplace(newPattern, String.valueOf(i), "(.*?)");
                } else {
                    newPattern = this.singleReplace(newPattern, String.valueOf(i), "(\\s*?\\S*?\\s*?)");
                }
            }

            this.regexp = newPattern;
            this.getLogger().debug("regexp is " + this.regexp);
            return;
        }
    }

    private void updateCustomLevelDefinitionMap() {
        if (this.customLevelDefinitions != null) {
            StringTokenizer entryTokenizer = new StringTokenizer(this.customLevelDefinitions, ",");
            this.customLevelDefinitionMap.clear();

            while(entryTokenizer.hasMoreTokens()) {
                StringTokenizer innerTokenizer = new StringTokenizer(entryTokenizer.nextToken(), "=");
                this.customLevelDefinitionMap.put(innerTokenizer.nextToken(), Level.toLevel(innerTokenizer.nextToken()));
            }
        }

    }

    private boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException var3) {
            return false;
        }
    }

    private String quoteTimeStampChars(String input) {
        StringBuffer result = new StringBuffer();
        boolean lastCharIsDateFormat = false;

        for(int i = 0; i < input.length(); ++i) {
            String thisVal = input.substring(i, i + 1);
            boolean thisCharIsDateFormat = "GyYMwWDdFEuaHkKhmsSzZX".contains(thisVal);
            if (!thisCharIsDateFormat && (i == 0 || lastCharIsDateFormat)) {
                result.append("'");
            }

            if (thisCharIsDateFormat && i > 0 && !lastCharIsDateFormat) {
                result.append("'");
            }

            lastCharIsDateFormat = thisCharIsDateFormat;
            result.append(thisVal);
        }

        if (!lastCharIsDateFormat) {
            result.append("'");
        }

        return result.toString();
    }

    private String singleReplace(String inputString, String oldString, String newString) {
        int propLength = oldString.length();
        int startPos = inputString.indexOf(oldString);
        if (startPos == -1) {
            this.getLogger().info("string: " + oldString + " not found in input: " + inputString + " - returning input");
            return inputString;
        } else {
            if (startPos == 0) {
                inputString = inputString.substring(propLength);
                inputString = newString + inputString;
            } else {
                inputString = inputString.substring(0, startPos) + newString + inputString.substring(startPos + propLength);
            }

            return inputString;
        }
    }

    private String getShortPropertyName(String longPropertyName) {
        String currentProp = longPropertyName.substring(longPropertyName.indexOf("PROP("));
        String prop = currentProp.substring(0, currentProp.indexOf(")") + 1);
        String shortProp = prop.substring("PROP(".length(), prop.length() - 1);
        return shortProp;
    }

    private String replaceMetaChars(String input) {
        input = input.replaceAll("\\\\", "\\\\\\");
        input = input.replaceAll(Pattern.quote("]"), "\\\\]");
        input = input.replaceAll(Pattern.quote("["), "\\\\[");
        input = input.replaceAll(Pattern.quote("^"), "\\\\^");
        input = input.replaceAll(Pattern.quote("$"), "\\\\$");
        input = input.replaceAll(Pattern.quote("."), "\\\\.");
        input = input.replaceAll(Pattern.quote("|"), "\\\\|");
        input = input.replaceAll(Pattern.quote("?"), "\\\\?");
        input = input.replaceAll(Pattern.quote("+"), "\\\\+");
        input = input.replaceAll(Pattern.quote("("), "\\\\(");
        input = input.replaceAll(Pattern.quote(")"), "\\\\)");
        input = input.replaceAll(Pattern.quote("-"), "\\\\-");
        input = input.replaceAll(Pattern.quote("{"), "\\\\{");
        input = input.replaceAll(Pattern.quote("}"), "\\\\}");
        input = input.replaceAll(Pattern.quote("#"), "\\\\#");
        return input;
    }

    private LoggingEvent convertToEvent(Map fieldMap, String[] exception) {
        if (fieldMap == null) {
            return null;
        } else {
            if (!fieldMap.containsKey("LOGGER")) {
                fieldMap.put("LOGGER", "Unknown");
            }

            if (exception == null) {
                exception = this.emptyException;
            }

            Logger logger = null;
            long timeStamp = 0L;
            String level = null;
            String threadName = null;
            Object message = null;
            String ndc = null;
            String className = null;
            String methodName = null;
            String eventFileName = null;
            String lineNumber = null;
            Hashtable properties = new Hashtable();
            logger = Logger.getLogger((String)fieldMap.remove("LOGGER"));
            if (this.dateFormat != null && fieldMap.containsKey("TIMESTAMP")) {
                try {
                    timeStamp = this.dateFormat.parse((String)fieldMap.remove("TIMESTAMP")).getTime();
                } catch (Exception var18) {
                    var18.printStackTrace();
                }
            }

            if (timeStamp == 0L) {
                timeStamp = System.currentTimeMillis();
            }

            message = fieldMap.remove("MESSAGE");
            if (message == null) {
                message = "";
            }

            level = (String)fieldMap.remove("LEVEL");
            Level levelImpl;
            if (level == null) {
                levelImpl = Level.DEBUG;
            } else {
                levelImpl = (Level)this.customLevelDefinitionMap.get(level);
                if (levelImpl == null) {
                    levelImpl = Level.toLevel(level.trim());
                    if (!level.equals(levelImpl.toString()) && levelImpl == null) {
                        levelImpl = Level.DEBUG;
                        this.getLogger().debug("found unexpected level: " + level + ", logger: " + logger.getName() + ", msg: " + message);
                        message = level + " " + message;
                    }
                }
            }

            threadName = (String)fieldMap.remove("THREAD");
            ndc = (String)fieldMap.remove("NDC");
            className = (String)fieldMap.remove("CLASS");
            methodName = (String)fieldMap.remove("METHOD");
            eventFileName = (String)fieldMap.remove("FILE");
            lineNumber = (String)fieldMap.remove("LINE");
            properties.put("hostname", this.host);
            properties.put("application", this.path);
            properties.put("receiver", this.getName());
            if (this.group != null) {
                properties.put("group", this.group);
            }

            properties.putAll(fieldMap);
            LocationInfo info = null;
            if (eventFileName == null && className == null && methodName == null && lineNumber == null) {
                info = LocationInfo.NA_LOCATION_INFO;
            } else {
                info = new LocationInfo(eventFileName, className, methodName, lineNumber);
            }

            LoggingEvent event = new LoggingEvent((String)null, logger, timeStamp, levelImpl, message, threadName, new ThrowableInformation(exception), ndc, info, properties);
            return event;
        }
    }

    public void shutdown() {
        this.getLogger().info(this.getPath() + " shutdown");
        this.active = false;

        try {
            if (this.reader != null) {
                this.reader.close();
                this.reader = null;
            }
        } catch (IOException var2) {
            var2.printStackTrace();
        }

    }

    public void activateOptions() {
        this.getLogger().info("activateOptions");
        this.active = true;
        Runnable runnable = new Runnable() {
            public void run() {
                LogEventsGenerator.this.initialize();

                while(LogEventsGenerator.this.reader == null) {
                    LogEventsGenerator.this.getLogger().info("attempting to load file: " + LogEventsGenerator.this.getFileURL());

                    try {
                        LogEventsGenerator.this.reader = new InputStreamReader((new URL(LogEventsGenerator.this.getFileURL())).openStream(), "UTF-8");
                    } catch (FileNotFoundException var10) {
                        LogEventsGenerator.this.getLogger().info("file not available - will try again");
                        synchronized(this) {
                            try {
                                this.wait(10000L);
                            } catch (InterruptedException var8) {
                                ;
                            }
                        }
                    } catch (IOException var11) {
                        LogEventsGenerator.this.getLogger().warn("unable to load file", var11);
                        return;
                    }
                }

                try {
                    BufferedReader bufferedReader = new BufferedReader(LogEventsGenerator.this.reader);
                    LogEventsGenerator.this.createPattern();

                    do {
                        LogEventsGenerator.this.process(bufferedReader);

                        try {
                            synchronized(this) {
                                this.wait(LogEventsGenerator.this.waitMillis);
                            }
                        } catch (InterruptedException var7) {
                            ;
                        }

                        if (LogEventsGenerator.this.tailing) {
                            LogEventsGenerator.this.getLogger().debug("tailing file");
                        }
                    } while(LogEventsGenerator.this.tailing);
                } catch (IOException var12) {
                    LogEventsGenerator.this.getLogger().info("stream closed");
                }

                LogEventsGenerator.this.getLogger().debug("processing " + LogEventsGenerator.this.path + " complete");
                LogEventsGenerator.this.shutdown();
            }
        };
        if (this.useCurrentThread) {
            runnable.run();
        } else {
            (new Thread(runnable, "LogFilePatternReceiver-" + this.getName())).start();
        }

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
        log.setMessage(event.getMessage().toString().trim());

        this.logs.add(log);
    }
}

