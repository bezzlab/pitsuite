package Controllers.config_generation;

import java.util.ArrayList;
import java.util.HashMap;

public class MSRunConfig {
    private String name;
    private String combinedRun;
    private ArrayList<String> files ;
    private ArrayList<String> fixedMods;
    private ArrayList<String> variableMods;
    private HashMap<String, String> labels;
    private String labelType;

    public MSRunConfig(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCombinedRun() {
        return combinedRun;
    }

    public void setCombinedRun(String combinedRun) {
        this.combinedRun = combinedRun;
    }

    public ArrayList<String> getFiles() {
        return files;
    }

    public void setFiles(ArrayList<String> files) {
        this.files = files;
    }

    public ArrayList<String> getFixedMods() {
        return fixedMods;
    }

    public void setFixedMods(ArrayList<String> fixedMods) {
        this.fixedMods = fixedMods;
    }

    public ArrayList<String> getVariableMods() {
        return variableMods;
    }

    public void setVariableMods(ArrayList<String> variableMods) {
        this.variableMods = variableMods;
    }

    public HashMap<String, String> getLabels() {
        return labels;
    }

    public void setLabels(HashMap<String, String> labels) {
        this.labels = labels;
    }

    public void setLabelType(String labelType) {
        this.labelType = labelType;
    }

    public String getLabelType() {
        return labelType;
    }
}
