package com.dgregory.il2ssd.business.server;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.inject.Inject;

/**
 * 01/10/13 20:42
 * il2ssd
 */
public class Mission {

    static BooleanProperty missionRunning = new SimpleBooleanProperty();
    static StringProperty missionName = new SimpleStringProperty();
    @Inject
    Connection connection;
    @Inject
    Command command;

    public static Boolean getMissionRunning() {
        return missionRunning.get();
    }

    public static void setMissionRunning(Boolean missionRunning) {
        Mission.missionRunning.set(missionRunning);
    }

    public static BooleanProperty missionRunningProperty() {
        return missionRunning;
    }

    public static String getMissionName() {
        return missionName.get();
    }

    public static void setMissionName(String missionName) {
        Mission.missionName.set(missionName);
    }

    public static StringProperty missionNameProperty() {
        return missionName;
    }

}
