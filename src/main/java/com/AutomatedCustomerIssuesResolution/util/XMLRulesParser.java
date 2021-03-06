package com.AutomatedCustomerIssuesResolution.util;

import com.AutomatedCustomerIssuesResolution.constants.Constants;
import com.AutomatedCustomerIssuesResolution.model.Condition;
import com.AutomatedCustomerIssuesResolution.model.Message;
import com.AutomatedCustomerIssuesResolution.model.Rule;

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
    static final String ENABLE = "enable";
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

        InputStream in = new FileInputStream(Constants.RULES_XML);
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

            Utility.validateXMLSchema(Constants.RULES_XML);
            // First, create a new XMLInputFactory
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLEventReader eventReader = inputFactory.createXMLEventReader(in);

            String data = "";
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();

                if (event.isStartElement()) {
                    StartElement startElement = event.asStartElement();
                    switch (startElement.getName().getLocalPart()){
                        case RULES:
                        case RULE:
                            rule = new Rule();
                            Iterator<Attribute> attributesRule = startElement.getAttributes();
                            while (attributesRule.hasNext()) {
                                Attribute attribute = attributesRule.next();
                                if (attribute.getName().toString().equals(NAME)) {
                                    rule.setRuleName(attribute.getValue());
                                }else if (attribute.getName().toString().equals(ENABLE)) {
                                    rule.setEnable(Boolean.parseBoolean(attribute.getValue()));
                                }
                            }
                            break;
                        case DESCRIPTION:
                            data = "";
                            event = eventReader.nextEvent();
                            while (!event.isEndElement()) {
                                data += event.asCharacters().getData();
                                event = eventReader.nextEvent();
                            }
                            rule.setDesc(data);
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
                                data = "";
                                event = eventReader.nextEvent();
                                while (!event.isEndElement()) {
                                    data += event.asCharacters().getData();
                                    event = eventReader.nextEvent();
                                }
                                condition.setLevel(data);
                                break;
                            case CLASSNAME:
                                data = "";
                                event = eventReader.nextEvent();
                                while (!event.isEndElement()) {
                                    data += event.asCharacters().getData();
                                    event = eventReader.nextEvent();
                                }
                                condition.setClassName(data);
                                break;
                            case METHODNAME:
                                data = "";
                                event = eventReader.nextEvent();
                                while (!event.isEndElement()) {
                                    data += event.asCharacters().getData();
                                    event = eventReader.nextEvent();
                                }
                                condition.setMethodName(data);
                                break;
                            case CLASSFILE:
                                data = "";
                                event = eventReader.nextEvent();
                                while (!event.isEndElement()) {
                                    data += event.asCharacters().getData();
                                    event = eventReader.nextEvent();
                                }
                                condition.setClassFile(data);
                                break;
                            case LINE:
                                data = "";
                                event = eventReader.nextEvent();
                                while (!event.isEndElement()) {
                                    data += event.asCharacters().getData();
                                    event = eventReader.nextEvent();
                                }
                                condition.setLine(data);
                                break;
                            case LOGFILE:
                                data = "";
                                event = eventReader.nextEvent();
                                while (!event.isEndElement()) {
                                    data += event.asCharacters().getData();
                                    event = eventReader.nextEvent();
                                }
                                condition.setLogFile(data);
                                break;
                        case MESSAGE:
                            message = new Message();
                            break;
                            case TOKEN:
                                data = "";
                                event = eventReader.nextEvent();
                                while (!event.isEndElement()) {
                                    data += event.asCharacters().getData();
                                    event = eventReader.nextEvent();
                                }
                                message.addMessage(TOKEN.toUpperCase(), data);
                                break;
                            case REGEX:
                                data = "";
                                event = eventReader.nextEvent();
                                while (!event.isEndElement()) {
                                    data += event.asCharacters().getData();
                                    event = eventReader.nextEvent();
                                }
                                message.addMessage(REGEX.toUpperCase(), data);
                                break;
                            case VAR:
                                data = "";
                                event = eventReader.nextEvent();
                                while (!event.isEndElement()) {
                                    data += event.asCharacters().getData();
                                    event = eventReader.nextEvent();
                                }
                                message.addMessage(VAR.toUpperCase(), data);
                                break;
                        case QUERY:
                            data = "";
                            event = eventReader.nextEvent();
                            while (!event.isEndElement()) {
                                data += event.asCharacters().getData();
                                event = eventReader.nextEvent();
                            }
                            rule.setQuery(data);
                            break;
                        case ACTIONS:
                            data = "";
                            event = eventReader.nextEvent();
                            while (!event.isEndElement()) {
                                data += event.asCharacters().getData();
                                event = eventReader.nextEvent();
                            }
                            rule.setActions(data);
                            break;
                        default:
                            throw new Exception(Constants.INVALID_XML);
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