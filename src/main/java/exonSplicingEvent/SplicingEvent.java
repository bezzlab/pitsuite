package exonSplicingEvent;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class SplicingEvent {
    private String gene;
    private ArrayList<String> alternativeTranscripts;
    private ArrayList<String> totalTranscripts;
    private Double deltaPsi;
    private Double pVal;
    HashMap<Pair<String, String>, Double> tpmWithConditionSampleMap; // pair: Condition Sample
    HashMap<Pair<String, String>, Double> psiConditionSampleMap;     // pair: Condition Sample
    String domainsIn;
    String domainsOut;
    private boolean hasPeptideEvidence;


    public SplicingEvent(String gene, ArrayList<String> alternativeTranscripts, ArrayList<String> totalTranscripts,
                         Double deltaPsi, Double pVal, HashMap<Pair<String, String>, Double> tpmWithConditionSampleMap,
                         HashMap<Pair<String, String>, Double> psiConditionSampleMap,
                         String domainsIn, String domainsOut, boolean hasPeptideEvidence) {
        this.gene = gene;
        this.alternativeTranscripts = alternativeTranscripts;
        this.totalTranscripts = totalTranscripts;
        this.deltaPsi = deltaPsi;
        this.pVal = pVal;
        this.tpmWithConditionSampleMap = tpmWithConditionSampleMap;
        this.psiConditionSampleMap = psiConditionSampleMap;
        this.domainsIn = domainsIn;
        this.domainsOut = domainsOut;
        this.hasPeptideEvidence = hasPeptideEvidence;
    }


    public String getGeneSymbol() {
        return gene;
    }

    public ArrayList<String> getAlternativeTranscripts() {
        return alternativeTranscripts;
    }

    public ArrayList<String> getTotalTranscripts() {
        return totalTranscripts;
    }

    public Double getDeltaPsi() {
        return deltaPsi;
    }

    public Double getPVal() {
        return pVal;
    }

    public HashMap<Pair<String, String>, Double> getTpmWithConditionSampleMap() {
        return tpmWithConditionSampleMap;
    }

    public HashMap<Pair<String, String>, Double> getPsiConditionSampleMap() {
        return psiConditionSampleMap;
    }

    public String getDomainsIn() {
        return domainsIn;
    }

    public String getDomainsOut() {
        return domainsOut;
    }

    public String getGene() {
        return gene;
    }

    public Double getpVal() {
        return pVal;
    }

    public boolean isHasPeptideEvidence() {
        return hasPeptideEvidence;
    }
}
