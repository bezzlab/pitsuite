package Controllers;

import Singletons.Config;
import TablesModels.PitdbFile;
import com.mongodb.*;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.MouseEvent;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.controlsfx.control.textfield.TextFields;
import org.dizitart.no2.Cursor;
import org.dizitart.no2.Document;
import org.dizitart.no2.Nitrite;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.*;
import org.apache.commons.fileupload.util.Streams;

public class PITDBUploadController implements Initializable {


    public Tab descriptionTab;
    public Tab filesTab;
    public TreeTableView<PitdbFile> filesTree;
    public TreeTableColumn<PitdbFile, String> filenameColumn;
    public TreeTableColumn<PitdbFile, Double> filesizeColumn;
    public TreeTableColumn<PitdbFile, Boolean> fileuploadedColumn;
    public TextArea projectDescriptionField;
    public TextField projectSpeciesField;
    public TextField projectNameField;
    private String projectName;
    private Nitrite db;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        filenameColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
        filesizeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("size"));
        fileuploadedColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("uploaded"));

        TextFields.bindAutoCompletion(projectSpeciesField, loadSpecies());


    }

    public void loadFiles(){
        TreeItem<PitdbFile> allFiles = new TreeItem<>();

        allFiles.getChildren().add(getGlobalFiles());
        allFiles.getChildren().add(getDgeFiles());
        allFiles.getChildren().add(getSplicingFiles());
        allFiles.getChildren().add(getBamFiles());
        allFiles.getChildren().add(getMzmlFiles());

        allFiles.setExpanded(true);

        filesTree.setRoot(allFiles);

    }


    public TreeItem<PitdbFile> getGlobalFiles(){

        double totalSize = 0;
        File allTranscriptsFile = new File("/media/esteban/data/outputVariationPeptide3/allTranscripts.json");
        TreeItem<PitdbFile> allTranscripts = new TreeItem<>(new PitdbFile("Transcripts",false));



        TreeItem<PitdbFile> allGenes = new TreeItem<>(new PitdbFile("Genes", false));




        TreeItem<PitdbFile> globalFiles = new TreeItem<>(new PitdbFile("Global"));

        globalFiles.getChildren().add(allTranscripts);
        globalFiles.getChildren().add(allGenes);
        globalFiles.setExpanded(true);

        return globalFiles;

    }

    public TreeItem<PitdbFile> getDgeFiles(){

        double totalSize = 0;
        TreeItem<PitdbFile> readCounts = new TreeItem<>(new PitdbFile("Read counts", false));


        ArrayList<String> conditions = new ArrayList<>(Config.getConditions());

        Iterator<int[]> iterator = CombinatoricsUtils.combinationsIterator(conditions.size(), 2);
        ArrayList<TreeItem<PitdbFile>> dgeTreeFiles = new ArrayList<>();
        while (iterator.hasNext()) {
            final int[] combination = iterator.next();

            String condA = conditions.get(combination[0]);
            String condB = conditions.get(combination[1]);
            String comp;
            if(condA.compareTo(condB)>0){
                comp=condB+"vs"+condA;
            }else{
                comp=condA+"vs"+condB;
            }
            comp+=" dge";
            TreeItem<PitdbFile> dge = new TreeItem<>(new PitdbFile(comp, false));
            dgeTreeFiles.add(dge);
        }



        TreeItem<PitdbFile> dgeFiles = new TreeItem<>(new PitdbFile("DGE"));

        dgeFiles.getChildren().add(readCounts);

        for(TreeItem<PitdbFile> file: dgeTreeFiles){
            dgeFiles.getChildren().add(file);
        }
        dgeFiles.setExpanded(true);

        return dgeFiles;

    }

    public TreeItem<PitdbFile> getSplicingFiles(){

        double totalSize = 0;
        TreeItem<PitdbFile> splicingPsi = new TreeItem<>(new PitdbFile("Splicing psi", false));


        ArrayList<String> conditions = new ArrayList<>(Config.getConditions());

        Iterator<int[]> iterator = CombinatoricsUtils.combinationsIterator(conditions.size(), 2);
        ArrayList<TreeItem<PitdbFile>> splicingTreeFiles = new ArrayList<>();
        while (iterator.hasNext()) {
            final int[] combination = iterator.next();

            String condA = conditions.get(combination[0]);
            String condB = conditions.get(combination[1]);

            String comp;
            if(condA.compareTo(condB)>0){
                comp=condB+"vs"+condA;
            }else{
                comp=condA+"vs"+condB;
            }
            TreeItem<PitdbFile> splicing = new TreeItem<>(new PitdbFile(comp+" dpsi", false));
            splicingTreeFiles.add(splicing);

            TreeItem<PitdbFile> splicingEvents = new TreeItem<>(new PitdbFile(comp+" splicing events",  false));
            splicingTreeFiles.add(splicingEvents);

        }



        TreeItem<PitdbFile> splicingFiles = new TreeItem<>(new PitdbFile("Splicing"));

        splicingFiles.getChildren().add(splicingPsi);

        for(TreeItem<PitdbFile> file: splicingTreeFiles){
            splicingFiles.getChildren().add(file);
        }

        splicingFiles.setExpanded(true);
        return splicingFiles;

    }

    public TreeItem<PitdbFile> getBamFiles(){

        double totalSize = 0;

        ArrayList<TreeItem<PitdbFile>> bams = new ArrayList<>();

        for(String condition: Config.getConditions()){
            for(String sample: Config.getSamplesInCondition(condition)){
                File bamFile = new File(Config.getOutputPath()+"/"+condition+"/"+sample+"/2pass/AlignedSorted.bam");
                TreeItem<PitdbFile> bam = new TreeItem<>(new PitdbFile(condition+"/"+sample+"/2pass/AlignedSorted.bam", bamFile.length(),
                        false));
                totalSize+=bamFile.length();
                bams.add(bam);
            }
        }

        TreeItem<PitdbFile> bamsTree = new TreeItem<>(new PitdbFile("Bam", totalSize, false));
        for(TreeItem<PitdbFile> file: bams){
            bamsTree.getChildren().add(file);
        }
        bamsTree.setExpanded(true);
        return bamsTree;

    }

    public TreeItem<PitdbFile> getMzmlFiles(){

        double totalSize = 0;

        ArrayList<TreeItem<PitdbFile>> mzmls = new ArrayList<>();

        for(String run: Config.getRuns()){

            File f = new File(Config.getOutputPath()+"/ms/"+run+"/files");
            File[] matchingFiles = f.listFiles();

            for(File file: matchingFiles){
                if(file.getAbsolutePath().endsWith(".mzML")) {
                    TreeItem<PitdbFile> mzml = new TreeItem<>(new PitdbFile(run + "/" + file.getName(), file.length(),
                            false));
                    totalSize += file.length();
                    mzmls.add(mzml);
                }
            }
        }

        TreeItem<PitdbFile> mzmlTree = new TreeItem<>(new PitdbFile("mzML", totalSize, false));
        for(TreeItem<PitdbFile> file: mzmls){
            mzmlTree.getChildren().add(file);
        }
        mzmlTree.setExpanded(true);
        return mzmlTree;

    }

    public void processFile(PitdbFile file){
        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));

        DB database = mongoClient.getDB("pitdb");

        if(file.getName().equals("Transcripts")){
            uploadAllTranscripts(file, database);
        }else if(file.getName().equals("Genes")){
            //uploadAllGenes(file, database);
        }else if(file.getName().equals("Mutations")){
            uploadMutations(file, database);
        }else if(file.getName().equals("Read counts")){
            uploadReadCount(file, database);
        }else if(file.getName().contains("splicing events")){
            uploadSplicingEvent(file);
        }else if(file.getName().equals("Splicing psi")){
            uploadReadCount(file, database);
        }else if(file.getName().contains("dpsi")){
            uploadSplicingDPSI(file);
        }else if(file.getName().contains("dge")){
            uploadDge(file);
        }else if(file.getName().endsWith(".bam")){
            uploadBam(file);
        }else if(file.getName().endsWith(".mzML")){
            uploadMzml(file);
        }
//        else if(file.getName().equals("Read counts")){
//            uploadSplicingPsi(database);
//        }else if(file.getName().equals("Read counts")){
//            uploadReadCount(database);
//        }else if(file.getName().equals("Read counts")){
//            uploadReadCount(database);
//        }


//        for(String collectionName: db.listCollectionNames()){
//            if(collectionName.equals("allTranscripts")){
//                uploadAllTranscripts(database);
//            }else if (collectionName.equals("allGenes")){
//                uploadAllGenes(database);
//            }else if (collectionName.equals("readCounts")){
//                //uploadReadCount(database);
//            }else if (collectionName.equals("mutations")){
//                uploadMutations(database);
//            }else if (collectionName.contains("transcriptUsageDPSI")){
//                //uploadtranscriptUsageDPSI(database, collectionName);
//            }else if (collectionName.contains("SplicingEvents_")){
//                //uploadSplicingEvent(database, collectionName);
//            }else if (collectionName.equals("SplicingPsi")){
//                //uploadSplicingPsi(database);
//            }else if (collectionName.contains("SplicingDPSI")){
//                //uploadSplicingDPSI(database, collectionName);
//            }else if (collectionName.equals("transcriptUsage")){
//                //uploadTranscriptUsage(database);
//            }else if (collectionName.equals("transcriptCounts")){
//                //uploadTranscriptCounts(database);
//            }else if (collectionName.equals("config")){
//                uploadConfig(database);
//            }
//        }
    }

    private void uploadAllTranscripts(PitdbFile file, DB mongoConnection){
        Cursor transcripts = db.getCollection("allTranscripts").find();


        ArrayList<BasicDBObject> docsToInsert = new ArrayList<>();

        JSONArray docs = new JSONArray();

        for(Document doc: transcripts){

            if (doc.containsKey("CDS")){
                HashMap<String, Object> allCds = (HashMap<String, Object>) doc.get("CDS");
                HashMap<String, Object> newCds = new HashMap<>();

                for(Map.Entry<String, Object> cds: allCds.entrySet()){
                    String newKey = cds.getKey().replaceAll("\\.", "[dot]");
                    newCds.put(newKey, cds.getValue());
                }

                doc.replace("CDS", newCds);
            }

            doc.put("project", projectName);
            doc.remove("_modified");
            doc.remove("_revision");
            doc.remove("_id");

            docs.put(new JSONObject(doc));


            BasicDBObject mongoDoc = new BasicDBObject(doc);
            docsToInsert.add(mongoDoc);
        }
        uploadFile(file, docs.toString(), "allTranscripts");

    }

    private void uploadAllGenes(PitdbFile file, DB mongoConnection){
        Cursor allGenes = db.getCollection("allGenes").find();


        JSONArray docs = new JSONArray();
        for(Document doc: allGenes){
            doc.put("project", projectName);
            JSONObject mongoDoc = new JSONObject(doc);
            mongoDoc.remove("_modified");
            mongoDoc.remove("_revision");
            mongoDoc.remove("_id");
            docs.put(mongoDoc);
        }
        uploadFile(file, docs.toString(), "allGenes");

    }

    private void uploadReadCount(PitdbFile file, DB mongoConnection){
        Cursor readCounts = db.getCollection("readCounts").find();


        ArrayList<BasicDBObject> docsToInsert = new ArrayList<>();
        for(Document doc: readCounts){
            doc.put("project", projectName);
            BasicDBObject mongoDoc = new BasicDBObject(doc);
            mongoDoc.remove("_modified");
            mongoDoc.remove("_revision");
            mongoDoc.remove("_id");
            docsToInsert.add(mongoDoc);
        }

        uploadFile(file, docsToInsert.toString(), "ReadCount");
    }



    private void uploadMutations(PitdbFile file, DB mongoConnection){
        Cursor mutations = db.getCollection("mutations").find();

        JSONArray docs = new JSONArray();

        for(Document doc: mutations){
            doc.put("project", projectName);
            HashMap<String, Object> transcriptsPos = (HashMap<String, Object>) doc.get("transcripts");
            HashMap<String, Object> newTranscriptsPos = new HashMap<>();

            for(Map.Entry<String, Object> entry: transcriptsPos.entrySet()){
                newTranscriptsPos.put(entry.getKey().replaceAll("\\.", "[dot]"), entry.getValue());
            }

            doc.replace("transcripts", newTranscriptsPos);

            doc.remove("_modified");
            doc.remove("_revision");
            doc.remove("_id");
            docs.put(new JSONObject(doc));
        }
        uploadFile(file, docs.toString(), "mutations");
    }

    private void uploadtranscriptUsageDPSI(PitdbFile file, DB mongoConnection, String collectionName){
        String comparison = collectionName.split("_")[1];

        Cursor transcriptsUsageDpsi = db.getCollection(collectionName).find();

        JSONArray docs = new JSONArray();
        for(Document doc: transcriptsUsageDpsi){
            doc.put("project", projectName);
            BasicDBObject mongoDoc = new BasicDBObject(doc);
            mongoDoc.remove("_modified");
            mongoDoc.remove("_revision");
            mongoDoc.remove("_id");
            mongoDoc.put("comparison", comparison);
            docs.put(new JSONObject(mongoDoc));
        }
        uploadFile(file, docs.toString(), "TranscriptUsageDPSI");

    }

    private void uploadSplicingEvent(PitdbFile file){
        String comparison = file.getName().split(" splicing events")[0];

        Cursor events = db.getCollection("SplicingEvents_"+comparison).find();

        JSONArray docs = new JSONArray();
        for(Document doc: events){
            doc.put("project", projectName);
            BasicDBObject mongoDoc = new BasicDBObject(doc);
            mongoDoc.remove("_modified");
            mongoDoc.remove("_revision");
            mongoDoc.remove("_id");
            mongoDoc.put("comparison", comparison);
            docs.put(new JSONObject(mongoDoc));
        }
        uploadFile(file, docs.toString(), "SplicingEvent");
    }

    private void uploadSplicingPsi(PitdbFile file, DB mongoConnection){
        Cursor eventsPsi = db.getCollection("SplicingPsi").find();


        JSONArray docs = new JSONArray();
        for(Document doc: eventsPsi){
            doc.put("project", projectName);
            BasicDBObject mongoDoc = new BasicDBObject(doc);
            mongoDoc.remove("_modified");
            mongoDoc.remove("_revision");
            mongoDoc.remove("_id");
            docs.put(new JSONObject(mongoDoc));
        }
        uploadFile(file, docs.toString(), "SplicingPsi");
    }

    private void uploadSplicingDPSI(PitdbFile file){
        String comparison = file.getName().split(" dpsi")[0];

        Cursor eventsDpsi = db.getCollection("SplicingDPSI_"+comparison).find();

        JSONArray docs = new JSONArray();
        for(Document doc: eventsDpsi){
            doc.put("project", projectName);
            BasicDBObject mongoDoc = new BasicDBObject(doc);
            mongoDoc.remove("_modified");
            mongoDoc.remove("_revision");
            mongoDoc.remove("_id");
            mongoDoc.put("comparison", comparison);
            docs.put(new JSONObject(mongoDoc));
        }
        uploadFile(file, docs.toString(), "SplicingDPSI");
    }

    private void uploadTranscriptUsage(PitdbFile file, DB mongoConnection){
        Cursor transcritpUsages = db.getCollection("transcriptUsage").find();

        DBCollection mongoCollection = mongoConnection.getCollection("transcriptUsage");

        JSONArray docs = new JSONArray();
        for(Document doc: transcritpUsages){
            doc.put("project", projectName);
            BasicDBObject mongoDoc = new BasicDBObject(doc);
            mongoDoc.remove("_modified");
            mongoDoc.remove("_revision");
            mongoDoc.remove("_id");
            docs.put(new JSONObject(mongoDoc));
        }
        uploadFile(file, docs.toString(), "TranscriptUsage");
    }

    private void uploadTranscriptCounts(PitdbFile file, DB mongoConnection){
        Cursor transcriptsCounts = db.getCollection("transcriptCounts").find();

        DBCollection mongoCollection = mongoConnection.getCollection("transcriptCounts");

        JSONArray docs = new JSONArray();
        for(Document doc: transcriptsCounts){
            doc.put("project", projectName);
            BasicDBObject mongoDoc = new BasicDBObject(doc);
            mongoDoc.remove("_modified");
            mongoDoc.remove("_revision");
            mongoDoc.remove("_id");
            docs.put(new JSONObject(mongoDoc));
        }
        uploadFile(file, docs.toString(), "TranscriptCount");
    }

    private void uploadPeptideMap(DB mongoConnection){

    }

    private void uploadGenePeptides(DB mongoConnection){

    }

    private void uploadDge(PitdbFile file){
        String comparison = file.getName().split(" dge")[0];
        Cursor doc = db.getCollection(comparison+"_dge").find();

        JSONObject mongoDoc = new JSONObject(doc);
        mongoDoc.remove("_modified");
        mongoDoc.remove("_revision");
        mongoDoc.remove("_id");
        mongoDoc.put("project", projectName);
        mongoDoc.put("comparison", comparison);

        uploadFile(file, mongoDoc.toString(), "DGE");
    }

    private void uploadConfig(PitdbFile file, DB mongoConnection){
        Document doc = db.getCollection("config").find().firstOrDefault();

        DBCollection mongoCollection = mongoConnection.getCollection("config");

        JSONObject mongoDoc = new JSONObject(doc);
        mongoDoc.remove("_modified");
        mongoDoc.remove("_revision");
        mongoDoc.remove("_id");
        mongoDoc.put("project", projectName);

        uploadFile(file, mongoDoc.toString(), "config");
        //mongoCollection.insert(mongoDoc);

    }

    public void uploadFile(PitdbFile file, String content, String collection) {

        String url = "http://localhost:3000/upload";
        String charset = "UTF-8";

        try {

            BufferedWriter writerTmp = new BufferedWriter(new FileWriter("tmp/json"));
            writerTmp.write(content);

            writerTmp.close();
            File textFile = new File("tmp/json");
            //File binaryFile = new File("tmp/text/json");
            String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
            String CRLF = "\r\n"; // Line separator required by multipart/form-data.




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
            writer.append("Content-Disposition: form-data; name=\"avatar\"; filename=\""+collection+"\"").append(CRLF);
            writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF); // Text file itself must be saved in this charset!
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
//
            // End of multipart/form-data.
            writer.append("--" + boundary + "--").append(CRLF).flush();


            // Request is lazily fired whenever you need to obtain information about response.
            int responseCode = ((HttpURLConnection) connection).getResponseCode();
            System.out.println(responseCode); // Should be 200

            Platform.runLater(() -> {
                file.setUploaded(true);
                filesTree.refresh();
            });



        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void uploadBam(PitdbFile file){


        try{

            System.out.println(file.getName());

            String url = "http://localhost:3000/upload";
            String charset = "UTF-8";
            String param = "value";

            File binaryFile = new File(Config.getOutputPath()+"/"+file.getName());
            String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
            String CRLF = "\r\n"; // Line separator required by multipart/form-data.




            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setChunkedStreamingMode(4096);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            OutputStream output = connection.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);

            writer.append("--" + boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"avatar\"; filename=\""+(file.getName()+"/"+projectName).replace('/', ':')+"\"").append(CRLF);
            writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(binaryFile.getName())).append(CRLF);
            writer.append("Content-Transfer-Encoding: binary").append(CRLF);
            writer.append(CRLF).flush();

            Streams.copy(new FileInputStream(binaryFile), output, false);
            output.flush(); // Important before continuing with writer!
            writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.


            writer.append("--" + boundary + "--").append(CRLF).flush();


            // Request is lazily fired whenever you need to obtain information about response.
            int responseCode = connection.getResponseCode();

            uploadTextFile(Config.getOutputPath()+"/"+file.getName()+".bai", file.getName()+"/"+projectName);

            Platform.runLater(() -> {
                file.setUploaded(true);
                filesTree.refresh();
            });
            System.out.println("uploaded "+responseCode);


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void uploadMzml(PitdbFile file){
        String[] filenameSplit = file.getName().split("/");
        uploadTextFile(Config.getOutputPath()+"/ms/"+filenameSplit[0]+"/files/"+filenameSplit[1], file.getName()+"/"+projectName);
        uploadTextFile(Config.getOutputPath()+"/ms/"+filenameSplit[0]+"/files/"+filenameSplit[1]+".index", file.getName()+".index"+"/"+projectName);
        Platform.runLater(() -> {
            file.setUploaded(true);
            filesTree.refresh();
        });
    }

    public void uploadTextFile(String path, String filename){


        String url = "http://localhost:3000/upload";
        String charset = "UTF-8";

        try {
            File textFile = new File(path);
            String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
            String CRLF = "\r\n"; // Line separator required by multipart/form-data.


            URLConnection connection = new URL(url).openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            OutputStream output = connection.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);

            // Send text file.
            writer.append("--" + boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"avatar\"; filename=\""+filename.replace('/', ':')+"\"").append(CRLF);
            writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF); // Text file itself must be saved in this charset!
            writer.append(CRLF).flush();
            Files.copy(textFile.toPath(), output);
            output.flush(); // Important before continuing with writer!
            writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.


            // End of multipart/form-data.
            writer.append("--" + boundary + "--").append(CRLF).flush();


            // Request is lazily fired whenever you need to obtain information about response.
            int responseCode = ((HttpURLConnection) connection).getResponseCode();
            System.out.println(responseCode); // Should be 200



        }catch (Exception e){
            e.printStackTrace();
        }


    }

    public void setProjectName(String projectName) {
        this.projectName=projectName;
    }

    public void setDb(Nitrite db) {
        this.db=db;
    }

    public void onUploadClick() {
        new Thread(() -> {
            for(TreeItem<PitdbFile> category: filesTree.getRoot().getChildren()){
                for(TreeItem<PitdbFile> file: category.getChildren()){
                    processFile(file.getValue());
                }
            }
        }).start();

    }

    public ArrayList<String> loadSpecies(){

        ArrayList<String> species = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("species.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                species.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return species;
    }
}
