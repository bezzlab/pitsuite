package Singletons;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class Config {
    private static JSONObject config;
    private static Config single_instance = null;

    public Config() {

    }

    public JSONObject getConfig() {
        return config;
    }

    public static Config getInstance()
    {
        if (single_instance == null)
            single_instance = new Config();

        return single_instance;
    }

    public static void setConfigDocument(JSONObject document){
        config = document;
    }

    public static Set<String> getConditions(){
        return config.getJSONObject("conditions").keySet();
    }
    public static Set<String> getSamplesInCondition(String condition){
        return config.getJSONObject("conditions").getJSONObject(condition).getJSONObject("samples").keySet();
    }


    public static Set<String> getRuns(){

        HashSet<String> runsProcessed = new HashSet<>();
        HashSet<String> runsAdded = new HashSet<>();



        if(config.getJSONObject("mzml").has("combine")){

            JSONObject combinedRuns = config.getJSONObject("mzml").getJSONObject("combine");

            for (String run : combinedRuns.keySet()) {
                JSONObject runObj = combinedRuns.getJSONObject(run);
                runsAdded.add(run);
                for(Object o2: runObj.getJSONArray("runs")){
                    String run2 = (String) o2;
                    runsProcessed.add(run2);
                }
            }
        }

        for(String run:config.getJSONObject("mzml").getJSONObject("runs").keySet()){
            if(!runsProcessed.contains(run)){
                runsAdded.add(run);
            }
        }

        return runsAdded;
    }

    public static boolean hasQuantification(String run){

//        if(config.getJSONObject("mzml").has("combine") && config.getJSONObject("mzml").getJSONObject("combine").has(run)){
//            return config.getJSONObject("mzml").getJSONObject("runs")
//                    .getJSONObject(config.getJSONObject("mzml").getJSONObject("combine").getJSONObject(run)
//                            .getJSONArray("runs").getString(0)).keySet().contains("TMT");
//        }else{
//            return config.getJSONObject("mzml").getJSONObject("runs").getJSONObject(run).keySet().contains("TMT");
//        }
        return true;

    }

    public static Set<String> getRunSamples(String run){

        if(config.getJSONObject("mzml").has("combine") &&
                config.getJSONObject("mzml").getJSONObject("combine").has(run)) {
            JSONObject runObj = config.getJSONObject("mzml").getJSONObject("runs")
                    .getJSONObject(config.getJSONObject("mzml").getJSONObject("combine")
                            .getJSONObject(run).getJSONArray("runs").getString(0));

            if(runObj.has("TMT")){
                return runObj.getJSONObject("TMT").keySet();
            }else return null;

        }else if(config.getJSONObject("mzml").getJSONObject("runs").getJSONObject(run).keySet().contains("SILAC")){
            return config.getJSONObject("mzml").getJSONObject("runs").getJSONObject(run).getJSONObject("SILAC").keySet();
        }else if(config.getJSONObject("mzml").getJSONObject("runs").getJSONObject(run).keySet().contains("TMT")){
            return config.getJSONObject("mzml").getJSONObject("runs").getJSONObject(run).getJSONObject("TMT").keySet();
        }else{
            JSONObject conditionsObj = config.getJSONObject("conditions");

           HashSet<String> runSamples = new HashSet<>();

           String condition = config.getJSONObject("mzml").getJSONObject("runs").getJSONObject(run).getString("condition");
           if(config.getJSONObject("mzml").getJSONObject("runs").getJSONObject(run).has("sample")){
               runSamples.add(condition+"/"+config.getJSONObject("mzml").getJSONObject("runs").getJSONObject(run).getString("sample"));
           }else{
               runSamples.add(condition+"/1");
           }

            return runSamples;
        }
    }

    public static String getOutputPath(){
        return config.getString("output");
    }

    public String getMsRunFilePath(String run){
        return config.getJSONObject("mzml").getJSONObject(run).getString("files");
    }

    public static String getFastaPath(){
        if(isReferenceGuided()){
            return config.getString("reference_fasta");
        }else{
            return config.get("output")+"/trinity_genes.fasta";
        }

    }

    public static boolean isCombinedRun(String run){
        if(!config.getJSONObject("mzml").has("combine")) return false;
        return config.getJSONObject("mzml").getJSONObject("combine").has(run);
    }

    public static List<Object> getCombinedRuns(String run){
        return config.getJSONObject("mzml").getJSONObject("combine").getJSONObject(run).getJSONArray("runs").toList();
    }

    public static String getRunType(String run){

        if(isCombinedRun(run)){
            return getRunType((String) getCombinedRuns(run).get(0));
        }else{
            if(config.getJSONObject("mzml").getJSONObject("runs").getJSONObject(run).has("SILAC")){
                return "SILAC";
            } else if(config.getJSONObject("mzml").getJSONObject("runs").getJSONObject(run).has("TMT")){
                return "TMT";
            }else{
                return "LABELFREE";
            }
        }

    }

    public static String getRunPath(String runName){
        if(config.getJSONObject("mzml").has("combine")){
            JSONObject combine = config.getJSONObject("mzml").getJSONObject("combine");
            for(String combinedRun: combine.keySet()){
                if(combine.getJSONObject(combinedRun).getJSONArray("runs").toList().contains(runName)){
                    return config.getString("output")+"/ms/"+combinedRun+"/files/";
                }
            }

        }
        return config.getString("output")+"/ms/"+runName+"/files/";
    }

    public static String getMainRun(String runName){
        if(config.getJSONObject("mzml").has("combine")){
            JSONObject combine = config.getJSONObject("mzml").getJSONObject("combine");
            for(String combinedRun: combine.keySet()){
                if(combine.getJSONObject(combinedRun).getJSONArray("runs").toList().contains(runName)){
                    return combinedRun;
                }
            }
        }
        return runName;
    }

    public static boolean isReferenceGuided(){
        return config.has("reference_fasta");
    }

    public static String getRunOrLabelCondition(String runName){

        if(getRunType(runName).equals("LABELFREE")){
            return config.getJSONObject("mzml").getJSONObject("runs").getJSONObject(runName).getString("condition");
        }
        return null;
    }


    public static ArrayList<String> getSubRuns(String runName){
        ArrayList<String> subRuns = new ArrayList<>();
        if(config.getJSONObject("mzml").has("combine")){
            for(Object o :config.getJSONObject("mzml").getJSONObject("combine")
                    .getJSONObject(runName).getJSONArray("runs")){
                subRuns.add((String) o);
            }
        }else{
            subRuns.add(runName);
        }

        return subRuns;
    }

    public static boolean haveReplicates(String[] conditions){
        for(String condition: conditions){
            if (getSamplesInCondition(condition).size()==1)
                return false;
        }
        return true;
    }

    public static String getReferenceMSCondition(String runName){
        if(isCombinedRun(runName)){
            if(config.getJSONObject("mzml").getJSONObject("combine").has("reference"))
                return config.getJSONObject("mzml").getJSONObject("combine").getString("reference");
            else{
                return new TreeSet<>(getRunSamples(runName)).iterator().next();
            }
        }else{
            if(config.getJSONObject("mzml").getJSONObject("runs").getJSONObject(runName).has("reference"))
                return config.getJSONObject("mzml").getJSONObject("runs").getJSONObject(runName).getString("reference");
            else{
                return new TreeSet<>(getRunSamples(runName)).iterator().next();
            }
        }
    }

    public static HashMap<String, ArrayList<String>> getPatientsGroups(){



        if(config.has("patients")){

            HashMap<String, ArrayList<String>> groups = new HashMap<>();

            Iterator<String> keys = config.getJSONObject("patients").keys();

            while(keys.hasNext()) {
                String key = keys.next();
                if (config.getJSONObject("patients").get(key) instanceof JSONArray) {
                    ArrayList<String> patients = new ArrayList<>();
                    for(Object o: config.getJSONObject("patients").getJSONArray(key)){
                        patients.add((String) o);
                    }
                    groups.put(key, patients);
                }
            }
            return groups;

        }
        return null;

    }

    public static boolean hasPatients(){
        return config.has("patients");
    }
}

