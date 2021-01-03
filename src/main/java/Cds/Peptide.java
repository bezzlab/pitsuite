package Cds;

import org.dizitart.no2.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utilities.MSRun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Peptide {


    private String sequence;
    private ArrayList<PSM> psms;
    private Double probability;
    HashSet<String> genes;
    HashMap<String, Double> intensities;
    MSRun run;


    public Peptide(String sequence){
        this.sequence = sequence;
        psms = new ArrayList<>();
    }

    public Peptide(String sequence, MSRun run){
        this.sequence = sequence;
        psms = new ArrayList<>();
        this.run = run;
    }

    public Peptide(Document document){
        this.sequence = document.get("peptide", String.class);
        this.probability = (double) document.get("probability");
        this.run = new MSRun((String) document.get("run"));

        psms = new ArrayList<>();

        JSONArray docPsms = document.get("psm", JSONArray.class);
        for(Object o: docPsms){
            JSONObject psm = (JSONObject) o;


            if(psm.containsKey("label")){
                psms.add(new PSM((String) psm.get("mod"), (double) psm.get("probability"),
                        (String) psm.get("label"), Math.toIntExact((Long) psm.get("specIndex")), (String) psm.get("file")));
            }else{
                psms.add(new PSM((String) psm.get("mod"), (double) psm.get("probability"),
                        Math.toIntExact((Long) psm.get("specIndex")), (String) psm.get("file")));
            }


        }

        JSONObject transcripts = document.get("transcripts", JSONObject.class);
        genes = new HashSet<>(transcripts.keySet());

        JSONObject jsonIntensities = document.get("intensities", JSONObject.class);
        intensities = (HashMap) jsonIntensities;

    }

    public Peptide(Document document, String runName, MSRun run){ //for combined runs
        this.sequence = document.get("peptide", String.class);

        psms = new ArrayList<>();

        JSONArray docPsms = document.get("psm", JSONArray.class);
        for(Object o: docPsms){
            JSONObject psm = (JSONObject) o;


            if(psm.containsKey("run") && psm.get("run").equals(runName)){

                if(psm.containsKey("intensities")){
                    psms.add(new PSM((String) psm.get("mod"), (double) psm.get("probability"),
                            (String) psm.get("run"), Math.toIntExact((Long) psm.get("specIndex")), (String) psm.get("file"),
                            (HashMap) psm.get("intensities")));
                }else{
                    psms.add(new PSM((String) psm.get("mod"), (double) psm.get("probability"),
                            (String) psm.get("label"), Math.toIntExact((Long) psm.get("specIndex")), (String) psm.get("file")));
                }


            }


        }


        JSONObject transcripts = document.get("transcripts", JSONObject.class);
        genes = new HashSet<>(transcripts.keySet());

        //intensities = new HashMap<>();
        JSONObject jsonIntensities = (JSONObject) document.get("intensities", JSONObject.class).get(runName);
        intensities = (HashMap) jsonIntensities;
//        for(Object o:  jsonIntensities.entrySet()){
//            Map.Entry<String, String> entry = (Map.Entry<String, String>) o;
//
//        }


        if(run.getType().equals("SILAC")){
            this.probability = (Double) document.get("probability");
        }else{
            this.probability = (Double) document.get("probability", JSONObject.class).get(runName);
        }


    }


    public void addPsm(PSM psm){
        psms.add(psm);
    }

    public String getSequence(){ return sequence; }

    public int getNbGenes(){
        if(!run.isCombined()){
            return genes.size();
        }else{
            HashSet<String> allGenes = new HashSet<>();
            for(MSRun subRun: run.getRuns()){
                Peptide p = subRun.getPeptide(sequence);
                if(p!=null){
                    allGenes.addAll(subRun.getPeptide(sequence).getGenes());
                }

            }
            return allGenes.size();
        }

    }

    public ArrayList<PSM> getPsms() {
        return psms;
    }

    public boolean contains(PTM ptm){
        for(PSM psm: psms){
            if(psm.contains(ptm)){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object v) {
        boolean retVal = false;

        if (v instanceof Peptide){
            Peptide ptr = (Peptide) v;
            retVal = ptr.getSequence().equals(this.sequence);
        }

        return retVal;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.sequence != null ? this.sequence.hashCode() : 0);
        return hash;
    }

    public Double getProbability() {
        return probability;
    }

    public HashSet<String> getGenes() {
        return genes;
    }

    public HashMap<String, Double> getIntensities() {
        return intensities;
    }

    public String getRunName(){
        return run.getName();
    }

    public boolean hasNonZeroIntensities(){
        for (Double intensity: intensities.values()) {
            if(intensity>0) return true;
        }
        return false;
    }

    public boolean hasPSM(){
        return psms.size()>0;
    }


}
