package uk.org.il2ssd.jfx;

import javafx.beans.property.SimpleStringProperty;

/**
 *
 */
public class DifficultySetting {
    public SimpleStringProperty setting = new SimpleStringProperty();
    public SimpleStringProperty value = new SimpleStringProperty();

    public DifficultySetting(String setting, String value) {
        this.setting.setValue(setting);
        this.value.setValue(value);
    }

    public String getSetting() {
        return setting.get();
    }

    public void setSetting(String setting) {
        this.setting.set(setting);
    }

    public SimpleStringProperty settingProperty() {
        return setting;
    }

    public String getValue() {
        return value.get();
    }

    public void setValue(String value) {
        this.value.set(value);
    }

    public SimpleStringProperty valueProperty() {
        return value;
    }

}
