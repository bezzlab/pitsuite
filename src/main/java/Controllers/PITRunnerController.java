package Controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Pair;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import pitguiv2.App;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.spec.ECField;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PITRunnerController implements Initializable {

    @FXML
    private GridPane filesDownloadGrid;
    @FXML
    private TabPane tabPane;
    @FXML
    private VBox filesBox;
    @FXML
    private TextField nameField;
    @FXML
    private TextField configPathField;
    @FXML
    private ListView<String> projectsListview;
    @FXML
    private TextArea logsArea;

    private ScheduledFuture updateScheduler;

    private String uploadId;
    private StringBuilder logs = new StringBuilder();

    private final HashMap<String, String> projects = new HashMap<>();
    private boolean processingCompleted;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        projectsListview.setOnMouseClicked(event -> selectProject(projectsListview.getSelectionModel().getSelectedItem()));
        loadProjects();
    }

    @FXML
    public void onRun() {

    }


    public void prepareDirectory(){

        new Thread(()-> {
//            try {
//                String text = Files.readString(Paths.get("/media/esteban/b0b05e8c-7bfc-4553-ae68-f70bfe910d3e/faraz_mouse/configMouse.json"));
//                uploadFile(new File("/media/esteban/b0b05e8c-7bfc-4553-ae68-f70bfe910d3e/faraz_mouse/configMouse.json"), true, null, false);
//                JSONObject config = new JSONObject(text);
//                File output = new File(config.getString("output"));
//                if (!output.exists())
//                    output.mkdirs();
//
//                Iterator<String> conditionsKeys = config.getJSONObject("conditions").keys();
//
//                while (conditionsKeys.hasNext()) {
//                    String condition = conditionsKeys.next();
//                    if (config.getJSONObject("conditions").get(condition) instanceof JSONObject) {
//                        Iterator<String> samplesKeys = config.getJSONObject("conditions").getJSONObject(condition).keys();
//                        JSONObject samplesObj = config.getJSONObject("conditions").getJSONObject(condition);
//                        while (samplesKeys.hasNext()) {
//                            String sample = samplesKeys.next();
//                            if (samplesObj.get(sample) instanceof JSONObject) {
//                                JSONObject sampleObj = samplesObj.getJSONObject(sample);
//                                if (sampleObj.has("left"))
//                                    uploadFile(new File(sampleObj.getString("left")), false, condition + "/" + sample, false);
//                                if (sampleObj.has("right"))
//                                    uploadFile(new File(sampleObj.getString("right")), false, condition + "/" + sample, false);
//                                if (sampleObj.has("single"))
//                                    uploadFile(new File(sampleObj.getString("single")), false, condition + "/" + sample, false);
//
//                            }
//                        }
//                    }
//                }
//
//                if (config.has("reference_fasta"))
//                    uploadFile(new File(config.getString("reference_fasta")), false, null, false);
//                if (config.has("reference_gff"))
//                    uploadFile(new File(config.getString("reference_gff")), false, null, false);
//
//
//                Iterator<String> runKeys = config.getJSONObject("mzml").getJSONObject("runs").keys();
//                while (runKeys.hasNext()) {
//                    String run = runKeys.next();
//                    JSONArray files = config.getJSONObject("mzml").getJSONObject("runs").getJSONObject(run).getJSONArray("files");
//                    for (Object o : files) {
//                        String file = (String) o;
//                        uploadFile(new File(file), false, "ms/" + run, false);
//                    }
//                }
//
//
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            runPIT();
        }).start();

    }

    public void uploadFile(File textFile, boolean isConfigFile, String path, boolean isChunk){

        int maxSize = 2 * 1024 * 1024 * 1000;
        if(textFile.length()>maxSize) {
            try {
                List<File> chunks =  splitFiles2(textFile);
                logs.append("Uploading ").append(textFile.getAbsolutePath()).append("\n");
                Platform.runLater(() -> logsArea.setText(logs.toString()));
                for(File chunk: chunks){
                    uploadFile(chunk, false, path, true);
                }
                for(File chunk: chunks){
                    chunk.delete();
                }
                closeChunks();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else{
            String url = "http://localhost:5000/upload";
            String charset = "UTF-8";
            String param = "value";
            //File textFile = new File("/path/to/file.txt");
            File binaryFile = new File("/path/to/file.bin");
            String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
            String CRLF = "\r\n"; // Line separator required by multipart/form-data.

            String fileName = isConfigFile ? "config.json" : textFile.getName();

            if(!isChunk) {
                logs.append("Uploading ").append(textFile.getAbsolutePath()).append("\n");
                Platform.runLater(() -> logsArea.setText(logs.toString()));
            }


            try {
                URLConnection connection = new URL(url).openConnection();
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                OutputStream output = connection.getOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
                // Send normal param.
//            writer.append("--" + boundary).append(CRLF);
//            writer.append("Content-Disposition: form-data; name=\"param\"").append(CRLF);
//            writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
//            writer.append(CRLF).append(param).append(CRLF).flush();

                // Send text file.
                writer.append("--" + boundary).append(CRLF);
                writer.append("Content-Disposition: form-data; name=\"textFile\"; filename=\"" + fileName + "\"").append(CRLF);
                writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF); // Text file itself must be saved in this charset!
                if (!isConfigFile)
                    writer.append("uploadId: ").append(uploadId).append(CRLF);
                if (path != null)
                    writer.append("path: ").append(path).append(CRLF);
                if(isChunk){
                    writer.append("chunk: true").append(path).append(CRLF);
                }

                writer.append(CRLF).flush();
                Files.copy(textFile.toPath(), output);
                output.flush(); // Important before continuing with writer!
                writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.

                // Send binary file.
//            writer.append("--" + boundary).append(CRLF);
//            writer.append("Content-Disposition: form-data; name=\"binaryFile\"; filename=\"" + binaryFile.getName() + "\"").append(CRLF);
//            writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(binaryFile.getName())).append(CRLF);
//            writer.append("Content-Transfer-Encoding: binary").append(CRLF);
//            writer.append(CRLF).flush();
//            Files.copy(binaryFile.toPath(), output);
//            output.flush(); // Important before continuing with writer!
//            writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.

                // End of multipart/form-data.
                writer.append("--" + boundary + "--").append(CRLF).flush();
                int responseCode = ((HttpURLConnection) connection).getResponseCode();
                if (isConfigFile)
                    uploadId = new BufferedReader(new InputStreamReader((connection.getInputStream()))).readLine();


            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    void readWrite(RandomAccessFile raf, BufferedOutputStream bw, long numBytes) throws IOException {
        byte[] buf = new byte[(int) numBytes];
        int val = raf.read(buf);
        if(val != -1) {
            bw.write(buf);
        }
    }

    public ArrayList<File> splitFiles2(File file) throws IOException {

        ArrayList<File> files = new ArrayList<>();
        RandomAccessFile raf = new RandomAccessFile(file.getAbsolutePath(), "r");
        int maxSize = 2 * 1024 * 1024 * 1000;
        long numSplits = file.length()/maxSize; //from user input, extract it from args
        long sourceSize = raf.length();
        long bytesPerSplit = maxSize/numSplits ;
        long remainingBytes = sourceSize % (numSplits*bytesPerSplit);

        int maxReadBufferSize = 8 * 1024; //8KB
        for(int destIx=1; destIx <= numSplits; destIx++) {
            File f = new File("/tmp/"+file.getName()+"_"+destIx);
            FileOutputStream fo = new FileOutputStream(f);
            BufferedOutputStream bw = new BufferedOutputStream(fo);
            if(bytesPerSplit > maxReadBufferSize) {
                long numReads = bytesPerSplit/maxReadBufferSize;
                long numRemainingRead = bytesPerSplit % maxReadBufferSize;
                for(int i=0; i<numReads; i++) {
                    readWrite(raf, bw, maxReadBufferSize);
                }
                if(numRemainingRead > 0) {
                    readWrite(raf, bw, numRemainingRead);
                }
            }else {
                readWrite(raf, bw, bytesPerSplit);
            }
            bw.flush();
            fo.flush();
            bw.close();
            fo.close();
            files.add(f);
        }
        if(remainingBytes > 0) {
            File f = new File("/tmp/"+file.getName()+"_"+numSplits+1);
            BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(f));
            readWrite(raf, bw, remainingBytes);
            bw.close();
            files.add(f);
        }
        raf.close();
        return files;

    }

    public void closeChunks(){

        try {

            HttpClient httpclient = HttpClients.createDefault();
            HttpPost httppost = new HttpPost("http://localhost:5000/joinChunks");


            List<NameValuePair> params = new ArrayList<NameValuePair>(1);
            params.add(new BasicNameValuePair("uploadId", uploadId));
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));


            HttpResponse response = httpclient.execute(httppost);


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void runPIT(){
        try {

            uploadId="5bd4ed57-e0b7-4d46-9b1e-b2383dda1fc0";
            processingCompleted = false;
            projectsListview.getItems().add(nameField.getText());
            try {
                File file  = new File("pitcloud.txt");
                file.createNewFile();
                Files.write(Paths.get(file.getAbsolutePath()), (nameField.getText()+","+uploadId).getBytes(), StandardOpenOption.APPEND);
            }catch (IOException e) {
                e.printStackTrace();
            }

            projects.put(nameField.getText(), uploadId);

            HttpClient httpclient = HttpClients.createDefault();
            HttpPost httppost = new HttpPost("http://localhost:5000/runPIT");


            List<NameValuePair> params = new ArrayList<NameValuePair>(1);
            params.add(new BasicNameValuePair("uploadId", uploadId));
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));


            httpclient.execute(httppost);

            Runnable runnable = () -> queryUpdate(false);

            if(updateScheduler!=null)
                updateScheduler.cancel(true);

            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            updateScheduler = executor.scheduleAtFixedRate(runnable, 0, 3, TimeUnit.SECONDS);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void queryUpdate(boolean allLogs){
        try {

            HttpClient httpclient = HttpClients.createDefault();
            HttpPost httppost = new HttpPost("http://localhost:5000/queryUpdate");


            List<NameValuePair> params = new ArrayList<NameValuePair>(1);
            params.add(new BasicNameValuePair("uploadId", uploadId));
            params.add(new BasicNameValuePair("allLogs", String.valueOf(allLogs)));
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));


            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, "UTF-8");

            if(responseString.startsWith("completed")){
                logsArea.setText("COMPLETED!");
                processingCompleted = true;
                if(updateScheduler!=null)
                    updateScheduler.cancel(true);
                listDownloadFiles(responseString);
            }else{
                logsArea.setText(logsArea.getText()+responseString);
            }



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadProjects(){

        try {
            BufferedReader br = new BufferedReader(new FileReader("pitcloud.txt"));
            StringBuilder sb = new StringBuilder();
            String[] line = br.readLine().split(",");
            projects.put(line[0], line[1]);
            projectsListview.getItems().add(line[0]);


            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine().split(",");
                projects.put(line[0], line[1]);
                projectsListview.getItems().add(line[0]);
            }
        } catch (Exception ignored){

        }

    }

    private void selectProject(String selectedItem){
        logsArea.setText("");
        processingCompleted = false;
        logs = new StringBuilder();
        uploadId = projects.get(selectedItem);
        if(updateScheduler!=null)
            updateScheduler.cancel(true);

        queryUpdate(true);

        Runnable runnable = () -> queryUpdate(false);

        if(!processingCompleted) {
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            updateScheduler = executor.scheduleAtFixedRate(runnable, 0, 3, TimeUnit.SECONDS);
        }

    }

    private void listDownloadFiles(String response){

        String[] files = response.split(";");
        Pair<String, ProgressBar>[] downloadData = new Pair[files.length-1];
        for (int i = 1; i < files.length; i++) {
            Label fileNameLabel = new Label(files[i]);
            filesDownloadGrid.add(fileNameLabel, 0, i - 1);
            ProgressBar progressBar = new ProgressBar();
            progressBar.setVisible(false);
            filesDownloadGrid.add(progressBar, 1, i - 1);
            GridPane.setHgrow(progressBar, Priority.ALWAYS);
            downloadData[i-1] = new Pair<>(files[i], progressBar);

        }
        new Thread(()-> {
            for (Pair<String, ProgressBar> downloadDatum : downloadData) {
                downloadFile(downloadDatum.getKey(), downloadDatum.getValue());
            }
        }).start();
    }

    private void downloadFile(String file, ProgressBar progressBar){


        try {
            HttpClient httpclient = HttpClients.createDefault();
            HttpPost httppost = new HttpPost("http://localhost:5000/downloadFile");
            System.out.println("a");


            List<NameValuePair> params = new ArrayList<>(1);
            params.add(new BasicNameValuePair("path",uploadId+"/"+file));
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));


            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            long completeFileSize = entity.getContentLength();
            BufferedInputStream in = new BufferedInputStream(entity.getContent());

            String[] pathSplit = file.split("/");
            StringBuilder path = new StringBuilder();
            for (int i = 0; i < pathSplit.length-1; i++) {
                path.append(pathSplit[i]).append("/");
                new File("/media/esteban/b0b05e8c-7bfc-4553-ae68-f70bfe910d3e/PITcloud/test/"+path).mkdirs();
            }
            FileOutputStream fos = new FileOutputStream("/media/esteban/b0b05e8c-7bfc-4553-ae68-f70bfe910d3e/PITcloud/test/"+file);

            BufferedOutputStream bout = new BufferedOutputStream(
                    fos, 1024);
            byte[] data = new byte[1024];
            long downloadedFileSize = 0;
            int x = 0;

            progressBar.setVisible(true);

            while ((x = in.read(data, 0, 1024)) >= 0) {
                downloadedFileSize += x;


                final double currentProgress = ((((double) downloadedFileSize) / ((double) completeFileSize)));
                Platform.runLater(() -> progressBar.setProgress(currentProgress));

                bout.write(data, 0, x);
            }
            bout.close();
            in.close();
            System.out.println("b");
        }catch (Exception e){
            e.printStackTrace();
        }


    }


    @FXML
    public void onSelectConfig() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Configuration file", "*.json"));
        File selectedFile = fileChooser.showOpenDialog(App.getApp().getStage());
        configPathField.setText(selectedFile.getAbsolutePath());
    }
}
