package utilities;

import Cds.PTM;
import Cds.Peptide;
import Controllers.PeptideTableController;
import javafx.application.Platform;
import org.dizitart.no2.Cursor;
import org.dizitart.no2.Document;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteCollection;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import Singletons.Config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.dizitart.no2.filters.Filters.eq;

public class MSRun {


    private String name;
    private boolean isCombined;
    private boolean hasQuantification;
    private HashMap<String, Peptide> allPeptides = new HashMap<>();
    private Set<String> channels;
    private ArrayList<MSRun> subRuns;
    private String output;
    HashMap<String, HashMap<Integer, Long>> mzmlIndexes = new HashMap();
    private String type;

    public MSRun(String name, String output) {
        this.name = name;
        this.output = output;
    }

    public MSRun(String name) {
        this.name = name;
    }

    public MSRun(String name, String output, String type) {
        this.name = name;
        this.output = output;
        this.type = type;
    }

    public void load(Nitrite db, String runName, Peptide peptideToFind, String conditionA, String conditionB){
        NitriteCollection collection = db.getCollection("peptideMap");
        Cursor cursor = collection.find(eq("run", runName));

        hasQuantification = Config.hasQuantification(runName);
        if(hasQuantification){
            channels = Config.getRunSamples(runName);
        }

        isCombined = Config.isCombinedRun(runName);
        this.type = Config.getRunType(runName);
        if(isCombined){
            subRuns = new ArrayList<>();
            for(Object subrunName: Config.getCombinedRuns(runName)){
                subRuns.add(new MSRun((String) subrunName, Config.getOutputPath(), type));
            }
        }

        //spectrumViewerController.getMzmlIndex(runCombobox.getSelectionModel().getSelectedItem());

        allPeptides = new HashMap<>(cursor.size());

        for (Document doc : cursor) {



            Peptide peptide = new Peptide(doc);

            if(conditionA!=null && conditionB!=null)
                peptide.calculateFoldChange(conditionA, conditionB);

            allPeptides.put(doc.get("peptide", String.class), peptide);
            for(Object o: doc.get("psms", JSONArray.class)){
                JSONObject psm = (JSONObject) o;
                String label = (String) psm.get("label");
                String file = (String) psm.get("file");
                if (!mzmlIndexes.containsKey(file)){
                    mzmlIndexes.put(file, new HashMap<>());
                    loadIndex(file);
                }
            }

            if(peptideToFind!=null && peptideToFind.getSequence().equals(peptide.getSequence())){
                Peptide finalPeptide = peptide;
                Platform.runLater(() -> PeptideTableController.getInstance().selectPeptide(finalPeptide));
            }

        }



    }

    private void loadIndex(String file){
        String path = Config.getRunPath(name)+"/"+file+".mzML.index";
        try {
            Files.lines(Path.of(path)).forEach(line -> {
                String[] lineSplit = line.split(",");

                if(!mzmlIndexes.containsKey(file)){
                    mzmlIndexes.put(file, new HashMap<>());
                }

                mzmlIndexes.get(file).put(Integer.parseInt(lineSplit[0]), Long.parseLong(lineSplit[1]));
            });

        } catch (IOException e) {

        }
    }

    public Collection<Peptide> getAllPeptides() {

        return allPeptides.values();

    }

    public Set<String> getChannels(){
        return channels;
    }

    public String getName() {
        return name;
    }


    public ArrayList<MSRun> getRuns(){
        if(isCombined){
            return subRuns;
        }
        ArrayList<MSRun> run = new ArrayList<>();
        run.add(this);
        return run;
    }

    public double getPeptideProbability(String peptide){
        return allPeptides.get(peptide).getProbability();
    }

    public Peptide getPeptide(String sequence){
        if(!allPeptides.containsKey(sequence)) return null;
        return allPeptides.get(sequence);
    }

    public boolean isCombined() {
        return isCombined;
    }

    
    public HashMap<Integer, Long> getIndex(String file){
        return mzmlIndexes.get(file);
    }

    public HashMap<String, Double> getIntensities(String peptide){

        HashMap<String, Double> intensities = new HashMap<>();
        int nbPeptides = 0;
        if(Config.getRunType(name).equals("LABELFREE"))
            nbPeptides=1;

        return allPeptides.get(peptide).getIntensities(name);

//        if(isCombined){
//            for(MSRun subrun: subRuns){
//                Peptide pep = subrun.getPeptide(peptide);
//                if(pep!=null){
//                    for(Map.Entry<String, Double> entry: pep.getIntensities(subrun.getName()).entrySet()){
//
//
//
//
//                        if(!intensities.containsKey(entry.getKey())){
//                            intensities.put(entry.getKey(), entry.getValue());
//                        }else{
//                            intensities.replace(entry.getKey(), intensities.get(entry.getKey())+entry.getValue());
//                        }
//                    }
//                    if(!Config.getRunType(name).equals("LABELFREE"))
//                        nbPeptides++;
//                }
//            }
//            for(Map.Entry<String, Double> entry: intensities.entrySet()){
//                intensities.replace(entry.getKey(), intensities.get(entry.getKey())/nbPeptides);
//            }
//            return intensities;
//
//        }else{
//            return allPeptides.get(peptide).getIntensities(name);
//        }
    }

    public HashMap<String, Double> getIntensities(String peptide, HashSet<PTM> ptms){

        HashMap<String, Double> intensities = new HashMap<>();
        int nbPeptides = 0;
        if(Config.getRunType(name).equals("LABELFREE"))
            nbPeptides=1;

        if(isCombined){
            for(MSRun subrun: subRuns){
                Peptide pep = subrun.getPeptide(peptide);
                if(pep!=null){
                    for(Map.Entry<String, Double> entry: pep.getIntensities(ptms, subrun.getName()).entrySet()){




                        if(!intensities.containsKey(entry.getKey())){
                            intensities.put(entry.getKey(), entry.getValue());
                        }else{
                            intensities.replace(entry.getKey(), intensities.get(entry.getKey())+entry.getValue());
                        }
                    }
                    if(!Config.getRunType(name).equals("LABELFREE"))
                        nbPeptides++;
                }
            }
            for(Map.Entry<String, Double> entry: intensities.entrySet()){
                intensities.replace(entry.getKey(), intensities.get(entry.getKey())/nbPeptides);
            }
            return intensities;

        }else{
            //return allPeptides.get(peptide).getIntensities();
            return null;
        }
    }

    public String getType() {
        return type;
    }
}
