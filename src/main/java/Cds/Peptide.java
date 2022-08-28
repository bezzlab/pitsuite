package Cds;

import Singletons.Config;
import javafx.util.Pair;
import org.dizitart.no2.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utilities.MSRun;

import java.util.*;

public class Peptide {


    private String sequence;
    private HashMap<String, ArrayList<PSM>> psms;
    private Double probability;
    HashSet<String> genes;
    HashMap<String, HashMap<String, Double>> intensities;
    MSRun run;
    double foldChange;


    public Peptide(String sequence){
        this.sequence = sequence;
        psms = new HashMap<>();
    }

    public Peptide(String sequence, MSRun run){
        this.sequence = sequence;
        psms = new HashMap<>();
        this.run = run;
        psms.put(run.getName(), new ArrayList<>());
    }


    public Peptide(Document document){
        this.sequence = document.get("peptide", String.class);
        this.probability = 1.;
        this.run = new MSRun((String) document.get("run"));

        psms = new HashMap<>();

        JSONArray docPsms = document.get("psms", JSONArray.class);
        for(Object o: docPsms){
            JSONObject psm = (JSONObject) o;

            String run = (String) psm.get("run");
            if(!run.equals("nan")) {
                if (!psms.containsKey(run))
                    psms.put(run, new ArrayList());


                if (psm.containsKey("label")) {
                    psms.get(run).add(new PSM((String) psm.get("mod"), (double) psm.get("probability"),
                            (String) psm.get("label"), Math.toIntExact((Long) psm.get("specIndex")), (String) psm.get("file")));
                } else {
                    if (psm.containsKey("intensity")) {
                        HashMap<String, Double> intensity = new HashMap<>();

                        if (psm.get("intensity").getClass().equals(Long.class))
                            intensity.put((String) psm.get("run"), 0.);
                        else if (Config.getRunType((String) psm.get("run")).equals("LABELFREE"))
                            intensity.put((String) psm.get("run"), (Double) psm.get("intensity"));
                        else
                            intensity = (HashMap) psm.get("intensity");

                        psms.get(run).add(new PSM((String) psm.get("mod"), (double) psm.get("probability"), run,
                                Math.toIntExact((Long) psm.get("specIndex")), (String) psm.get("file"), intensity));
                    } else {
                        psms.get(run).add(new PSM((String) psm.get("mod"), (double) psm.get("probability"),
                                Math.toIntExact((Long) psm.get("specIndex")), (String) psm.get("file")));
                    }

                }
            }


        }

        JSONObject transcripts = document.get("transcripts", JSONObject.class);
        genes = new HashSet<>(transcripts.keySet());

        JSONObject jsonIntensities = document.get("intensity", JSONObject.class);
        intensities = (HashMap) jsonIntensities;

    }




    public String getSequence(){ return sequence; }

    public int getNbGenes(){
        return genes.size();

    }


    public ArrayList<PSM> getPsms(){

        ArrayList<PSM> allPsm = new ArrayList<>();

        for(ArrayList<PSM> runPsms: psms.values()){
            allPsm.addAll(runPsms);
        }
        return allPsm;
    }

    public ArrayList<PSM> getPsms(String run) {
        return psms.get(run);
    }

    public boolean contains(PTM ptm, String run){
        for(PSM psm: psms.get(run)){
            if(psm.contains(ptm)){
                return true;
            }
        }
        return false;
    }

    public boolean contains(PTM ptm){

        for(ArrayList<PSM> runPsms: psms.values()){
            for(PSM psm: runPsms){
                if(psm.contains(ptm)){
                    return true;
                }
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
    public HashMap<String, Double> getIntensities(String run) {
        HashMap<String, Double> psmIntensities = new HashMap<>();

        if(Config.getRunType(run).equals("LABELFREE")){
            for(Map.Entry<String, ArrayList<PSM>> entry: psms.entrySet()){
                for(PSM psm: entry.getValue()){
                    for(Map.Entry<String, Double> entryPsm: psm.getIntensities().entrySet()){
                        if(!psmIntensities.containsKey(entryPsm.getKey()))
                            psmIntensities.put(entryPsm.getKey(), 0.);
                        psmIntensities.replace(entryPsm.getKey(), psmIntensities.get(entryPsm.getKey())+entryPsm.getValue());
                    }
                }
            }

        }else{
            for(PSM psm: psms.get(run)){
                for(Map.Entry<String, Double> entry: psm.getIntensities().entrySet()){
                    if(!psmIntensities.containsKey(entry.getKey()))
                        psmIntensities.put(entry.getKey(), 0.);
                    psmIntensities.replace(entry.getKey(), psmIntensities.get(entry.getKey())+entry.getValue());
                }
            }
        }



        return psmIntensities;

    }

    public HashMap<String, Double> getIntensities(HashSet<PTM> ptms, String run) {
        HashMap<String, Double> psmIntensities = new HashMap<>();

        for(PSM psm: psms.get(run)){

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


    public boolean hasPSM(){
        return psms.size()>0;
    }

    public double calculateFoldChange(String condA, String condB){
        ArrayList<Double> condAIntensities = new ArrayList<>();
        ArrayList<Double> condBIntensities = new ArrayList<>();

        //HashMap<String, Double> intensities = getIntensities(run.getName());

        if(Config.isCombinedRun(run.getName())){
//            if(Config.getRunType(run.getName()).equals("LABELFREE")){
//                for(MSRun subrun: run.getRuns()){
//                    if(Config.getRunOrLabelCondition(subrun.getName()).equals(condA)){
//                        condAIntensities.add(intensities.get(subrun.getName()));
//                    }else if(Config.getRunOrLabelCondition(subrun.getName()).equals(condB)){
//                        condBIntensities.add(intensities.get(subrun.getName()));
//                    }
//                }
//
//                OptionalDouble averageA = condAIntensities
//                        .stream()
//                        .mapToDouble(a -> a)
//                        .average();
//                OptionalDouble averageB = condBIntensities
//                        .stream()
//                        .mapToDouble(a -> a)
//                        .average();
//
//                if(averageA.isPresent() && averageB.isPresent()){
//                    if(averageB.getAsDouble()!=0.){
//                        return averageA.getAsDouble()/averageB.getAsDouble();
//                    }
//                }
//            }
            if(Config.getRunType(run.getName()).equals("SILAC")){

                for(String subrun: Config.getSubRuns(run.getName())){
                    if(psms.containsKey(subrun)){
                        HashMap<String, Double> intensities = getIntensities(subrun);


                        if(intensities.get(condA)!=null)
                            condAIntensities.add(intensities.get(condA));
                        if(intensities.get(condB)!=null)
                            condBIntensities.add(intensities.get(condB));

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
                        foldChange = Math.log10(averageA.getAsDouble()/averageB.getAsDouble())/Math.log10(2);
                        return averageA.getAsDouble()/averageB.getAsDouble();
                    }
                }


            }
        }else{

        }

        return 0.;


    }

    public Set<String> getRuns(){
        return psms.keySet();
    }


    public void addPsm(PSM psm, String run) {
        if(!run.equals("nan"))
            if(!psms.containsKey(run))
                psms.put(run, new ArrayList<>());
            psms.get(run).add(psm);
    }

    public double getFoldChange(){return foldChange;}

    public Pair<Integer, Integer> getPosInCds(CDS cds){
        int start = cds.getSequence().indexOf(sequence);
        if(start!=-1){
            return new Pair<>(start+1, start+sequence.length());
        }else
            return null;
    }
}
