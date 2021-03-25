package Cds;

import Singletons.Config;
import org.dizitart.no2.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utilities.MSRun;

import java.util.*;

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

        JSONArray docPsms = document.get("psms", JSONArray.class);
        for(Object o: docPsms){
            JSONObject psm = (JSONObject) o;


            if(psm.containsKey("label")){
                psms.add(new PSM((String) psm.get("mod"), (double) psm.get("probability"),
                        (String) psm.get("label"), Math.toIntExact((Long) psm.get("specIndex")), (String) psm.get("file")));
            }else{
                if (psm.containsKey("intensity")){
                    HashMap<String, Double> intensity = new HashMap<>();
                    intensity.put((String) psm.get("run"), (Double) psm.get("intensity"));
//                    psms.add(new PSM((String) psm.get("mod"), (double) psm.get("probability"),
//                            Math.toIntExact((Long) psm.get("specIndex")), (String) psm.get("file"), intensity));
                }else{
                    psms.add(new PSM((String) psm.get("mod"), (double) psm.get("probability"),
                            Math.toIntExact((Long) psm.get("specIndex")), (String) psm.get("file")));
                }

            }


        }

        JSONObject transcripts = document.get("transcripts", JSONObject.class);
        genes = new HashSet<>(transcripts.keySet());

        JSONObject jsonIntensities = document.get("intensity", JSONObject.class);
        intensities = (HashMap) jsonIntensities;

    }

    public Peptide(Document document, String runName, MSRun run){ //for combined runs
        this.sequence = document.get("peptide", String.class);

        psms = new ArrayList<>();

        JSONArray docPsms = document.get("psms", JSONArray.class);
        for(Object o: docPsms){
            JSONObject psm = (JSONObject) o;


            if(psm.containsKey("run") && psm.get("run").equals(runName)){

                if(psm.containsKey("intensity")){

                    if(Config.getRunType(runName).equals("LABELFREE")){
                        HashMap<String, Double> intensity = new HashMap<>();

                        if (psm.get("intensity").getClass().equals(Double.class)){
                            intensity.put(runName, (Double) psm.get("intensity"));
                        }else{
                            intensity.put(runName, ((Long) psm.get("intensity")).doubleValue());
                        }

                        psms.add(new PSM((String) psm.get("mod"), (double) psm.get("probability"),
                                (String) psm.get("run"), Math.toIntExact((Long) psm.get("specIndex")), (String) psm.get("file"),
                                intensity));
                    }else{
                        psms.add(new PSM((String) psm.get("mod"), (double) psm.get("probability"),
                                (String) psm.get("run"), Math.toIntExact((Long) psm.get("specIndex")), (String) psm.get("file"),
                                (HashMap) psm.get("intensity")));
                    }

                }else{
                    psms.add(new PSM((String) psm.get("mod"), (double) psm.get("probability"),
                            (String) psm.get("label"), Math.toIntExact((Long) psm.get("specIndex")), (String) psm.get("file")));
                }


            }


        }


        JSONObject transcripts = document.get("transcripts", JSONObject.class);
        genes = new HashSet<>(transcripts.keySet());

        //intensities = new HashMap<>();
        if(Config.getRunType(runName).equals("LABELFREE")){
            JSONObject jsonIntensities = document.get("intensity", JSONObject.class);

            intensities = new HashMap<>();
            for(Object key: jsonIntensities.keySet()){
                if(jsonIntensities.get(key).getClass().equals(Double.class)){
                    intensities.put((String) key, (Double) jsonIntensities.get(key));
                }else{
                    intensities.put((String) key, ((Long) jsonIntensities.get(key)).doubleValue());
                }
            }
        }else{
            JSONObject jsonIntensities = (JSONObject) document.get("intensity", JSONObject.class).get(runName);
            intensities = (HashMap) jsonIntensities;
        }

//        for(Object o:  jsonIntensities.entrySet()){
//            Map.Entry<String, String> entry = (Map.Entry<String, String>) o;
//
//        }


        if(run.getType().equals("SILAC")){
            this.probability = (Double) document.get("probability");
        }else if(run.getType().equals("LABELFREE")){
            this.probability = (Double) document.get("probability");
        }else {
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

//    public HashMap<String, Double> getIntensities() {
//        return intensities;
//    }
    public HashMap<String, Double> getIntensities() {
        HashMap<String, Double> psmIntensities = new HashMap<>();

        for(PSM psm: psms){
            for(Map.Entry<String, Double> entry: psm.getIntensities().entrySet()){
                if(!psmIntensities.containsKey(entry.getKey()))
                    psmIntensities.put(entry.getKey(), 0.);
                psmIntensities.replace(entry.getKey(), psmIntensities.get(entry.getKey())+entry.getValue());
            }
        }


        return psmIntensities;

    }

    public HashMap<String, Double> getIntensities(HashSet<PTM> ptms) {
        HashMap<String, Double> psmIntensities = new HashMap<>();

        for(PSM psm: psms){

            if(psm.contains(ptms)){
                for(Map.Entry<String, Double> entry: psm.getIntensities().entrySet()){
                    if(!psmIntensities.containsKey(entry.getKey()))
                        psmIntensities.put(entry.getKey(), 0.);
                    psmIntensities.replace(entry.getKey(), psmIntensities.get(entry.getKey())+entry.getValue());
                }
            }



        }


        return psmIntensities;

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

    public double calculateFoldChange(String condA, String condB){
        ArrayList<Double> condAIntensities = new ArrayList<>();
        ArrayList<Double> condBIntensities = new ArrayList<>();

        HashMap<String, Double> intensities = getIntensities();

        if(run.isCombined()){
            if(Config.getRunType(run.getName()).equals("LABELFREE")){
                for(MSRun subrun: run.getRuns()){
                    if(Config.getRunOrLabelCondition(subrun.getName()).equals(condA)){
                        condAIntensities.add(intensities.get(subrun.getName()));
                    }else if(Config.getRunOrLabelCondition(subrun.getName()).equals(condB)){
                        condBIntensities.add(intensities.get(subrun.getName()));
                    }
                }

                OptionalDouble averageA = condAIntensities
                        .stream()
                        .mapToDouble(a -> a)
                        .average();
                OptionalDouble averageB = condBIntensities
                        .stream()
                        .mapToDouble(a -> a)
                        .average();

                if(averageA.isPresent() && averageB.isPresent()){
                    if(averageB.getAsDouble()!=0.){
                        return averageA.getAsDouble()/averageB.getAsDouble();
                    }
                }
            }
        }else{

        }

        return 0.;


    }


}
