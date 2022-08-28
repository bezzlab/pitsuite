package Controllers;

import com.jfoenix.controls.JFXListView;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import org.json.JSONTokener;
import org.json.simple.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import pitguiv2.Settings;


import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Scanner;


public class SettingsController implements Initializable {


    @FXML
    private GridPane browserPane;
    @FXML
    private Spinner minSashimiReads;
    @FXML
    private GridPane fontPane;
    @FXML
    private Spinner browserFontSizeSpinner;
    @FXML
    private Spinner generalFontSizeSpinner;
    @FXML
    private Spinner chartsFontSizeSpinner;

    @FXML
    private GridPane externalToolsPane;
    @FXML
    private TextField pathToRField;



    @FXML
    private JFXListView<String> categoriesListview;

    private JSONObject settings;
    private ResultsController parent;

    private GridPane currentPane;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        categoriesListview.getItems().add("Fonts");
        categoriesListview.getItems().add("External tools");
        categoriesListview.getItems().add("Browser");

        currentPane = fontPane;

        categoriesListview.setOnMouseClicked(c -> {
            currentPane.setVisible(false);
            if (categoriesListview.getSelectionModel().getSelectedItem().equals("Fonts")){
                currentPane = fontPane;
            }else if (categoriesListview.getSelectionModel().getSelectedItem().equals("External tools")){
                currentPane = externalToolsPane;
            }else if (categoriesListview.getSelectionModel().getSelectedItem().equals("Browser")){
                currentPane = browserPane;
            }
            currentPane.setVisible(true);
        });
        readSettings();
        setProperties();

    }


    @FXML
    public void applyChanges() {
        settings.getJSONObject("Fonts").getJSONObject("browser").put("size", browserFontSizeSpinner.getValueFactory().getValue());
        settings.getJSONObject("Fonts").getJSONObject("general").put("size", generalFontSizeSpinner.getValueFactory().getValue());
        settings.getJSONObject("Fonts").getJSONObject("charts").put("size", chartsFontSizeSpinner.getValueFactory().getValue());
        settings.getJSONObject("Browser").put("minSashimiReads", minSashimiReads.getValueFactory().getValue());

        settings.getJSONObject("tools").put("R", pathToRField.getText());

        Settings.getInstance().setSetting(settings);

        try (FileWriter file = new FileWriter("./settings.json")) {

            file.write(settings.toString(4));
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }


        parent.updateSetting(settings);
    }

    public void setParentController(ResultsController parent){
        this.parent = parent;
    }


    private void readSettings(){

        try {
            Scanner in = new Scanner(new FileReader("./settings.json"));
            StringBuilder sb = new StringBuilder();
            while(in.hasNext()) {
                sb.append(in.next());
            }

            settings = new JSONObject(sb.toString());
            Settings.getInstance().setSetting(settings);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setProperties(){

        browserFontSizeSpinner.getValueFactory().setValue(settings.getJSONObject("Fonts").getJSONObject("browser").getInt("size"));
        generalFontSizeSpinner.getValueFactory().setValue(settings.getJSONObject("Fonts").getJSONObject("general").getInt("size"));
        chartsFontSizeSpinner.getValueFactory().setValue(settings.getJSONObject("Fonts").getJSONObject("charts").getInt("size"));

        minSashimiReads.getValueFactory().setValue(settings.getJSONObject("Browser").getInt("minSashimiReads"));

        if (settings.getJSONObject("tools").getString("R").length()>0){
            pathToRField.setText(settings.getJSONObject("tools").getString("R"));
        }else{
            if(System.getProperty("os.name").toUpperCase().contains("WIN")){
                File f = new File("C:\\Program Files\\R");
                if(f.exists()) {

                    String[] names = f.list();

                    for(String name : names)
                    {
                        if (new File("C:\\Program Files\\R\\" + name).isDirectory())
                        {
                            pathToRField.setText("C:\\Program Files\\R\\" + name + "\\bin\\Rscript.exe");
                            applyChanges();
                        }
                        break;
                    }




                }
            }else{
                File f = new File("/usr/bin/Rscript");
                if(f.exists()) {
                    pathToRField.setText("/usr/bin/Rscript");
                    applyChanges();
                }
            }
        }
    }


}



