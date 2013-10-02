package com.dgregory.il2ssd.business.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 23/09/13 20:45
 * il2ssd
 */
public class Parser {
    static Pattern mission = Pattern.compile("Mission");
    static Pattern nothingLoaded = Pattern.compile("Mission\\b.+\\bNOT loaded");
    static Pattern missionLoaded = Pattern.compile("Mission\\b.+\\bis Loaded");


    public static String cleanText(String text) {
        text = text.replace("\\n", "\n");
        text = text.replace("\\t", "\t");
        text = text.replace("\\u0020", "\u0020");
        return text;
    }

    public static String getLoaded(String text) {
        Matcher matchLoaded = missionLoaded.matcher(text);
        Matcher matchNotLoaded = nothingLoaded.matcher(text);
        if (matchLoaded.find()) {
            return "load";
        }
        if (matchNotLoaded.find()) {
            return "unload";
        }
        return "ignore";
    }

}
