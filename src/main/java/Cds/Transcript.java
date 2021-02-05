package Cds;

import Singletons.Config;
import TablesModels.Variation;
import javafx.util.Pair;
import org.dizitart.no2.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;


/**
 * Transcript data, transcript id is saved as key in a hashmap in ReaderCds.java
 * part of the information of the transcript is unique (is repeated between samples) and
 * another part is variable (each sample has different values)
 */
public class Transcript {
    private String geneId;
    //unique
    private String seq;
    private String strand;
    private int start;
    private int end;
    private String chr;
    private String transcriptId;
    private ArrayList<Exon> exons;
    // variable
    private ArrayList<Pair<String, String>> conditionsSamples;
    // for the Hashmaps, the key is Pair (condition, sample) . Eg.  Pair("Nsi", "1")
    private HashMap<String, HashMap<String, Double>> tpm;
    ArrayList<Variation> variations;
    HashSet<CDS> cdss;



    public Transcript(String transcriptId, String seq, String strand, int start, int end, ArrayList<Exon> exons, HashMap<Pair<String, String>, Double> tpm, ArrayList<Pair<String, String>> conditionsSamples, boolean hasCds ) {
//        this.transcriptId = transcriptId;
//        this.seq = seq;
//        this.strand = strand;
//        this.start = start;
//        this.end = end;
//        // sort the exons by start, to assure that they are correctly input
//        exons.sort(Comparator.comparingInt(Exon::getStart));
//        this.exons = exons;
//        this.tpm = tpm;
//        this.conditionsSamples = conditionsSamples;
//        this.hasCds = hasCds;
//        variations = new ArrayList<>();
//        cdss = new HashSet<>();
    }


    public Transcript(Document document) {
        transcriptId = document.get("transcriptID", String.class);
        seq = document.get("seq", String.class);
        if(Config.isReferenceGuided()){
            chr = document.get("chr", String.class);
        }else{
            chr = document.get("gene", String.class);
        }
        start = document.get("start", Long.class).intValue();
        end = document.get("end", Long.class).intValue();
        exons = (ArrayList<Exon>) document.get("exons");

        exons = new ArrayList<>();
        for (Object obj : (JSONArray) document.get("exons")) {
            JSONArray o = (JSONArray) obj;
            exons.add(new Exon(((Long) o.get(0)).intValue(), ((Long) o.get(1)).intValue()));
        }
    }


    public Transcript(Document document, HashMap<String, CDS> allCds){
        transcriptId = document.get("transcriptID", String.class);
        seq = document.get("seq", String.class);

        if(Config.isReferenceGuided()){
            chr = document.get("chr", String.class);
        }else{
            chr = document.get("gene", String.class);
        }

        start = document.get("start", Long.class).intValue();
        end = document.get("end", Long.class).intValue();
        exons = (ArrayList<Exon>) document.get("exons");

        exons = new ArrayList<>();
        for (Object obj: (JSONArray) document.get("exons")){
            JSONArray o = (JSONArray) obj;
            exons.add(new Exon( ((Long) o.get(0)).intValue(),((Long) o.get(1)).intValue()));
        }


        cdss = new HashSet<>();

        variations = new ArrayList<>();
        tpm = (HashMap<String, HashMap<String, Double>>) document.get("TPM");
        if(document.containsKey("CDS")){

            JSONObject transcriptCds = (JSONObject) document.get("CDS");

            CDS cds;
            for (Object key : transcriptCds.keySet()) {
                if (transcriptCds.get(key) instanceof JSONObject) {
                    JSONObject cdsObj = (JSONObject) transcriptCds.get(key);
                    String seq = (String) cdsObj.get("sequence");

                    if (allCds.containsKey(seq)) {
                        cds = allCds.get(seq);
                    } else {
                        cds = new CDS((String) cdsObj.get("strand"), seq, (String) key);
                        if(cdsObj.containsKey("uniprot")){
                            cds.setUniprotId((String) cdsObj.get("uniprot"));
                        }
                        allCds.put(seq, cds);
                    }

                    cdss.add(cds);

                    cds.addTranscript(this, ((Long) cdsObj.get("start")).intValue(), ((Long) cdsObj.get("end")).intValue());

                    if (((JSONObject) transcriptCds.get(key)).containsKey("peptides")) {
                        JSONArray peptides = (JSONArray) ((JSONObject) transcriptCds.get(key)).get("peptides");
                        for (Object pepObj : peptides) {
                            JSONObject pepO = (JSONObject) pepObj;
                            cds.addPeptide((String) pepO.get("sequence"),(String)  pepO.get("mod"), (double) pepO.get("probability"),
                                    (String) pepO.get("run"), (String) pepO.get("condition"), (String)  pepO.get("sample"));
                        }
                    }

                    if(cdsObj.containsKey("pfam")){
                        JSONArray domains = (JSONArray) cdsObj.get("pfam");
                        for(Object obj: domains){
                            JSONObject o = (JSONObject) obj;
                            cds.addPfam(new Pfam(((Long) o.get("start")).intValue(), ((Long) o.get("end")).intValue(), (String) o.get("desc"), (String) o.get("id")));
                        }
                    }

                }
            }


        }

    }

    public String getTranscriptId() {
        return transcriptId;
    }

    public String getStrand() {
        return strand;
    }

    public int getStartGenomCoord() {
        return start;
    }

    public int getEndGenomCoord() {
        return end;
    }

    public ArrayList<Exon> getExons() {
        return exons;
    }


    // sample
    public void addSample(Pair<String, String> sample){
        conditionsSamples.add(sample);
    }

    public ArrayList<Pair<String, String>> getConditionsSamples() {
        return conditionsSamples;
    }


    //tpm
    public void addTpm(Pair<String, String> conditionSample, Double tpmValue){
//        tpm.put(conditionSample, tpmValue);
    }

    public HashMap<String, HashMap<String, Double>>  getTpms(){
        return tpm;
    }

    public Double getSampleTpm(String condition, String sample){ return tpm.get(condition).get(sample); }


    // variations


    // geneId
    public void setGeneId(String geneId){
        this.geneId = geneId;
    }
    public String getGeneId() {        return geneId;     }


    // seq coordinates to genom coord
    public Integer genomCoordFromSeqPos(Integer seqPos) {
        int genomPosition = 0;
        int distance = 0;

        exons.sort(Comparator.comparingInt(Exon::getStart));
        // coordinates are end inclusive, so this adds a +1 position
        for (Exon exon: exons){
            if ( seqPos <=  (distance + (exon.getEnd() - exon.getStart()) + 1) ){
//                if ( seqPos >= distance && seqPos <=  (distance + (exon.getEnd() - exon.getStart())) ){
                genomPosition =  exon.getStart() +  (seqPos  - distance) ;
                break;
            } else {
                distance += (exon.getEnd() - exon.getStart()) + 1; //  +1 cause coord are inclusive
            }
        }

        return genomPosition;

    }

    public boolean getHasCds(){
        return cdss.size() > 0;
    }



    public double getAverageTPMByCondition(String condition){
        double average;

        String conditionUpper = condition.trim().toUpperCase();

        double accumSum =0.0;
        int count = 0;

        if(!tpm.containsKey(condition)){
            return Double.NaN;
        }

        for(Map.Entry<String, Double> tpmEntry: tpm.get(condition).entrySet()){
            accumSum += tpmEntry.getValue();
            count ++;

        }

        if (count > 0 ){
            average = accumSum / count;
            return average;
        } else {
            return  Double.NaN;
        }
    }


    public double getCumSumTPMByCondition(String condition){
        double average;

        String conditionUpper = condition.trim().toUpperCase();

        double accumSum =0.0;

        for(Map.Entry<String, Double> tpmEntry: tpm.get(condition).entrySet()){
            String entryCond = tpmEntry.getKey().toUpperCase();
            if (conditionUpper.equals(entryCond)){
                accumSum += tpmEntry.getValue();
            }
        }


        return accumSum;

    }

    /**
     * was the transcript identified in the sample?
     * @param conditionToTest  condition name
     * @return boolean that indicates if the transcript was identified in the condition
     */
    public boolean identifiedInCondition(String conditionToTest){
        for (Pair<String, String> condSamplPair: conditionsSamples){
            String conditon = condSamplPair.getKey();
            if (conditon.toUpperCase().equals(conditionToTest.toUpperCase())){
                return true;
            }
        }

        return false;
    }

    public String getSequence(int start, int end){
        return seq.substring(start, end);
    }
    public String getSequence(int start){
        return seq.substring(start);
    }
    public String getSequence(){
        return seq;
    }

    public void addVariation(Variation variation){
        variations.add(variation);
    }

    public ArrayList<Variation> getVariations(){
        return variations;
    }

    public void addCDS(CDS cds){
        cdss.add(cds);
    }


    public HashSet<CDS> getCdss() {
        return cdss;
    }

    public HashMap<Integer, Variation> getVariationsWithPosition(){

        HashMap<Integer, Variation> res = new HashMap<>();

        for(Variation variation: variations){
            int genomicPos = variation.getRefPos();
            int pos = genomicPos - start + 1;
            for(Exon exon: exons){
                if(exon.getStart()<=genomicPos && exon.getEnd()>=genomicPos){
                    pos -= (exon.getStart()+1);
                }else{
                    pos -= (exon.getEnd()-exon.getStart()+1);
                }
            }
            res.put(pos, variation);
        }
        return res;

    }


    public String getChr() {
        return chr;
    }
}
