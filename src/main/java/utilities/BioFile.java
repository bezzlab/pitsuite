package utilities;

import javafx.beans.property.BooleanProperty;

public class BioFile {

    protected String path;
    protected String condition;
    protected String sample;
    protected BooleanProperty selected;

    public BioFile(String path, String condition, String sample) {
        this.path = path;
        this.condition = condition;
        this.sample = sample;
    }

    public BioFile(String path, String condition) {
        this.path = path;
        this.condition = condition;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getSample() {
        return sample;
    }

    public void setSample(String sample) {
        this.sample = sample;
    }

    public boolean isSelected() {
        return selected.get();
    }
    public BooleanProperty selectedProperty(){return selected;}
    public void setSelected(BooleanProperty selected) {
        this.selected = selected;
    }
    public void setSelected(boolean selected) {
        this.selected.setValue(selected);
    }
}
