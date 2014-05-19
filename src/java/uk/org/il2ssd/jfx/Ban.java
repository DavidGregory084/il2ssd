package uk.org.il2ssd.jfx;

import javafx.beans.property.SimpleStringProperty;

/**
 *
 */
public class Ban {
    public SimpleStringProperty type;
    public SimpleStringProperty value;

    public Ban(String type, String value) {
        this.type.setValue(type);
        this.value.setValue(value);
    }

    public String getType() {
        return type.get();
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public SimpleStringProperty typeProperty() {
        return type;
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
