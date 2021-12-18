package Controllers.PITrun;

import Controllers.FXMLDocumentController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class PITRunLocalController implements Initializable {

    @FXML
    private TextArea logsArea;
    @FXML
    private Button cancelButton;
    @FXML
    private Button runButton;
    @FXML
    private TextField configField;
    private String containerName;


    Process process;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    public void run(){
        ProcessBuilder processBuilder = new ProcessBuilder();


        new Thread(()-> {
            JSONParser parser = new JSONParser();


            try {
                JSONObject a = (JSONObject) parser.parse(new FileReader(configField.getText()));
                String output = (String) a.get("output");
                processBuilder.command("python3", "/home/esteban/Documents/PIT/pit_docker/LaunchDocker.py", "-c", configField.getText());
                process = processBuilder.start();
                process.waitFor();

                containerName = Paths.get(output).getFileName().toString();
                processBuilder.command("docker", "run",  "--name", containerName, "-v", Paths.get(output) + ":/project/", "pit");
                process = processBuilder.start();

                cancelButton.setVisible(true);
                runButton.setVisible(false);


                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder builder = new StringBuilder();
                String line = null;
                while ( (line = reader.readLine()) != null) {
                    builder.append(line);
                    builder.append(System.getProperty("line.separator"));
                    logsArea.setText(builder.toString());
                }

            } catch (IOException | ParseException | InterruptedException e) {
                e.printStackTrace();
            }

        }).start();


    }
    @FXML
    public void chooseConfig() {
        FileChooser directoryChooser = new FileChooser();
        File selectedDirectory = directoryChooser.showOpenDialog(FXMLDocumentController.getInstance().getStage());
        if(selectedDirectory!=null)
            configField.setText(selectedDirectory.getAbsolutePath());
    }
    @FXML
    public void cancel() {
        process.destroy();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("docker", "kill", containerName);
            Process p = processBuilder.start();
            p.waitFor();
            processBuilder.command("docker", "rm", containerName);
            p = processBuilder.start();
            p.waitFor();
            cancelButton.setVisible(false);
            runButton.setVisible(true);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setConfig(String path){
        configField.setText(path);
    }
}
