package com.loganalyzer.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.loganalyzer.constants.Constants;
import com.loganalyzer.model.SearchCriteria;
import org.springframework.stereotype.Component;

import javax.swing.text.html.parser.Parser;
import java.util.ArrayList;
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

    public static String infixToPostfix(String infix, Map<String, SearchCriteria> map) throws Exception{

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

    private static String toPostfix(List<String> infix)
    //converts an infix expression to postfix
    {
        Stack operators = new Stack();
        String symbol;
        String postfix = "";

        for(int i=0;i<infix.size();++i)
        //while there is input to be read
        {
            symbol = infix.get(i);
            if ("".equals(symbol) || Character.isSpaceChar(symbol.charAt(0))){
                continue;
            }

            if ("&".equals(symbol) || "|".equals(symbol)){
                while (!operators.isEmpty() && !(operators.peek().equals("(")))
                    postfix = postfix + operators.pop();

                operators.push(symbol);
            }
            //if it's an operand, add it to the string
            else if (Pattern.matches("^[a-zA-Z]+$", symbol))
                postfix = postfix + symbol;
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
                    postfix = postfix + operators.pop();
                }
                operators.pop();		//remove '('
            }
        }
        while (!operators.isEmpty())
            postfix = postfix + operators.pop();
        return postfix;
    }


}

class Stack
{
    String a[]=new String[100];
    int top=-1;

    void push(String c)
    {
        try
        {
            a[++top]= c;
        }
        catch(StringIndexOutOfBoundsException e)
        {
            System.out.println("Stack full , no room to push , size=100");
            System.exit(0);
        }
    }

    String pop()
    {
        return a[top--];
    }

    boolean isEmpty()
    {
        return (top==-1)?true:false;
    }

    String peek()
    {
        return a[top];
    }

}