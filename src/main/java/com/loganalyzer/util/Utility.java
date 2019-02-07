package com.loganalyzer.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.loganalyzer.constants.Constants;
import com.loganalyzer.model.Condition;
import com.loganalyzer.model.SearchCriteria;
import org.springframework.stereotype.Component;

import javax.swing.text.html.parser.Parser;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class Utility {

    static ObjectMapper mapper = new ObjectMapper();

    public static String getPathSeparator(){
        return System.getProperty("os.name").toLowerCase().contains("windows") ? "\\" : "/";
    }

    public static String getFileName(String fullPath){

        String pathSeparator = "/";
        return fullPath.substring(fullPath.lastIndexOf(pathSeparator) + 1, fullPath.lastIndexOf("."));

    }

    public static String shortFileName(String fileName){
        int index = fileName.indexOf("-");
        return index != -1 ? fileName.substring(0, index) : fileName;
    }

    public static int indexOf(Pattern pattern, String s) {
        Matcher matcher = pattern.matcher(s);
        return matcher.find() ? matcher.start() : s.length();
    }

    public static List<String> infixToPostfixXML(String infix, Map<String, String> map) throws Exception {

        char key = 'A';
        List<String> list = new ArrayList<>();
        String[] tokens = infix.split(" ");
        for (int i = 0; i < tokens.length; i++){
            String token = tokens[i].replaceAll("[^a-z0-9A-Z():]", "");
            if ("(".equals(token) || ")".equals(token)){
                list.add(token);
            }else if ("OR".equals(token)){
                list.add("|");
            }else if ("AND".equals(token)){
                if ("AFTER".equals(tokens[i+1]) || "BEFORE".equals(tokens[i+1])){
                    String time = tokens[i+2];
                    String[] splitted = time.split(":");
                    Long milliseconds = 0L;
                    milliseconds = Long.parseLong(splitted[0])*60*60*1000 + Long.parseLong(splitted[1])*60*1000 + Long.parseLong(splitted[2])*1000 + Long.parseLong(splitted[3]);
                    list.add("&" + ( "AFTER".equals(tokens[i+1]) ? milliseconds : -milliseconds ));
                    i = i + 2;
                }else {
                    list.add("&");
                }
            }else {
                list.add(String.valueOf(key));
                map.put(String.valueOf(key), tokens[i].trim());
                key++;
            }
        }
        return toPostfix(list);
    }

    public static List<String> infixToPostfix(String infix, Map<String, SearchCriteria> map) throws Exception{

    List<String> list = new ArrayList<>();
    char count = 'A';

        boolean finding = false;
        for (int i = 0; i < infix.length(); i++){
            char s = infix.charAt(i);

            switch (s){
                case '(':
                    list.add("(");
                    break;
                case ')':
                    list.add(")");
                    break;
                case 'A':
                    if ("AND".equals(infix.substring(i, i+3))){
                        list.add("&");
                        i += 3;
                    }else if ("AFTER".equals(infix.substring(i, i+5))){
                        String whole = getStringTillCondition(infix.substring(i, infix.length()));
                        String[] splitted = whole.split(" ");
                        if (splitted.length > 1){
                            splitted = splitted[1].split(":");
                            Long milliseconds = 0L;
                            milliseconds = Long.parseLong(splitted[0])*60*60*1000 + Long.parseLong(splitted[1])*60*1000 + Long.parseLong(splitted[2])*1000 + Long.parseLong(splitted[3]);
                            list.remove(list.size() - 1);
                            list.add( "&" + String.valueOf(milliseconds));
                        }
                        i += whole.length() - 1;
                    }else {
                        throw new Exception(Constants.INVALID_RULE);
                    }
                    break;
                case 'B':
                    if ("BEFORE".equals(infix.substring(i, i+6))){
                        String whole = getStringTillCondition(infix.substring(i, infix.length()));
                        String[] splitted = whole.split(" ");
                        if (splitted.length > 1){
                            splitted = splitted[1].split(":");
                            Long milliseconds = 0L;
                            milliseconds = Long.parseLong(splitted[0])*60*60*1000 + Long.parseLong(splitted[1])*60*1000 + Long.parseLong(splitted[2])*1000 + Long.parseLong(splitted[3]);
                            list.remove(list.size() - 1);
                            list.add( "&" + String.valueOf(-milliseconds));
                        }
                        i += whole.length() - 1;
                    }else {
                        throw new Exception(Constants.INVALID_RULE);
                    }
                    break;
                case 'O':
                    if ("OR".equals(infix.substring(i, i+2))){
                        list.add("|");
                        i += 2;
                    }else {
                        throw new Exception(Constants.INVALID_RULE);
                    }
                    break;
                case '{':
                    int starting  = i;
                    while (true) {
                        if (infix.charAt(i) == '}') {
                            if (isJSONValid(infix.substring(starting, i+1))){
                                addToMap(infix.substring(starting, i+1), count, map);
                                list.add(String.valueOf(count));
                                count++;
                                break;
                            }
                        }
                        i++;
                    }
                    break;
            }
        }

       return toPostfix(list);
    }

    public static String getStringTillCondition(String whole){

        int index = 0;
        String output = "";
        while (whole.charAt(index) != '{'){
            output += whole.charAt(index);
            index++;
        }

        return output;

    }

    public static boolean isJSONValid(String json) {
        try {
            mapper.readValue(json, SearchCriteria.class);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    public static void addToMap(String json, char key, Map<String, SearchCriteria> map) {

        try {
            SearchCriteria criteria = mapper.readValue(json, SearchCriteria.class);
            map.put(String.valueOf(key), criteria);
        } catch (Exception ex) {
        }
    }

    private static List<String> toPostfix(List<String> infix)
    //converts an infix expression to postfix
    {
        Stack operators = new Stack();
        String symbol;
        List<String> postfix = new ArrayList<>();

        for(int i=0;i<infix.size();++i)
        //while there is input to be read
        {
            symbol = infix.get(i);
            if ("".equals(symbol) || Character.isSpaceChar(symbol.charAt(0))){
                continue;
            }

            if (symbol.contains("&") || symbol.contains("|")){
                while (!operators.isEmpty() && !(operators.peek().equals("(")))
                    postfix.add(operators.pop());

                operators.push(symbol);
            }
            //if it's an operand, add it to the string
            else if (Pattern.matches("^[a-zA-Z]+$", symbol))
                postfix.add(symbol);
            else if ("(".equals(symbol))
            //push (
            {

                operators.push(symbol);
            }
            else if (")".equals(symbol))
            //push everything back to (
            {
                while (!operators.peek().equals("("))
                {
                    postfix.add(operators.pop());
                }
                operators.pop();		//remove '('
            }
        }
        while (!operators.isEmpty())
            postfix.add(operators.pop());
        return postfix;
    }


}