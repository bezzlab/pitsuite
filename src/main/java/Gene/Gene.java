package Gene;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Gene {
    private String chromosome;
    private int startCoordinate;
    private int endCoordinate;
    private String symbol;
    private ArrayList<String> transcripts;

    public Gene() {
        this.transcripts = new ArrayList<>();
    }

    public Gene(String symbol, String chromosome, int startCoordinate, int endCoordinate, ArrayList<String> transcripts) {
        this.symbol = symbol;
        this.chromosome = chromosome;
        this.startCoordinate = startCoordinate;
        this.endCoordinate = endCoordinate;
        this.transcripts = transcripts;
    }

    public ArrayList<String> getTranscripts() {
        return transcripts;
    }

    public void  addTranscript(String transcID) {
        transcripts.add(transcID);
    }


    public String getChromosome() {
        return chromosome;
    }

    public int getStartCoordinate() {
        return startCoordinate;
    }

    public int getEndCoordinate() {
        return endCoordinate;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setTranscripts(ArrayList<String> transcripts) {
        this.transcripts = transcripts;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public void setStartCoordinate(int startCoordinate) {
        this.startCoordinate = startCoordinate;
    }

    public void setEndCoordinate(int endCoordinate) {
        this.endCoordinate = endCoordinate;
    }
}

