package utilities;

import Cds.PSM;
import Cds.Peptide;
import Controllers.PeptideTableController;
import javafx.application.Platform;
import javafx.util.Pair;
import org.dizitart.no2.Cursor;
import org.dizitart.no2.Document;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteCollection;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import pitguiv2.Config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.dizitart.no2.filters.Filters.eq;
import static org.dizitart.no2.filters.Filters.in;

public class MSRun {


    private String name;
    private boolean isCombined;
    private boolean hasQuantification;
    private HashMap<String, Peptide> allPeptides = new HashMap<>();
    private Set<String> channels;
    private ArrayList<MSRun> subRuns;
    private String output;
    HashMap<String, HashMap<Integer, Long>> mzmlIndexes = new HashMap();

    public MSRun(String name, String output) {
        this.name = name;
        this.output = output;
    }

    public void load(Nitrite db, String output, String runName, PeptideTableController controller, Peptide peptideToFind, Config config){
        NitriteCollection collection = db.getCollection("peptideMap");
        Cursor cursor = collection.find(eq("run", runName));

        hasQuantification = config.hasQuantification(runName);
        if(hasQuantification){
            channels = config.getRunSamples(runName);
        }

        isCombined = config.isCombinedRun(runName);
        if(isCombined){
            subRuns = new ArrayList<>();
            for(Object subrunName: config.getCombinedRuns(runName)){
                subRuns.add(new MSRun((String) subrunName, config.getOutputPath()));
            }
        }

        //spectrumViewerController.getMzmlIndex(runCombobox.getSelectionModel().getSelectedItem());

        allPeptides = new HashMap<>(cursor.size());

        for (Document doc : cursor) {
            Peptide peptide = null;
            for(MSRun subRuns: subRuns){
                Peptide tpmPep = subRuns.addPeptide(doc);
                if(tpmPep!=null){
                    peptide = tpmPep;
                }
            }

            if(isCombined){
                allPeptides.put(doc.get("peptide", String.class), new Peptide(doc.get("peptide", String.class), this));
            }else{
                allPeptides.put(doc.get("peptide", String.class), new Peptide(doc));
                for(Object o: doc.get("psm", JSONArray.class)){
                    JSONObject psm = (JSONObject) o;
                    String label = (String) psm.get("label");
                    String file = (String) psm.get("file");
                    if (!mzmlIndexes.containsKey(file)){
                        mzmlIndexes.put(file, new HashMap<>());
                        loadIndex(output, file);
                    }
                }
            }

            if(peptideToFind!=null && peptideToFind.getSequence().equals(peptide.getSequence())){
                Peptide finalPeptide = peptide;
                Platform.runLater(() -> controller.selectPeptide(finalPeptide));
            }

        }



    }

    private void loadIndex(String output,  String file){
        String path = output+"/ms/"+name+"/files/"+file+".mzML.index";
        try {
            Files.lines(Path.of(path)).forEach(line -> {
                String[] lineSplit = line.split(",");

                if(!mzmlIndexes.containsKey(file)){
                    mzmlIndexes.put(file, new HashMap<>());
                }

                mzmlIndexes.get(file).put(Integer.parseInt(lineSplit[0]), Long.parseLong(lineSplit[1]));
            });

        } catch (IOException e) {
            e.printStackTrace();
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

    public Peptide addPeptide(Document doc){
        Peptide peptide =  new Peptide(doc, name);


        for(PSM psm : peptide.getPsms()){
            if(!mzmlIndexes.containsKey(psm.getFile())){
                loadIndex(output, psm.getFile());
            }
        }

        if(peptide.getProbability()!=null){
            allPeptides.put(peptide.getSequence(), peptide);
        }

        return peptide;

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

        if(isCombined){
            for(MSRun subrun: subRuns){
                Peptide pep = subrun.getPeptide(peptide);
                if(pep!=null){
                    for(Map.Entry<String, Double> entry: pep.getIntensities().entrySet()){
                        if(!intensities.containsKey(entry.getKey())){
                            intensities.put(entry.getKey(), entry.getValue());
                        }else{
                            intensities.replace(entry.getKey(), intensities.get(entry.getKey())+entry.getValue());
                        }
                    }
                    nbPeptides++;
                }
            }
            for(Map.Entry<String, Double> entry: intensities.entrySet()){
                intensities.replace(entry.getKey(), intensities.get(entry.getKey())/nbPeptides);
            }
            return intensities;

        }else{
            return allPeptides.get(peptide).getIntensities();
        }
    }
}
