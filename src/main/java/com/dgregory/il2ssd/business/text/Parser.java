package com.dgregory.il2ssd.business.text;

/**
 * 23/09/13 20:45
 * il2ssd
 */
public class Parser {
    private static final String ls = System.getProperty("line.separator");

    public static String cleanText(String text) {

        if (text.startsWith("\\u0020")) {
            text = " " + text.substring(6, text.length());
        }

        int sslen = text.length() - 2;
        String sst = text.substring(0, sslen);
        text = sst + ls;
        text = text.replace("\\t", "\t");
        return text;
    }

}
