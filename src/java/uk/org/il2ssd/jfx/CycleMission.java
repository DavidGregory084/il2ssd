package uk.org.il2ssd.jfx;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 */
public class CycleMission {
    public SimpleIntegerProperty index = new SimpleIntegerProperty();
    public SimpleStringProperty mission = new SimpleStringProperty();
    public SimpleStringProperty timer = new SimpleStringProperty();

    public CycleMission(int index, String mission, String timer) {
        this.index.setValue(index);
        this.mission.setValue(mission);
        this.timer.setValue(timer);
    }

    public int getIndex() {
        return index.get();
    }

    public void setIndex(int index) {
        this.index.set(index);
    }

    public SimpleIntegerProperty indexProperty() {
        return index;
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
