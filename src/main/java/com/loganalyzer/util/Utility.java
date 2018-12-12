package com.loganalyzer.util;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class Utility {

    public static String getFileName(String fullPath){

        return fullPath.substring(fullPath.lastIndexOf("\\") + 1, fullPath.lastIndexOf("."));

    }

    public static int indexOf(Pattern pattern, String s) {
        Matcher matcher = pattern.matcher(s);
        return matcher.find() ? matcher.start() : s.length();
    }
}
