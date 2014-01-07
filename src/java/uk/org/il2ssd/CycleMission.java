package uk.org.il2ssd;

import javafx.beans.property.SimpleStringProperty;

/**
 *
 */
public class CycleMission {
    public SimpleStringProperty mission = new SimpleStringProperty();
    public SimpleStringProperty timer = new SimpleStringProperty();

    public String getTimer() {
        return timer.get();
    }

    public void setTimer(String timer) {
        this.timer.set(timer);
    }

    public SimpleStringProperty timerProperty() {
        return timer;
    }

    public String getMission() {
        return mission.get();
    }

    public void setMission(String mission) {
        this.mission.set(mission);
    }

    public SimpleStringProperty missionProperty() {
        return mission;
    }
}
