package Controllers.config_generation;

import Controllers.FXMLDocumentController;
import Controllers.MSControllers.PAGController;
import Controllers.PITrun.PITRunLocalController;
import Controllers.SettingsController;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;

public class ConfigGenerationController implements Initializable {


    @FXML
    private AnchorPane container;


    private SampleConfigController sampleConfigController;
    private Parent sampleConfigRoot;

    private MSConfigController msConfigController;
    private Parent msConfigRoot;

    private static ConfigGenerationController instance;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        instance = this;

        FXMLLoader fxmlLoader = new FXMLLoader(SettingsController.class.getResource("/sampleconfig.fxml"));
        try {
            sampleConfigRoot = fxmlLoader.load();
            sampleConfigController = fxmlLoader.getController();
            container.getChildren().add(sampleConfigRoot);

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void showMSConfig(){
        container.getChildren().clear();
        FXMLLoader fxmlLoader = new FXMLLoader(SettingsController.class.getResource("/msconfig.fxml"));
        try {
            msConfigRoot = fxmlLoader.load();
            msConfigController = fxmlLoader.getController();
            container.getChildren().add(msConfigRoot);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static ConfigGenerationController getInstance() {
        return instance;
    }

    public void generateConfigFile(){
        ObservableList<Sample> samples = sampleConfigController.getSamples();
        JSONObject json = new JSONObject();

        JSONObject conditions = new JSONObject();
        for(Sample sample: samples){
            if(!conditions.has(sample.getCondition()))
                conditions.put(sample.getCondition(), new JSONObject());

            JSONObject sampleObject = new JSONObject();
            if(sample.getLeft()!=null){
                sampleObject.put("left", sample.getLeft());
                sampleObject.put("right", sample.getRight());
            }else{
                sampleObject.put("single", sample.getSingle());
            }
            conditions.getJSONObject(sample.getCondition()).put(sample.getName(), sampleObject);
        }

        json.put("conditions", conditions);
        json.put("ref_condition", sampleConfigController.getRefCondition());
        json.put("threads", sampleConfigController.getThreads()>0?sampleConfigController.getThreads():8);
        json.put("output", sampleConfigController.getOutput()+"/"+sampleConfigController.getProjectName());


        if(sampleConfigController.getRefGtf()!=null)
            json.put("reference_gff", sampleConfigController.getRefGtf());
        if(sampleConfigController.getRefFasta()!=null)
            json.put("reference_fasta", sampleConfigController.getRefFasta());


        JSONObject ms = new JSONObject();
        HashMap<String, ArrayList<String>> combinedRuns = new HashMap<>();
        JSONObject runs = new JSONObject();
        for(MSRunConfig run: MSConfigController.getInstance().getRuns()){
            if(run.getCombinedRun()!=null) {
                if (!combinedRuns.containsKey(run.getCombinedRun()))
                    combinedRuns.put(run.getCombinedRun(), new ArrayList<>());
                combinedRuns.get(run.getCombinedRun()).add(run.getName());
            }

            JSONObject runObj = new JSONObject();
            JSONArray files = new JSONArray();
            for(String file: run.getFiles()){
                files.put(file);
            }
            runObj.put("files", files);
            if(run.getLabelType().equals("TMT")){
                JSONObject labels = new JSONObject();
                for(Map.Entry<String, String> entry: run.getLabels().entrySet()){
                    labels.put(entry.getKey(), entry.getValue());
                }
                runObj.put("TMT", labels);
            }else if(run.getLabelType().equals("SILAC")){
                JSONObject labels = new JSONObject();
                for(Map.Entry<String, String> entry: run.getLabels().entrySet()){
                    if(!entry.getValue().equals("None")) {
                        JSONObject conditionObj = new JSONObject();
                        JSONArray samplesConditionRun = new JSONArray();
                        for (Sample sample : SampleConfigController.getInstance().getSamples()) {
                            if (sample.getCondition().equals(entry.getValue())) {
                                samplesConditionRun.put(sample.getName());
                            }
                        }
                        conditionObj.put("samples", samplesConditionRun);

                        JSONArray conditionLabels = new JSONArray();
                        if (entry.getKey().equals("Medium")) {
                            conditionLabels.put("Lys4");
                            conditionLabels.put("Arg6");
                        } else if (entry.getKey().equals("Heavy")) {
                            conditionLabels.put("Lys8");
                            conditionLabels.put("Arg10");
                        }
                        conditionObj.put("label", conditionLabels);

                        labels.put(entry.getValue(), conditionObj);
                    }
                }
                runObj.put("SILAC", labels);
            }

            JSONObject modsObj = new JSONObject();
            JSONArray fixedMods = new JSONArray();
            if(run.getFixedMods()!=null){
                for(String mod: run.getFixedMods()){
                    fixedMods.put(mod);
                }
            }
            JSONArray variableMods = new JSONArray();
            if(run.getVariableMods()!=null){
                for(String mod: run.getVariableMods()){
                    variableMods.put(mod);
                }
            }
            modsObj.put("fixed", fixedMods);
            modsObj.put("variable", variableMods);
            runObj.put("modifications", modsObj);

            runs.put(run.getName(), runObj);
        }

        if(!combinedRuns.isEmpty()){
            JSONObject allCombinedObj = new JSONObject();
            for(Map.Entry<String, ArrayList<String>> entry: combinedRuns.entrySet()){
                JSONObject combinedObj = new JSONObject();
                JSONArray runsObj = new JSONArray();
                for(String run: entry.getValue()){
                    runsObj.put(run);
                }
                combinedObj.put("runs", runsObj);
                allCombinedObj.put(entry.getKey(), combinedObj);
            }
            ms.put("combined", allCombinedObj);

        }
        ms.put("runs", runs);

        json.put("ms", ms);

        json.put("dge", SampleConfigController.getInstance().runDge());
        json.put("splicing", SampleConfigController.getInstance().runSplicing());
        json.put("mutations", SampleConfigController.getInstance().runMutations());

        try {
            File theDir = new File(sampleConfigController.getOutput()+"/"+sampleConfigController.getProjectName());
            if (!theDir.exists()){
                theDir.mkdirs();
            }
            FileWriter file = new FileWriter(sampleConfigController.getOutput()+"/"+sampleConfigController.getProjectName()+"/config.json");
            file.write(json.toString(4));
            file.close();

            showRunPane(sampleConfigController.getOutput()+"/"+sampleConfigController.getProjectName()+"/config.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void backToSamples() {
        container.getChildren().clear();
        container.getChildren().add(sampleConfigRoot);
    }

    public void showRunPane(String configPath){
        FXMLLoader fxmlLoader = new FXMLLoader(SettingsController.class.getResource("/pitrunlocal.fxml"));
        try {
            sampleConfigRoot = fxmlLoader.load();
            PITRunLocalController pitRunLocalController = fxmlLoader.getController();
            pitRunLocalController.setConfig(configPath);
            container.getChildren().clear();
            container.getChildren().add(sampleConfigRoot);

        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
