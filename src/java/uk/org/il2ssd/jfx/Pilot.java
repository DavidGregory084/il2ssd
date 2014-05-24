package uk.org.il2ssd.jfx;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 */
public class Pilot {
    SimpleStringProperty number = new SimpleStringProperty();
    SimpleStringProperty socket = new SimpleStringProperty();
    SimpleStringProperty ip = new SimpleStringProperty();
    SimpleStringProperty name = new SimpleStringProperty();
    SimpleLongProperty score = new SimpleLongProperty();
    SimpleStringProperty team = new SimpleStringProperty();

    public Pilot(String socket, String ip, String name) {
        this.number.setValue("");
        this.socket.setValue(socket);
        this.ip.setValue(ip);
        this.name.setValue(name);
        this.score.setValue(0);
        this.team.setValue("");
    }

    public Pilot(String number, String socket, String ip, String name) {
        this.number.setValue(number);
        this.socket.setValue(socket);
        this.ip.setValue(ip);
        this.name.setValue(name);
        this.score.setValue(0);
        this.team.setValue("");
    }

    public Pilot(String number, String name, Long score, String team) {
        this.number.setValue(number);
        this.socket.setValue("");
        this.ip.setValue("");
        this.name.setValue(name);
        this.score.setValue(score);
        this.team.setValue(team);
    }

    public Pilot(String number, String name, long score, String team) {
        this.number.setValue(number);
        this.socket.setValue("");
        this.ip.setValue("");
        this.name.setValue(name);
        this.score.setValue(score);
        this.team.setValue(team);
    }

    public String getNumber() {
        return number.get();
    }

    public void setNumber(String number) {
        this.number.set(number);
    }

    public SimpleStringProperty numberProperty() {
        return number;
    }

    public void setScore(long score) {
        this.score.set(score);
    }

    public String getIp() {
        return ip.get();
    }

    public void setIp(String ip) {
        this.ip.set(ip);
    }

    public SimpleStringProperty ipProperty() {
        return ip;
    }

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

    public Long getScore() {
        return score.get();
    }

    public void setScore(Long score) {
        this.score.set(score);
    }

    public SimpleLongProperty scoreProperty() {
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
