package uk.org.il2ssd.jfx;

import javafx.beans.property.SimpleStringProperty;

/**
 *
 */
public class CycleMission {
    public SimpleStringProperty mission = new SimpleStringProperty();
    public SimpleStringProperty timer = new SimpleStringProperty();

    public CycleMission(String mission, String timer) {
        this.mission.setValue(mission);
        this.timer.setValue(timer);
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

    public String getTimer() {
        return timer.get();
    }

    public void setTimer(String timer) {
        this.timer.set(timer);
    }

    public SimpleStringProperty timerProperty() {
        return timer;
    }
}
