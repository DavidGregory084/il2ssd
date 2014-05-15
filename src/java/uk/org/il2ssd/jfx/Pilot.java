package uk.org.il2ssd.jfx;

import javafx.beans.property.SimpleStringProperty;

/**
 *
 */
public class Pilot {
    SimpleStringProperty socket = new SimpleStringProperty();
    SimpleStringProperty name = new SimpleStringProperty();
    SimpleStringProperty score = new SimpleStringProperty();
    SimpleStringProperty team = new SimpleStringProperty();

    public String getSocket() {
        return socket.get();
    }

    public void setSocket(String socket) {
        this.socket.set(socket);
    }

    public SimpleStringProperty socketProperty() {
        return socket;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public String getScore() {
        return score.get();
    }

    public void setScore(String score) {
        this.score.set(score);
    }

    public SimpleStringProperty scoreProperty() {
        return score;
    }

    public String getTeam() {
        return team.get();
    }

    public void setTeam(String team) {
        this.team.set(team);
    }

    public SimpleStringProperty teamProperty() {
        return team;
    }
}
