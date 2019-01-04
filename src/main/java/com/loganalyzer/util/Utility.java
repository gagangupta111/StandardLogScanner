package com.loganalyzer.util;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class Utility {

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
}
