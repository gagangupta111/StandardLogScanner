package com.loganalyzer.util;

import com.loganalyzer.model.Condition;
import com.loganalyzer.model.Message;
import com.loganalyzer.model.Rule;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class XMLRulesParser {
    static final String RULES = "rules";
    static final String RULE = "rule";
    static final String NAME = "name";
    static final String DESCRIPTION = "description";
    static final String MESSAGE = "message";
    public static final String TOKEN = "token";
    public static final String REGEX = "regex";
    public static final String VAR = "var";
    static final String QUERY = "query";
    static final String ACTIONS = "actions";

    static final String CONDITIONS = "conditions";
    static final String CONDITION = "condition";
    static final String LEVEL = "level";
    static final String CLASSNAME = "className";
    static final String METHODNAME = "methodName";
    static final String CLASSFILE = "classFile";
    static final String LINE = "line";
    static final String LOGFILE = "logFile";

    @SuppressWarnings({ "unchecked", "null" })
    public List<Rule> readConfig(String configFile) throws Exception{

        String path = configFile;
        FileInputStream fis;
        List<Rule> rules = new ArrayList<>();
        Rule rule = null;
        List<Condition> conditions = new ArrayList<>();
        Condition condition = new Condition();
        Message message = new Message();

        try {
            fis = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            throw new Exception("rules.xml not found");
        }

        try {
            // First, create a new XMLInputFactory
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            // Setup a new eventReader
            InputStream in = new FileInputStream(configFile);
            XMLEventReader eventReader = inputFactory.createXMLEventReader(in);

            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();

                if (event.isStartElement()) {
                    StartElement startElement = event.asStartElement();
                    switch (startElement.getName().getLocalPart()){
                        case RULE:
                            rule = new Rule();
                            break;
                        case NAME:
                            rule.setRuleName(event.asCharacters().getData());
                            break;
                        case DESCRIPTION:
                            rule.setDesc(event.asCharacters().getData());
                            break;
                        case CONDITIONS:
                            conditions = new ArrayList<>();
                            break;
                            case CONDITION:
                                condition = new Condition();
                                break;
                            case LEVEL:
                                condition.setLevel(event.asCharacters().getData());
                                break;
                            case CLASSNAME:
                                condition.setClassName(event.asCharacters().getData());
                                break;
                            case METHODNAME:
                                condition.setMethodName(event.asCharacters().getData());
                                break;
                            case CLASSFILE:
                                condition.setClassFile(event.asCharacters().getData());
                                break;
                            case LINE:
                                condition.setLine(event.asCharacters().getData());
                                break;
                            case LOGFILE:
                                condition.setLogFile(event.asCharacters().getData());
                                break;
                        case MESSAGE:
                            message = new Message();
                            break;
                            case TOKEN:
                                message.addMessage(TOKEN, event.asCharacters().getData());
                                break;
                            case REGEX:
                                message.addMessage(REGEX, event.asCharacters().getData());
                                break;
                            case VAR:
                                message.addMessage(VAR, event.asCharacters().getData());
                                break;
                        case QUERY:
                            rule.setQuery(event.asCharacters().getData());
                            break;
                        case ACTIONS:
                            rule.setActions(event.asCharacters().getData());
                            break;
                    }

                }
                // If we reach the end of an item element, we add it to the list
                if (event.isEndElement()) {
                    EndElement endElement = event.asEndElement();
                    if (endElement.getName().getLocalPart().equals(RULE)) {
                        rules.add(rule);
                    }else if (endElement.getName().getLocalPart().equals(CONDITION)) {
                        conditions.add(condition);
                    }else if (endElement.getName().getLocalPart().equals(MESSAGE)) {
                        condition.setMessage(message);
                    }else if (endElement.getName().getLocalPart().equals(CONDITIONS)) {
                        rule.setConditions(conditions);
                    }
                }

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return rules;
    }

}