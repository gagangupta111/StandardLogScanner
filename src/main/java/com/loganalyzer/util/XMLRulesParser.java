package com.loganalyzer.util;

import com.loganalyzer.model.Condition;
import com.loganalyzer.model.Message;
import com.loganalyzer.model.Rule;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
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

    public List<Rule> readConfig(String configFile) throws Exception{

        List<Rule> rules = new ArrayList<>();
        Rule rule = null;
        List<Condition> conditions = new ArrayList<>();
        Condition condition = new Condition();
        Message message = new Message();

        InputStream in = getClass().getResourceAsStream("/rules.xml");
        if (in == null) {
            try {
                in = new FileInputStream(configFile);
            } catch (FileNotFoundException e) {
                throw new Exception("rules.xml not found");
            } catch (Exception e){
                throw e;
            }
        }

        try {

            Utility.validateXMLSchema("/rules.xml");
            // First, create a new XMLInputFactory
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
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
                            event = eventReader.nextEvent();
                            rule.setRuleName(event.asCharacters().getData());
                            break;
                        case DESCRIPTION:
                            event = eventReader.nextEvent();
                            rule.setDesc(event.asCharacters().getData());
                            break;
                        case CONDITIONS:
                            conditions = new ArrayList<>();
                            break;
                            case CONDITION:
                                condition = new Condition();
                                Iterator<Attribute> attributes = startElement.getAttributes();
                                while (attributes.hasNext()) {
                                    Attribute attribute = attributes.next();
                                    if (attribute.getName().toString().equals(NAME)) {
                                        condition.setName(attribute.getValue());
                                    }
                                }
                                break;
                            case LEVEL:
                                event = eventReader.nextEvent();
                                condition.setLevel(event.asCharacters().getData());
                                break;
                            case CLASSNAME:
                                event = eventReader.nextEvent();
                                condition.setClassName(event.asCharacters().getData());
                                break;
                            case METHODNAME:
                                event = eventReader.nextEvent();
                                condition.setMethodName(event.asCharacters().getData());
                                break;
                            case CLASSFILE:
                                event = eventReader.nextEvent();
                                condition.setClassFile(event.asCharacters().getData());
                                break;
                            case LINE:
                                event = eventReader.nextEvent();
                                condition.setLine(event.asCharacters().getData());
                                break;
                            case LOGFILE:
                                event = eventReader.nextEvent();
                                condition.setLogFile(event.asCharacters().getData());
                                break;
                        case MESSAGE:
                            message = new Message();
                            break;
                            case TOKEN:
                                event = eventReader.nextEvent();
                                message.addMessage(TOKEN.toUpperCase(), event.asCharacters().getData());
                                break;
                            case REGEX:
                                event = eventReader.nextEvent();
                                message.addMessage(REGEX.toUpperCase(), event.asCharacters().getData());
                                break;
                            case VAR:
                                event = eventReader.nextEvent();
                                message.addMessage(VAR.toUpperCase(), event.asCharacters().getData());
                                break;
                        case QUERY:
                            event = eventReader.nextEvent();
                            rule.setQuery(event.asCharacters().getData());
                            break;
                        case ACTIONS:
                            event = eventReader.nextEvent();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rules;
    }

}