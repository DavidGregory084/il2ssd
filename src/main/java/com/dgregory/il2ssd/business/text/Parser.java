package com.dgregory.il2ssd.business.text;

/**
 * 23/09/13 20:45
 * il2ssd
 */
public class Parser {
    private static final String ls = System.getProperty("line.separator");

    public static String cleanText(String text) {
        text = text.replace("\\n", "\n");
        text = text.replace("\\t", "\t");
        text = text.replace("\\u0020", "\u0020");
        return text;
    }

}
