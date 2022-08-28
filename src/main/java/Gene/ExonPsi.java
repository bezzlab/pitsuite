package Gene;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class ExonPsi {
    private int startCoordinate;
    private int endCoordinate;
    private String bindsA2;
    private ArrayList<String> domains;
    private HashMap<Pair<String,String>, Double> condSamplPsi;
    private HashMap<Pair<String,String>, ArrayList<String>> condSamplTranscriptIds;
    private HashMap<String, ArrayList<String >> condPosPeptides;

    public ExonPsi(int startCoordinate, int endCoordinate, String bindsA2, ArrayList<String> domains,
                   HashMap<Pair<String, String>, Double> condSamplPsi, HashMap<Pair<String, String>,
            ArrayList<String>> condSamplTranscriptIds, HashMap<String, ArrayList<String>> condPosPeptides) {
        this.startCoordinate = startCoordinate;
        this.endCoordinate = endCoordinate;
        this.bindsA2 = bindsA2;
        this.domains = domains;
        this.condSamplPsi = condSamplPsi;
        this.condSamplTranscriptIds = condSamplTranscriptIds;
        this.condPosPeptides = condPosPeptides;
    }

    public int getStartCoordinate() {
        return startCoordinate;
    }

    public int getEndCoordinate() {
        return endCoordinate;
    }

    public String getBindsA2() {
        return bindsA2;
    }

    public ArrayList<String> getDomains() {
        return domains;
    }

    public HashMap<Pair<String, String>, Double> getCondSamplPsi() {
        return condSamplPsi;
    }

    public HashMap<Pair<String, String>, ArrayList<String>> getCondSamplTranscriptIds() {
        return condSamplTranscriptIds;
    }

    public HashMap<String, ArrayList<String>> getCondPosPeptides() {
        return condPosPeptides;
    }
}
