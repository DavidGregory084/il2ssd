package com.dgregory.il2ssd.business.text;

import com.dgregory.il2ssd.business.server.Mission;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 23/09/13 20:45
 * il2ssd
 */
public class Parser {
    static Pattern mission = Pattern.compile("Mission\\b.+");
    static Pattern chat = Pattern.compile("Chat:\\b.+");
    static Pattern nothingLoaded = Pattern.compile("Mission\\b.+\\bNOT loaded");
    static Pattern missionLoaded = Pattern.compile("Mission\\b.+\\bis Loaded");
    static Deque<String> parserQueue = new ConcurrentLinkedDeque<>();

    public static void addParseLine(String line) {
        parserQueue.addLast(line);
    }

    public static String pollParseLine() {
        return parserQueue.pollFirst();
    }

    public static String cleanText(String text) {
        text = text.replace("\\n", "\n");
        text = text.replace("\\t", "\t");
        text = text.replace("\\u0020", "\u0020");
        return text;
    }

    public static void parseLine(String line) {
        Matcher matchMission = mission.matcher(line);
        Matcher matchChat = chat.matcher(line);
        if (matchMission.find()) {
            parseMissionLine(line);
            return;
        }
        if (matchChat.find()) {

        }
    }

    public static void parseMissionLine(String line) {
        Matcher matchLoaded = missionLoaded.matcher(line);
        Matcher matchNotLoaded = nothingLoaded.matcher(line);
        if (matchLoaded.find()) {
            Mission.setMissionRunning(true);
            return;
        }
        if (matchNotLoaded.find()) {
            Mission.setMissionRunning(false);
        }
    }

}
