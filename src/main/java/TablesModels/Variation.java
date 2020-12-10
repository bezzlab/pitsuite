package TablesModels;

import Cds.CDS;
import Cds.Transcript;
import javafx.util.Pair;
import org.json.simple.JSONObject;

import java.util.*;

public class Variation {
    private String gene;
    private int refPos;
    private String ref;
    private String alt;
    private String chr;
    private JSONObject transcripts;
    private Map<String, Map< String, Map<String, Double>>> conditions;
    private boolean hasPeptideEvidence;
    private boolean inCDS;
    private boolean silent;
    private String type;

    public Variation(String gene, String chr, int pos, String ref, String alt, boolean hasPeptideEvidence, Map<String,
            Map< String, Map<String, Double>>> conditions, boolean inCDS, boolean silent, String type) {
        this.gene = gene;
        this.refPos = pos;
        this.ref = ref;
        this.alt = alt;
        this.chr = chr;
        this.hasPeptideEvidence = hasPeptideEvidence;
        this.conditions = conditions;
        this.inCDS = inCDS;
        this.silent = silent;
        this.type = type;
    }

    public Variation(String gene, String chr, int pos, String ref, String alt, boolean hasPeptideEvidence, Map<String,
            Map< String, Map<String, Double>>> conditions, JSONObject transcripts, boolean inCDS, boolean silent, String type) {
        this.gene = gene;
        this.refPos = pos;
        this.ref = ref;
        this.alt = alt;
        this.chr = chr;
        this.hasPeptideEvidence = hasPeptideEvidence;
        this.conditions = conditions;
        this.inCDS = inCDS;
        this.silent = silent;
        this.type = type;
        this.transcripts = transcripts;
    }

    // used in gene browser
    public Variation(String gene, String chr, int pos, String ref, String alt, boolean hasPeptideEvidence,
                     Map<String, Map< String, Map<String, Double>>> conditions, JSONObject transcripts, boolean inCDS, boolean silent) {
        this.gene = gene;
        this.refPos = pos;
        this.ref = ref;
        this.alt = alt;
        this.chr = chr;
        this.hasPeptideEvidence = hasPeptideEvidence;
        this.conditions = conditions;
        this.transcripts = transcripts;
        this.inCDS = inCDS;
        this.silent = silent;
    }

    public String getGene() {
        return gene;
    }

    public int getRefPos() {
        return refPos;
    }

    public String getRef() {
        return ref;
    }

    public String getAlt() {
        return alt;
    }

    public boolean isHasPeptideEvidence() {
        return hasPeptideEvidence;
    }

    public Map<String, Map<String, Map<String, Double>>> getConditions() {
        return conditions;
    }

    public String getChr() {
        return chr;
    }

    public JSONObject getTranscripts() {
        return transcripts;
    }

    public ArrayList<String> getTranscriptIds() {
        ArrayList<String> ids = new ArrayList<>(transcripts.size());
        for(Object obj: transcripts.keySet()){
            ids.add((String) obj);
        }
        return ids;
    }

    public Integer getPositionInTranscript(String transcript){
        return Math.toIntExact((Long)((JSONObject) transcripts.get(transcript)).get("pos"));
    }

    public int getPositionInCds(CDS cds, Transcript transcript){
        Pair<Integer, Integer> transcriptWithPos = cds.getTranscriptWithCdsPos(transcript);
        int varPosInTranscript = getPositionInTranscript(transcript.getTranscriptId());
        return varPosInTranscript-transcriptWithPos.getKey()+1;
    }

    public String getRefAA(){

        HashSet<String> refs = new HashSet<>();


        for(Object entry:transcripts.values()){
            JSONObject transcript = (JSONObject) entry;
            refs.add((String) transcript.get("aaRef"));
        }

        StringBuilder sb = new StringBuilder();
        for(String ref: refs){
            if(ref!=null){
                sb.append(ref).append(";");
            }

        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

    public String getRefAA(String transcriptID){
        return (String) ((JSONObject) transcripts.get(transcriptID)).get("aaRef");
    }
    public String getAltAA(String transcriptID){
        return (String) ((JSONObject) transcripts.get(transcriptID)).get("aaAlt");
    }
    public String getPeptides(String transcriptID){
        return (String) ((JSONObject) transcripts.get(transcriptID)).get("peptides");
    }
    public double getPeptideProb(String transcriptID){
        return (double) ((JSONObject) transcripts.get(transcriptID)).get("peptideProb");
    }

    public JSONObject getTranscriptObj(String transcriptID){
        return ((JSONObject) transcripts.get(transcriptID));
    }

    public JSONObject getTranscriptsObj(){
        return transcripts;
    }


    public String getAltAA(){

        HashSet<String> alts = new HashSet<>();

        for(Object entry:transcripts.values()){
            JSONObject transcript = (JSONObject) entry;
            alts.add((String) transcript.get("aaAlt"));
        }

        StringBuilder sb = new StringBuilder();
        for(String alt: alts){
            if(alt!=null){
                sb.append(alt).append(";");
            }

        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

    public boolean isInCDS() {
        return inCDS;
    }

    public boolean isSilent() {
        return silent;
    }

    public String getType() {
        return type;
    }
}