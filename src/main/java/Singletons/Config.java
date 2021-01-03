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

        if(config.getJSONObject("mzml").getJSONObject("combine").has(run)) {
            JSONObject runObj = config.getJSONObject("mzml").getJSONObject("runs")
                    .getJSONObject(config.getJSONObject("mzml").getJSONObject("combine")
                            .getJSONObject(run).getJSONArray("runs").getString(0));

            if(runObj.has("TMT")){
                return runObj.getJSONObject("TMT").keySet();
            }else return null;

        }else if(config.getJSONObject("mzml").getJSONObject(run).keySet().contains("SILAC")){
            return config.getJSONObject("mzml").getJSONObject(run).getJSONObject("SILAC").keySet();
        }else if(config.getJSONObject("mzml").getJSONObject(run).keySet().contains("TMT")){
            return config.getJSONObject("mzml").getJSONObject(run).getJSONObject("TMT").keySet();
        }else{
            JSONObject conditionsObj = config.getJSONObject("conditions");
            Iterator<String> condKeys = conditionsObj.keys();

           HashSet<String> runSamples = new HashSet<>();

            while(condKeys.hasNext()) {
                String condition = condKeys.next();
                if (conditionsObj.get(condition) instanceof JSONObject) {
                    JSONObject samplesObj = conditionsObj.getJSONObject(condition).getJSONObject("samples");

                    Iterator<String> samplesKeys = samplesObj.keys();
                    while(samplesKeys.hasNext()) {
                        String sample = samplesKeys.next();
                        if (samplesObj.get(sample) instanceof JSONObject) {
                            JSONObject sampleObj = samplesObj.getJSONObject(sample);

                            JSONArray mzmls = sampleObj.getJSONArray("mzml");
                            for(Object o: mzmls){
                                String mzml = (String) o;
                                if(mzml.equals(run)){
                                    runSamples.add(condition+"/"+sample);
                                }

                            }

                        }
                    }

                }
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
        return config.getString("reference_fasta");
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
            }
            return null;
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
}

