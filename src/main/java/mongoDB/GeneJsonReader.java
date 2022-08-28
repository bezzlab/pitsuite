package mongoDB;

import java.util.ArrayList;
import java.util.HashMap;

public class GeneJsonReader {
    private String chr;
    private int start;
    private int end;
    private String symbol;
    private HashMap<String, HashMap<String, HashMap<String, ArrayList<String>> >> transcripts;
    private ArrayList<String> kegg;
    private ArrayList<String> GO;


    public String getChr() {
        return chr;
    }

    public void setChr(String chr) {
        this.chr = chr;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>> getTranscripts() {
        return transcripts;
    }

    public void setTranscripts(HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>> transcripts) {
        this.transcripts = transcripts;
    }

    public ArrayList<String> getKegg() {
        return kegg;
    }

    public void setKegg(ArrayList<String> kegg) {
        this.kegg = kegg;
    }

    public ArrayList<String> getGO() {
        return GO;
    }

    public void setGO(ArrayList<String> GO) {
        this.GO = GO;
    }
}

