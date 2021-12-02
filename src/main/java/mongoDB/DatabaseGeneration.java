package mongoDB;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.dizitart.no2.*;
import org.dizitart.no2.filters.Filters;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class DatabaseGeneration {

    private String databasePathAndName;

    private final HashSet<String> transcIdsInDb = new HashSet<>();

    /**
     * this is the main function to generate the database
     * @param path path selected by the user
     * @param databasePath path to the database where the info is going to be saved
     */

    public boolean genDatabase(String path, String databasePath, ProgressBar loadingBar,  Label progressDialog)
            throws IOException, ParseException {


        // define the path to the database and the name of the database. This variable is used throughout the rest of the code...
        databasePathAndName = databasePath;

        loadingBar.setVisible(true);
        progressDialog.setVisible(true);
        Platform.runLater(() -> progressDialog.setText("Beginning import..."));

//        loadingBar.setProgress(progress);
        // get the location of the required files

        // get the required files
        HashMap<String, ArrayList<Path>> filesMap = fileWalkwer(path);


        for (Map.Entry<String, ArrayList<Path>> fileMapEntry: filesMap.entrySet()) {
            String fileType = fileMapEntry.getKey();
            ArrayList<Path> filePaths = fileMapEntry.getValue();

            for (Path filePath : filePaths) {
                String filePathString = filePath.toString();

                switch (fileType) {
                    case "allGenes":

                        // This the only way to update the UI while the thread is running
                        Platform.runLater(() -> progressDialog.setText("Importing all genes..."));

                        if (filePaths.size() > 1) {
                            System.out.println("WARNING: more than 1 allGenes.json File... please check that..");
                        }


                        allGenesParser(filePathString);


                        Platform.runLater(() -> progressDialog.setText("Importing ChrMap"));



                        break;
                    case "allTranscripts":
                        allTranscriptsParser(filePathString);
                        break;
                    case "splicingEvents":
                        if (filePath.getFileName().toString().contains("_events")) {
                            Platform.runLater(() -> progressDialog.setText("Importing splicing events..."));
                            spliceEventsParser(filePath);
                        } else if (filePath.getFileName().toString().contains("splicingAllEvents_psi.tsv")) {
                            Platform.runLater(() -> progressDialog.setText("Importing splicing Psi / TPM ..."));
                            splicePsiParser(filePath);
                        } else if (filePath.getFileName().toString().contains("splicingAllEvents_dpsi.json")){
                            Platform.runLater(() -> progressDialog.setText("Importing splicing dPsi: "));
                            spliceDPSIParser(filePath);
                        }

                        break;
                    case "transcriptUsage":
                        if (filePath.getFileName().toString().contains("transcriptUsage.dpsi")) {
                            Platform.runLater(() -> progressDialog.setText("Importing splicing events..."));
                            transcriptUsageDPSIParser(filePath);
                        } else if (filePath.getFileName().toString().contains("combinedTranscriptUsage.csv")) {
                            Platform.runLater(() -> progressDialog.setText("Importing splicing Psi / TPM ..."));
                            transcriptUsageParser(filePath);
                        }

                        break;
                    case "petidesMaps": {
                        System.out.println("Starting PSM map");
                        String[] pathSplit = filePath.toString().split(Pattern.quote(File.separator));
                        String runName = pathSplit[pathSplit.length - 2];
                        peptideMapsParser(filePathString, runName);
                        System.out.println("peptides Map: " + runName + "done");

                        break;
                    }
                    case "genePeptides": {
                        System.out.println("Starting PSM map");
                        String[] pathSplit = filePath.toString().split(Pattern.quote(File.separator));
                        String runName = pathSplit[pathSplit.length - 2];
                        genePeptidesParser(filePathString, runName);
                        System.out.println("gene peptides: " + runName + "done");

                        break;
                    }
                    case "spectrumPeaks": {
                        String[] pathSplit = filePath.toString().split(Pattern.quote(File.separator));
                        String runName = pathSplit[pathSplit.length - 3];
                        peaksParser(filePathString, runName);

                        break;
                    }
                    case "dge":
                        dgeParser(filePath);
                        System.out.println("dge: " + filePathString);
                        break;
                    case "bamFiles": {

                        String[] pathSplit = filePathString.split(Pattern.quote(File.separator));
                        String condition = pathSplit[pathSplit.length - 4];
                        String sample = pathSplit[pathSplit.length - 3];
                        bamFilesPathToDatabase(condition, sample, filePathString);
                        break;
                    }
                    case "eventPeptides": {

                        parseEventPeptides(filePathString);
                        break;
                    }
                    case "readCounts":
                        readCountsParser(filePathString);
                        break;
                    case "proteinQuant":
                        proteinQuantParser(filePathString);
                        break;
                    case "peptideQuant":
                        peptideQuantParser(filePathString);
                        break;
                    case "mutations":
                        mutationParser(filePathString);
                        break;
                    case "config":
                        configParser(filePathString);
                        break;
                    case "ptm":
                        ptmParser(filePath);
                        break;
                    case "transcriptCount":
                        transcriptCountsParser(filePath);
                        break;
                    case "blast":
                        blastParser(filePathString);
                        break;
                    case "kinaseActivity":
                        kinaseActivityParser(filePath);
                        break;
                }


            }
        }
        Platform.runLater(() -> progressDialog.setText("Creating indexes..."));
        createIndexes();
        Platform.runLater(() -> progressDialog.setText("Import complete. Project can be opened"));

        return true;
    }

    private  HashMap<String, ArrayList<Path>> fileWalkwer (String path) {

        HashMap<String, ArrayList<Path>> pathsMap = new HashMap<>();
        pathsMap.put("allGenes", new ArrayList<>());
        pathsMap.put("sequencesCDSPfamPeptides", new ArrayList<>());
        pathsMap.put("petidesMaps", new ArrayList<>());
        pathsMap.put("dge", new ArrayList<>());
        pathsMap.put("splicingEvents", new ArrayList<>());
        pathsMap.put("bamFiles", new ArrayList<>());
        pathsMap.put("heatMapsPngs", new ArrayList<>());
        pathsMap.put("readCounts", new ArrayList<>());
        pathsMap.put("proteinQuant", new ArrayList<>());
        pathsMap.put("peptideQuant", new ArrayList<>());
        pathsMap.put("mutations", new ArrayList<>());
        pathsMap.put("config", new ArrayList<>());
        pathsMap.put("allTranscripts", new ArrayList<>());
        pathsMap.put("genePeptides", new ArrayList<>());
        pathsMap.put("spectrumPeaks", new ArrayList<>());
        pathsMap.put("eventPeptides", new ArrayList<>());
        pathsMap.put("transcriptUsage", new ArrayList<>());
        pathsMap.put("transcriptCount", new ArrayList<>());
        pathsMap.put("ptm", new ArrayList<>());
        pathsMap.put("kinaseActivity", new ArrayList<>());
        pathsMap.put("blast", new ArrayList<>());


        Stream<Path> filePathStream = null;
        try {

            filePathStream = Files.walk(Paths.get(String.valueOf(path)));

        } catch (IOException e) {
            e.printStackTrace();
        }

        filePathStream.forEach(filePath -> {

            if (Files.isRegularFile(filePath)) {
                if ( filePath.endsWith("allGenes.json")) {
                    pathsMap.get("allGenes").add(filePath);
                } else if ( filePath.endsWith("sequencesAnnotated.json")) {
                    pathsMap.get("sequencesCDSPfamPeptides").add(filePath);
                } else if (filePath.getFileName().toString().contains("peptideMap.json")) {
                    pathsMap.get("petidesMaps").add(filePath);
                } else if (filePath.getFileName().toString().equals("dge.json")){
                    pathsMap.get("dge").add(filePath);
                } else  if (filePath.getFileName().toString().contains("splicing")) {
                    pathsMap.get("splicingEvents").add(filePath);
                } else if (filePath.toString().endsWith("AlignedSorted.bam")){
                    pathsMap.get("bamFiles").add(filePath);
                } else if (filePath.getFileName().toString().contains("_heatmap.png")){
                    pathsMap.get("heatMapsPngs").add(filePath);
                } else if (filePath.getFileName().toString().contains("allReadCountsNormalised.json")){
                    pathsMap.get("readCounts").add(filePath);
                } else if (filePath.getFileName().toString().contains("proteinQuant.json")){
                    pathsMap.get("proteinQuant").add(filePath);
                } else if (filePath.getFileName().toString().contains("peptideQuant.json")){
                    pathsMap.get("peptideQuant").add(filePath);
                } else if (filePath.getFileName().toString().contains("allMutations.json")){
                    pathsMap.get("mutations").add(filePath);
                } else if (filePath.getFileName().toString().contains("config.json")){
                    pathsMap.get("config").add(filePath);
                } else if (filePath.getFileName().toString().contains("allTranscripts.json")){
                    pathsMap.get("allTranscripts").add(filePath);
                } else if (filePath.getFileName().toString().contains("genesPeptides.json")){
                    pathsMap.get("genePeptides").add(filePath);
                }else if (filePath.getFileName().toString().contains("peaks.json")){
                    pathsMap.get("spectrumPeaks").add(filePath);
                }else if (filePath.getFileName().toString().contains("eventPeptides.json")){
                    pathsMap.get("eventPeptides").add(filePath);
                }else if (filePath.getFileName().toString().toUpperCase().contains("TRANSCRIPTUSAGE")){
                    pathsMap.get("transcriptUsage").add(filePath);
                }else if (filePath.getFileName().toString().contains("transcriptReadCount_normalised")){
                    pathsMap.get("transcriptCount").add(filePath);
                }else if (filePath.getFileName().toString().equals("ptm.json")){
                    pathsMap.get("ptm").add(filePath);
                }else if (filePath.getFileName().toString().equals("hits.json")){
                    pathsMap.get("blast").add(filePath);
                }else if (filePath.getFileName().toString().equals("kinaseActivity.json")){
                    pathsMap.get("kinaseActivity").add(filePath);
                }

            }

        });
        return pathsMap;
    }

    private  void allGenesParser(String filePath) throws IOException {

        JsonReader jsonReader = new JsonReader(new InputStreamReader( new FileInputStream(filePath)));

        Gson gson = new GsonBuilder().create();


        Nitrite db = Nitrite.builder().filePath(databasePathAndName).openOrCreate();
        NitriteCollection genesCollection = db.getCollection("allGenes");

        jsonReader.beginObject();

        ArrayList<String> transcriptsIdsList;

        ArrayList<org.dizitart.no2.Document> genesDocsToDBList = new ArrayList<>();

        while(jsonReader.hasNext()) {
            if (genesDocsToDBList.size() >= 10000) {
                Document[] geneDocsSimple = new Document[genesDocsToDBList.size()];
                geneDocsSimple = genesDocsToDBList.toArray(geneDocsSimple);
                genesCollection.insert(geneDocsSimple);
                genesDocsToDBList = new ArrayList<>();
            }
            String geneID = jsonReader.nextName();
            GeneJsonReader geneJsonReaderItemJson = gson.fromJson(jsonReader, GeneJsonReader.class);

            transcriptsIdsList = new ArrayList<>(geneJsonReaderItemJson.getTranscripts().keySet());

            Document currDocument = new Document();

            currDocument.put("chr", geneJsonReaderItemJson.getChr());
            currDocument.put("start", geneJsonReaderItemJson.getStart());
            currDocument.put("end", geneJsonReaderItemJson.getEnd());
            if(geneJsonReaderItemJson.getSymbol()!=null){
                currDocument.put("symbol", geneJsonReaderItemJson.getSymbol());
            }else{
                currDocument.put("symbol", geneID);
            }

            currDocument.put("transcripts", transcriptsIdsList);
            currDocument.put("kegg", geneJsonReaderItemJson.getKegg());
            currDocument.put("goTerms", geneJsonReaderItemJson.getGO());


            genesDocsToDBList.add(currDocument);
        }

        if(genesDocsToDBList.size() > 0){
            Document[] geneDocsSimple = new Document[genesDocsToDBList.size()];
            geneDocsSimple = genesDocsToDBList.toArray(geneDocsSimple);
            genesCollection.insert(geneDocsSimple);
        }

        db.close();

    }

    private void readCountsParser(String filePath) throws IOException, ParseException {

        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(filePath));
        Nitrite db = Nitrite.builder().filePath(databasePathAndName).openOrCreate();
        NitriteCollection readCountCollection = db.getCollection("readCounts");

        JSONObject jsonObject = (JSONObject) obj;

        ArrayList<Document> genesDocsToDBList = new ArrayList<>();

        for (Object o : jsonObject.keySet()) {
            String key = (String) o;
            if (genesDocsToDBList.size() >= 10000) {
                Document[] geneDocsSimple = new Document[genesDocsToDBList.size()];
                geneDocsSimple = genesDocsToDBList.toArray(geneDocsSimple);
                readCountCollection.insert(geneDocsSimple);
                genesDocsToDBList = new ArrayList<>();
            }
            if (jsonObject.get(key) instanceof JSONObject) {
                Document currDocument = new Document();
                currDocument.put("gene", key);
                currDocument.put("counts", jsonObject.get(key));
                genesDocsToDBList.add(currDocument);
            }
        }

        if(genesDocsToDBList.size() > 0){
            Document[] geneDocsSimple = new Document[genesDocsToDBList.size()];
            geneDocsSimple = genesDocsToDBList.toArray(geneDocsSimple);
            readCountCollection.insert(geneDocsSimple);
        }

        db.close();
    }

    private void mutationParser(String filePath) throws IOException, ParseException {

        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(filePath));
        Nitrite db = Nitrite.builder().filePath(databasePathAndName).openOrCreate();
        NitriteCollection mutationsCollection = db.getCollection("mutations");

        JSONArray jsonArray = (JSONArray) obj;

        ArrayList<Document> genesDocsToDBList = new ArrayList<>();

        for (Object o : jsonArray) {

            // 250 to reduce memory usage, since the whole json is already in memory
            if (genesDocsToDBList.size() >= 250) {
                Document[] geneDocsSimple = new Document[genesDocsToDBList.size()];
                geneDocsSimple = genesDocsToDBList.toArray(geneDocsSimple);
                mutationsCollection.insert(geneDocsSimple);
                genesDocsToDBList = new ArrayList<>();
            }

            JSONObject jsonObject = (JSONObject) o;
            Document currDocument = new Document(jsonObject);
            currDocument.put("gene", jsonObject.get("gene"));
            currDocument.put("chr", jsonObject.get("chr"));
            currDocument.put("refPos", jsonObject.get("refPos"));
            currDocument.put("ref", jsonObject.get("ref"));
            currDocument.put("alt", jsonObject.get("alt"));
            currDocument.put("hasPeptideEvidence", jsonObject.get("hasPeptideEvidence"));
            currDocument.put("inCDS", jsonObject.get("inCDS"));
            currDocument.put("silent", jsonObject.get("silent"));
            currDocument.put("condition", jsonObject.get("condition"));
            currDocument.put("transcripts", jsonObject.get("transcripts"));
            if(jsonObject.containsKey("pfam"))
                currDocument.put("pfam", jsonObject.get("pfam"));
            genesDocsToDBList.add(currDocument);
        }


        if(genesDocsToDBList.size() > 0){
            Document[] geneDocsSimple = new Document[genesDocsToDBList.size()];
            geneDocsSimple = genesDocsToDBList.toArray(geneDocsSimple);
            mutationsCollection.insert(geneDocsSimple);
        }

        mutationsCollection.createIndex("gene", IndexOptions.indexOptions(IndexType.NonUnique, false));
        mutationsCollection.createIndex("hasPeptideEvidence", IndexOptions.indexOptions(IndexType.NonUnique, false));
        mutationsCollection.createIndex("inCDS", IndexOptions.indexOptions(IndexType.NonUnique, false));
        mutationsCollection.createIndex("silent", IndexOptions.indexOptions(IndexType.NonUnique, false));
        db.close();
    }

    private void configParser(String filePath) throws IOException, ParseException {

        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(filePath));
        Nitrite db = Nitrite.builder().filePath(databasePathAndName).openOrCreate();
        NitriteCollection genesCollection = db.getCollection("config");

        JSONObject jsonObject = (JSONObject) obj;
        Document currDocument = new Document(jsonObject);

        genesCollection.insert(currDocument);
        db.close();

    }

    private void proteinQuantParser(String filePath) throws IOException, ParseException {

        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(filePath));
        Nitrite db = Nitrite.builder().filePath(databasePathAndName).openOrCreate();
        NitriteCollection protQuantCollection = db.getCollection("proteinQuant");

        JSONObject jsonObject = (JSONObject) obj;

        ArrayList<Document> genesDocsToDBList = new ArrayList<>();

        for (Object o : jsonObject.keySet()) {
            String key = (String) o;
            if (genesDocsToDBList.size() >= 10000) {
                Document[] geneDocsSimple = new Document[genesDocsToDBList.size()];
                geneDocsSimple = genesDocsToDBList.toArray(geneDocsSimple);
                protQuantCollection.insert(geneDocsSimple);
                genesDocsToDBList = new ArrayList<>();
            }
            if (jsonObject.get(key) instanceof JSONObject) {
                Document currDocument = new Document();
                currDocument.put("gene", key);
                JSONObject geneObj = (JSONObject) jsonObject.get(key);

                if(geneObj.containsKey("abundance")){
                    currDocument.put("abundance", geneObj.get("abundance"));
                }else{
                    currDocument.put("ratio", geneObj.get("ratio"));
                }

                currDocument.put("peptides", geneObj.get("peptides"));
                genesDocsToDBList.add(currDocument);
            }
        }

        if(genesDocsToDBList.size() > 0){
            Document[] geneDocsSimple = new Document[genesDocsToDBList.size()];
            geneDocsSimple = genesDocsToDBList.toArray(geneDocsSimple);
            protQuantCollection.insert(geneDocsSimple);
        }
        db.close();

    }

    private void peptideQuantParser(String filePath) throws IOException, ParseException {

        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(filePath));
        Nitrite db = Nitrite.builder().filePath(databasePathAndName).openOrCreate();
        NitriteCollection protQuantCollection = db.getCollection("peptideQuant");

        JSONObject jsonObject = (JSONObject) obj;

        ArrayList<Document> genesDocsToDBList = new ArrayList<>();

        for (Object o : jsonObject.keySet()) {
            String key = (String) o;
            if (genesDocsToDBList.size() >= 10000) {
                Document[] geneDocsSimple = new Document[genesDocsToDBList.size()];
                geneDocsSimple = genesDocsToDBList.toArray(geneDocsSimple);
                protQuantCollection.insert(geneDocsSimple);
                genesDocsToDBList = new ArrayList<>();
            }
            if (jsonObject.get(key) instanceof JSONObject) {
                Document currDocument = new Document();
                currDocument.put("peptide", key);
                currDocument.put("abundance", jsonObject.get(key));
                genesDocsToDBList.add(currDocument);
            }
        }

        if(genesDocsToDBList.size() > 0){
            Document[] geneDocsSimple = new Document[genesDocsToDBList.size()];
            geneDocsSimple = genesDocsToDBList.toArray(geneDocsSimple);
            protQuantCollection.insert(geneDocsSimple);
        }
        db.close();

    }

    private void allTranscriptsParser(String path){

        Nitrite db = Nitrite.builder().filePath(databasePathAndName).openOrCreate();
        NitriteCollection collection = db.getCollection("allTranscripts");

        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(path));
            JSONObject jsonObject = (JSONObject) obj;
            Iterator<String> keys = jsonObject.keySet().iterator();

            ArrayList<Document> genesDocsToDBList = new ArrayList<>();

            while(keys.hasNext()) {
                String key = keys.next();

                if (genesDocsToDBList.size() >= 5000) {
                    Document[] geneDocsSimple = new Document[genesDocsToDBList.size()];
                    geneDocsSimple = genesDocsToDBList.toArray(geneDocsSimple);
                    collection.insert(geneDocsSimple);
                    genesDocsToDBList = new ArrayList<>();
                }

                if (jsonObject.get(key) instanceof JSONObject) {
                    JSONObject o = (JSONObject) jsonObject.get(key);
                    Document doc = new Document( (JSONObject) jsonObject.get(key));
                    doc.put("transcriptID", key);
                    genesDocsToDBList.add(doc);
                }
            }

            if(genesDocsToDBList.size() > 0){
                Document[] geneDocsSimple = new Document[genesDocsToDBList.size()];
                geneDocsSimple = genesDocsToDBList.toArray(geneDocsSimple);
                collection.insert(geneDocsSimple);
            }

            db.close();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }





    private void peptideMapsParser(String filePath, String runName) {



        Nitrite db = Nitrite.builder().filePath(databasePathAndName).openOrCreate();
        NitriteCollection peptidesMapsCollection = db.getCollection("peptideMap");
        System.out.println(filePath);

        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(filePath));
            JSONObject jsonObject = (JSONObject) obj;
            Iterator<String> keys = jsonObject.keySet().iterator();

            ArrayList<Document> peptidesDocsToDbList = new ArrayList<>();
            String key;

            while (keys.hasNext()) {
                if (peptidesDocsToDbList.size() >= 10000) {
                    // Nitrite doesn't accept an array list of documents when inserting many documents
                    // So they need to be converted into arrays before being inserted
                    Document[] peptideSimple = new Document[peptidesDocsToDbList.size()];
                    peptideSimple = peptidesDocsToDbList.toArray(peptideSimple);
                    peptidesMapsCollection.insert(peptideSimple);
                    peptidesDocsToDbList = new ArrayList<>();
                }
                key = keys.next();

                if(jsonObject.get(key) instanceof JSONObject){
                    Document peptideMapDoc = new Document();
                    peptideMapDoc.put("peptide", key);
                    peptideMapDoc.put("run", runName);
                    peptideMapDoc.put("transcripts", ((JSONObject) jsonObject.get(key)).get("transcripts"));
                    peptideMapDoc.put("psms", ((JSONObject) jsonObject.get(key)).get("psms"));
                    peptideMapDoc.put("probability", ((JSONObject) jsonObject.get(key)).get("probability"));
                    if(((JSONObject) jsonObject.get(key)).containsKey("intensity")){
                        peptideMapDoc.put("intensity", ((JSONObject) jsonObject.get(key)).get("intensity"));
                    }

                    peptidesDocsToDbList.add(peptideMapDoc);
                }


            }

            if (peptidesDocsToDbList.size() > 0) {
                Document[] peptideSimple = new Document[peptidesDocsToDbList.size()];
                peptideSimple = peptidesDocsToDbList.toArray(peptideSimple);
                peptidesMapsCollection.insert(peptideSimple);
            }

            db.close();
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private void genePeptidesParser(String filePath, String runName) {



        Nitrite db = Nitrite.builder().filePath(databasePathAndName).openOrCreate();
        NitriteCollection peptidesMapsCollection = db.getCollection("genePeptides");

        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(filePath));
            JSONObject jsonObject = (JSONObject) obj;
            Iterator<String> keys = jsonObject.keySet().iterator();

            ArrayList<Document> peptidesDocsToDbList = new ArrayList<>();
            String key;

            while (keys.hasNext()) {
                if (peptidesDocsToDbList.size() >= 10000) {
                    // Nitrite doesn't accept an array list of documents when inserting many documents
                    // So they need to be converted into arrays before being inserted
                    Document[] peptideSimple = new Document[peptidesDocsToDbList.size()];
                    peptideSimple = peptidesDocsToDbList.toArray(peptideSimple);
                    peptidesMapsCollection.insert(peptideSimple);
                    peptidesDocsToDbList = new ArrayList<>();
                }
                key = keys.next();

                if(jsonObject.get(key) instanceof JSONObject){
                    Document peptideMapDoc = new Document();
                    peptideMapDoc.put("gene", key);
                    peptideMapDoc.put("run", runName);
                    peptideMapDoc.put("peptides", jsonObject.get(key));
                    peptidesDocsToDbList.add(peptideMapDoc);
                }



            }

            if (peptidesDocsToDbList.size() > 0) {
                Document[] peptideSimple = new Document[peptidesDocsToDbList.size()];
                peptideSimple = peptidesDocsToDbList.toArray(peptideSimple);
                peptidesMapsCollection.insert(peptideSimple);
            }

            db.close();
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private void peaksParser(String filePath, String runName) {



        Nitrite db = Nitrite.builder().filePath(databasePathAndName).openOrCreate();
        NitriteCollection peptidesMapsCollection = db.getCollection("spectra");

        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(filePath));
            JSONArray spectra = (JSONArray) obj;

            ArrayList<Document> peptidesDocsToDbList = new ArrayList<>();

            for( Object o: spectra) {
                JSONObject spectrum = (JSONObject) o;
                if (peptidesDocsToDbList.size() >= 10000) {
                    // Nitrite doesn't accept an array list of documents when inserting many documents
                    // So they need to be converted into arrays before being inserted
                    Document[] peptideSimple = new Document[peptidesDocsToDbList.size()];
                    peptideSimple = peptidesDocsToDbList.toArray(peptideSimple);
                    peptidesMapsCollection.insert(peptideSimple);
                    peptidesDocsToDbList = new ArrayList<>();
                }

                Document peptideMapDoc = new Document(spectrum);
                peptidesDocsToDbList.add(peptideMapDoc);

            }

            if (peptidesDocsToDbList.size() > 0) {
                Document[] peptideSimple = new Document[peptidesDocsToDbList.size()];
                peptideSimple = peptidesDocsToDbList.toArray(peptideSimple);
                peptidesMapsCollection.insert(peptideSimple);
            }

            db.close();
        } catch (Exception e){
            e.printStackTrace();
        }

    }


    private void dgeParser(Path filePath) throws IOException {

        Nitrite db = Nitrite.builder().filePath(databasePathAndName).openOrCreate();
        NitriteCollection dgeCollection = db.getCollection(filePath.getParent().getFileName().toString()+"_dge");


        ArrayList<Document> dgeDocsToDBList = new ArrayList<>();

        JSONParser parser = new JSONParser();

        boolean hasBlastName = false;

        try {
            Object obj = parser.parse(new FileReader(filePath.toString()));

            JSONArray genes = (JSONArray) obj;
            for (Object o: genes) {
                JSONObject gene = (JSONObject) o;
                if (dgeDocsToDBList.size() >= 10000) {
                    Document[] splicDocsArray = new Document[dgeDocsToDBList.size()];
                    splicDocsArray = dgeDocsToDBList.toArray(splicDocsArray);
                    try{
                        dgeCollection.insert(splicDocsArray);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    dgeDocsToDBList = new ArrayList<>();
                }



                Document dgeDocument = new Document(gene);
                if(dgeDocument.get("log2fc").getClass().equals(String.class)){
                    dgeDocument.replace("log2fc", Double.parseDouble((String) dgeDocument.get("log2fc")));
                }

                if(dgeDocument.containsKey("names")){
                    hasBlastName = true;
                }
                dgeDocsToDBList.add(dgeDocument);


            }

            if(dgeDocsToDBList.size() > 0){
                Document[] splicDocsArray = new Document[dgeDocsToDBList.size()];
                splicDocsArray = dgeDocsToDBList.toArray(splicDocsArray);
                dgeCollection.insert(splicDocsArray);
            }

            dgeCollection.createIndex("symbol", IndexOptions.indexOptions(IndexType.Unique, false));
            if(hasBlastName){
                //dgeCollection.createIndex("names", IndexOptions.indexOptions(IndexType.Fulltext, false));
            }


            db.close();



        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private HashMap<String, ArrayList<Path>> findSamples(String path) {

        HashMap<String, ArrayList<Path>> pathsMap = new HashMap<>();
        pathsMap.put("peptides", new ArrayList<>());



        Stream<Path> filePathStream = null;
        try {

            filePathStream = Files.walk(Paths.get(String.valueOf(path)));

        } catch (IOException e) {
            e.printStackTrace();
        }

        filePathStream.forEach(filePath -> {
            if (Files.isRegularFile(filePath)) {
                if (filePath.endsWith("sequencesCDSPfamPeptides.json")) {
                    pathsMap.get("peptides").add(filePath);
                }
            }
        });
        return pathsMap;
    }

    public void transcriptCountsParser(Path filePath) {

        Nitrite db = Nitrite.builder().filePath(databasePathAndName ).openOrCreate();
        NitriteCollection currCollection = db.getCollection("transcriptCounts");

        ArrayList<Document> splicDocsToDBList = new ArrayList<>();


        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toString()))) {
            String line;

            // get / skip header
            String[] headers = br.readLine().split(",");


            while ((line = br.readLine()) != null) {

                if (splicDocsToDBList.size() >= 10000) {
                    Document[] splicDocsArray = new Document[splicDocsToDBList.size()];
                    splicDocsArray = splicDocsToDBList.toArray(splicDocsArray);
                    currCollection.insert(splicDocsArray);
                    splicDocsToDBList = new ArrayList<>();
                }

                Document currDocument = new Document();
                String[] values = line.split(",");

                currDocument.put("transcript", values[0]);
                HashMap<String, Double> readCounts = new HashMap<>();
                for(int i= 1; (i < headers.length  && i < values.length) ; i++){
                    if (values[i].length() > 0 ){
                        if(values[i].equals("")){
                            readCounts.put(headers[i], 0.);
                        }else{
                            readCounts.put(headers[i], Double.valueOf(values[i]));
                        }

                    }
                }
                currDocument.put("readCounts", readCounts);
                splicDocsToDBList.add(currDocument);

            }

            if(splicDocsToDBList.size() > 0){
                Document[] splicDocsArray = new Document[splicDocsToDBList.size()];
                splicDocsArray = splicDocsToDBList.toArray(splicDocsArray);
                currCollection.insert(splicDocsArray);
            }

            currCollection.createIndex("transcript", IndexOptions.indexOptions(IndexType.Unique, false));


        } catch (IOException e) {
            e.printStackTrace();
        }


        db.close();
    }






    private void bamFilesPathToDatabase(String condition, String sample, String bamPath){

        Nitrite db = Nitrite.builder().filePath(databasePathAndName).openOrCreate();
        NitriteCollection bamPathsCollection = db.getCollection("bamPaths");


        Document bamPathDoc = new Document();
        bamPathDoc.put("condition", condition);
        bamPathDoc.put("sample", sample);
        bamPathDoc.put("bamPath", bamPath);

        bamPathsCollection.insert(bamPathDoc);
        System.out.println("bamPaths inserted");
        db.close();


    }


    /**
     * reads the splicing_events.csv file
     * @param filePath
     * order of the file columns (tab separated):
     * geneName	event	alternative_transcripts	total_transcripts	domains_in	domains_out	Gene_id	Event_type	chr	peptideEvidence
     *  0        1       2                          3                    4         5          6        7          8   9
     */
    public void spliceEventsParser(Path filePath) {

        Nitrite db = Nitrite.builder().filePath(databasePathAndName).openOrCreate();

        NitriteCollection currCollection = db.getCollection("SplicingEvents_"+ filePath.getParent().getFileName().toString());

        ArrayList<org.dizitart.no2.Document> splicEventsDocsToDBList = new ArrayList<>();


        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toString()))) {
            String line;

            // skip header
            br.readLine();

            while ((line = br.readLine()) != null) {

                if (splicEventsDocsToDBList.size() >= 10000) {
                    Document[] splicDocsArray = new Document[splicEventsDocsToDBList.size()];
                    splicDocsArray = splicEventsDocsToDBList.toArray(splicDocsArray);
                    currCollection.insert(splicDocsArray);
                    splicEventsDocsToDBList = new ArrayList<>();
                }

                Document currDocument = new Document();
                String[] values = line.split("\\t");


                String[] total_transcripts = values[3].split(",");
                String[] domains_in = values[4].split(",");
                String[] domains_out = values[5].split(",");

                currDocument.put("geneName", values[0]);
                currDocument.put("event", values[1]);
                currDocument.put("alternative_transcripts", values[2]);
                currDocument.put("total_transcripts", total_transcripts);
                currDocument.put("event_type", values[6]);
                currDocument.put("pep_evidence", Boolean.valueOf(values[8]));
                currDocument.put("domains_in", domains_in);
                currDocument.put("domains_out", domains_out);

                splicEventsDocsToDBList.add(currDocument);
            }

            if(splicEventsDocsToDBList.size() > 0){
                Document[] splicDocsArray = new Document[splicEventsDocsToDBList.size()];
                splicDocsArray = splicEventsDocsToDBList.toArray(splicDocsArray);
                currCollection.insert(splicDocsArray);
            }

            currCollection.
                    createIndex("event", IndexOptions.indexOptions(IndexType.NonUnique, false));

        } catch (IOException e) {
            e.printStackTrace();
        }
        db.close();
    }



    /**
     * parses the splicingAllEvents_psiPerSample.csv file
     * @param filePath

     * order of the file columns (tab separated):
     * first column is event_id, the rest are the TPM_WITH and PSI per cond/sample : COND/Sample_TPMwith  or COND/Sample_psi
     * event	Nsi/1_TPMwith	Nsi/2_TPMwith	Nsi/3_TPMwith	si/1_TPMwith	si/2_TPMwith	si/3_TPMwith	Nsi/1_psi	Nsi/2_psi	Nsi/3_psi	si/1_psi	si/2_psi	si/3_psi
     *  0
     */
    public void splicePsiParser(Path filePath) {

        Nitrite db = Nitrite.builder().filePath(databasePathAndName ).openOrCreate();
        NitriteCollection currCollection = db.getCollection("SplicingPsi");

        ArrayList<Document> splicDocsToDBList = new ArrayList<>();


        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toString()))) {
            String line;

            // get / skip header
            String[] headers = br.readLine().split("\\t");


            while ((line = br.readLine()) != null) {

                if (splicDocsToDBList.size() >= 10000) {
                    Document[] splicDocsArray = new Document[splicDocsToDBList.size()];
                    splicDocsArray = splicDocsToDBList.toArray(splicDocsArray);
                    currCollection.insert(splicDocsArray);
                    splicDocsToDBList = new ArrayList<>();
                }

                Document currDocument = new Document();
                String[] values = line.split("\\t");

                currDocument.put("event", values[0]);
                for(int i= 1; (i < headers.length  && i < values.length) ; i++){
                    if (values[i].length() > 0 ){
                        if(values[i].equals("")){
                            currDocument.put(headers[i], 0.);
                        }else{
                            currDocument.put(headers[i], Double.valueOf(values[i]));
                        }

                    }
                }
                splicDocsToDBList.add(currDocument);

            }

            if(splicDocsToDBList.size() > 0){
                Document[] splicDocsArray = new Document[splicDocsToDBList.size()];
                splicDocsArray = splicDocsToDBList.toArray(splicDocsArray);
                currCollection.insert(splicDocsArray);
            }

            currCollection.createIndex("event", IndexOptions.indexOptions(IndexType.NonUnique, false));


        } catch (IOException e) {
            e.printStackTrace();
        }


        db.close();
    }

    /**
     * parses the splicingAllEvents_psiPerSample.csv file
     * @param filePath
     * order of the file columns (tab separated):
     * geneName	event	delta_psi	pvalue	event_type  pepEvid
     *  0        1           2       3       4           5
     */
    public void spliceDPSIParser(Path filePath) {


        Nitrite db = Nitrite.builder().filePath(databasePathAndName ).openOrCreate();
        NitriteCollection dPsiCollection = db.getCollection("SplicingDPSI_"+ filePath.getParent().getFileName().toString());

        ArrayList<Document> splicDocsToDBList = new ArrayList<>();


        try  {

            ArrayList<Document> dpsiDocsToDBList = new ArrayList<>();

            JSONParser parser = new JSONParser();

            boolean hasBlastName = false;

            try {
                Object obj = parser.parse(new FileReader(filePath.toString()));

                JSONObject events = (JSONObject) obj;
                Set<String> eventNames = events.keySet();
                for (String event: eventNames) {
                    JSONObject eventObj = (JSONObject) events.get(event);
                    if (dpsiDocsToDBList.size() >= 10000) {
                        Document[] splicDocsArray = new Document[dpsiDocsToDBList.size()];
                        splicDocsArray = dpsiDocsToDBList.toArray(splicDocsArray);
                        dPsiCollection.insert(splicDocsArray);
                        dpsiDocsToDBList = new ArrayList<>();
                    }



                    Document dpsiDocument = new Document(eventObj);
                    String comp = filePath.getParent().getFileName().toString();

                    if(dpsiDocument.get(comp+"_delta_psi")!=null){


                        dpsiDocument.put("geneName", eventObj.get("gene_id"));
                        dpsiDocument.put("event", event);
                        dpsiDocument.put("deltaPsi", dpsiDocument.get(comp+"_delta_psi"));
                        if(dpsiDocument.containsKey(comp+"_pvalue"))
                            dpsiDocument.put("pval", dpsiDocument.get(comp+"_pvalue"));
                        dpsiDocument.put("pepEvidence", dpsiDocument.get("peptide_evidence"));
                        dpsiDocument.put("eventType", dpsiDocument.get("event_type"));

                        if (eventObj.containsKey("protein")){
                            dpsiDocument.put("protein", eventObj.get("protein"));
                        }
                        dpsiDocsToDBList.add(dpsiDocument);
                    }




                }

                if(dpsiDocsToDBList.size() > 0){
                    Document[] splicDocsArray = new Document[dpsiDocsToDBList.size()];
                    splicDocsArray = dpsiDocsToDBList.toArray(splicDocsArray);
                    dPsiCollection.insert(splicDocsArray);
                }



            } catch (ParseException e) {
                e.printStackTrace();
            }

            dPsiCollection.createIndex("event", IndexOptions.indexOptions(IndexType.NonUnique, false));
            dPsiCollection.createIndex("pval", IndexOptions.indexOptions(IndexType.NonUnique, false));
            dPsiCollection.createIndex("geneName", IndexOptions.indexOptions(IndexType.NonUnique, false));
            //dPsiCollection.createIndex("geneRatioDiff", IndexOptions.indexOptions(IndexType.NonUnique, false));

        } catch (IOException e) {
            e.printStackTrace();
        }
        db.close();
    }

    public void transcriptUsageParser(Path filePath) {
        System.out.println(filePath);
        Nitrite db = Nitrite.builder().filePath(databasePathAndName ).openOrCreate();
        NitriteCollection currCollection = db.getCollection("transcriptUsage");

        ArrayList<Document> splicDocsToDBList = new ArrayList<>();


        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toString()))) {
            String line;

            // get / skip header
            String[] headers = br.readLine().split(",");


            while ((line = br.readLine()) != null) {

                if (splicDocsToDBList.size() >= 10000) {
                    Document[] splicDocsArray = new Document[splicDocsToDBList.size()];
                    splicDocsArray = splicDocsToDBList.toArray(splicDocsArray);
                    currCollection.insert(splicDocsArray);
                    splicDocsToDBList = new ArrayList<>();
                }

                Document currDocument = new Document();
                String[] values = line.split(",");

                String[] eventSplit = values[0].split(";");

                currDocument.put("geneName", eventSplit[0]);
                currDocument.put("transcript", eventSplit[1]);


                HashMap<String, Double> psi = new HashMap<>();

                for(int i= 1; (i < headers.length  && i < values.length) ; i++){
                    if (values[i].length() > 0 ){
                        if(values[i].equals("")){
                            psi.put(headers[i], 0.);
                        }else{
                            psi.put(headers[i], Double.valueOf(values[i]));
                        }
                    }
                }
                currDocument.put("psi", psi);

                splicDocsToDBList.add(currDocument);

            }

            if(splicDocsToDBList.size() > 0){
                Document[] splicDocsArray = new Document[splicDocsToDBList.size()];
                splicDocsArray = splicDocsToDBList.toArray(splicDocsArray);
                currCollection.insert(splicDocsArray);
            }

            currCollection.createIndex("geneName", IndexOptions.indexOptions(IndexType.NonUnique, false));
            currCollection.createIndex("transcript", IndexOptions.indexOptions(IndexType.NonUnique, false));


        } catch (IOException e) {
            e.printStackTrace();
        }


        db.close();
    }

    public void transcriptUsageDPSIParser(Path filePath) {
        System.out.println(filePath);
        Nitrite db = Nitrite.builder().filePath(databasePathAndName).openOrCreate();
        NitriteCollection dPsiCollection = db.getCollection("transcriptUsageDPSI_"+ filePath.getParent().getFileName().toString());

        ArrayList<Document> splicDocsToDBList = new ArrayList<>();


        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toString()))) {
            String line;

            //skip header
            br.readLine();

            while ((line = br.readLine()) != null) {

                if (splicDocsToDBList.size() >= 10000) {
                    Document[] splicDocsArray = new Document[splicDocsToDBList.size()];
                    splicDocsArray = splicDocsToDBList.toArray(splicDocsArray);
                    dPsiCollection.insert(splicDocsArray);
                    splicDocsToDBList = new ArrayList<>();
                }

                Document currDocument = new Document();
                String[] values = line.split("\\t");

                // pass if delta_psi  = ""
                if (values[1].length() == 0){
                    continue;
                }

                String[] eventIDSplit = values[0].split(";");

                currDocument.put("geneName", eventIDSplit[0]);
                currDocument.put("transcript", eventIDSplit[1]);
                currDocument.put("deltaPsi", Double.valueOf(values[1]));
                currDocument.put("pval", Double.valueOf(values[2]));

                splicDocsToDBList.add(currDocument);

            }

            if(splicDocsToDBList.size() > 0){
                Document[] splicDocsArray = new Document[splicDocsToDBList.size()];
                splicDocsArray = splicDocsToDBList.toArray(splicDocsArray);
                dPsiCollection.insert(splicDocsArray);
            }

            dPsiCollection.createIndex("geneName", IndexOptions.indexOptions(IndexType.NonUnique, false));
            dPsiCollection.createIndex("transcript", IndexOptions.indexOptions(IndexType.Unique, false));
            dPsiCollection.createIndex("pval", IndexOptions.indexOptions(IndexType.NonUnique, false));

        } catch (IOException e) {
            e.printStackTrace();
        }
        db.close();
    }

    public void parseEventPeptides(String filePath){
        Nitrite db = Nitrite.builder().filePath(databasePathAndName).openOrCreate();
        NitriteCollection dgeCollection = db.getCollection("eventPeptides");


        ArrayList<Document> dgeDocsToDBList = new ArrayList<>();

        JSONParser parser = new JSONParser();

        try {

            Object obj = parser.parse(new FileReader(filePath));

            JSONObject events = (JSONObject) obj;
            for (Object o: events.keySet()) {

                String eventID = (String) o;

                JSONObject event = (JSONObject) events.get(eventID);
                if (dgeDocsToDBList.size() >= 10000) {
                    Document[] splicDocsArray = new Document[dgeDocsToDBList.size()];
                    splicDocsArray = dgeDocsToDBList.toArray(splicDocsArray);
                    dgeCollection.insert(splicDocsArray);
                    dgeDocsToDBList = new ArrayList<>();
                }


                Document doc = new Document(event);
                doc.put("event", eventID);

                dgeDocsToDBList.add(doc);


            }

            if(dgeDocsToDBList.size() > 0){
                Document[] splicDocsArray = new Document[dgeDocsToDBList.size()];
                splicDocsArray = dgeDocsToDBList.toArray(splicDocsArray);
                dgeCollection.insert(splicDocsArray);
            }

            dgeCollection.createIndex("event", IndexOptions.indexOptions(IndexType.Unique, false));

            db.close();



        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }

    private void ptmParser(Path filePath){
        Nitrite db = Nitrite.builder().filePath(databasePathAndName).openOrCreate();
        NitriteCollection ptmCollection = db.getCollection("ptm");


        ArrayList<Document> dgeDocsToDBList = new ArrayList<>();

        JSONParser parser = new JSONParser();

        try {

            Object obj = parser.parse(new FileReader(filePath.toString()));

            JSONObject ptms = (JSONObject) obj;
            for (Object o: ptms.keySet()) {

                String ptmId = (String) o;

                JSONObject ptm = (JSONObject) ptms.get(ptmId);
                if (dgeDocsToDBList.size() >= 10000) {
                    Document[] splicDocsArray = new Document[dgeDocsToDBList.size()];
                    splicDocsArray = dgeDocsToDBList.toArray(splicDocsArray);
                    ptmCollection.insert(splicDocsArray);
                    dgeDocsToDBList = new ArrayList<>();
                }


                Document doc = new Document(ptm);
                doc.put("id", ptmId);
                doc.put("comparison", filePath.getParent().getFileName().toString());
                doc.put("type", filePath.getParent().getParent().getFileName().toString());


                dgeDocsToDBList.add(doc);


            }

            if(dgeDocsToDBList.size() > 0){
                Document[] splicDocsArray = new Document[dgeDocsToDBList.size()];
                splicDocsArray = dgeDocsToDBList.toArray(splicDocsArray);
                ptmCollection.insert(splicDocsArray);
            }

            db.close();



        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }

    private void kinaseActivityParser(Path filePath){
        Nitrite db = Nitrite.builder().filePath(databasePathAndName).openOrCreate();
        NitriteCollection ptmCollection = db.getCollection("kinaseActivity");


        ArrayList<Document> dgeDocsToDBList = new ArrayList<>();

        JSONParser parser = new JSONParser();

        try {

            Object obj = parser.parse(new FileReader(filePath.toString()));

            JSONObject ptms = (JSONObject) obj;
            for (Object o: ptms.keySet()) {

                String ptmId = (String) o;

                JSONObject ptm = (JSONObject) ptms.get(ptmId);
                if (dgeDocsToDBList.size() >= 10000) {
                    Document[] splicDocsArray = new Document[dgeDocsToDBList.size()];
                    splicDocsArray = dgeDocsToDBList.toArray(splicDocsArray);
                    ptmCollection.insert(splicDocsArray);
                    dgeDocsToDBList = new ArrayList<>();
                }


                Document doc = new Document(ptm);
                doc.put("id", ptmId);
                doc.put("comparison", filePath.getParent().getFileName().toString());
                doc.put("type", filePath.getParent().getParent().getFileName().toString());


                dgeDocsToDBList.add(doc);


            }

            if(dgeDocsToDBList.size() > 0){
                Document[] splicDocsArray = new Document[dgeDocsToDBList.size()];
                splicDocsArray = dgeDocsToDBList.toArray(splicDocsArray);
                ptmCollection.insert(splicDocsArray);
            }

            db.close();



        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }



    private void blastParser(String filePath){
        Nitrite db = Nitrite.builder().filePath(databasePathAndName).openOrCreate();
        NitriteCollection blastCollection = db.getCollection("blast");


        ArrayList<Document> dgeDocsToDBList = new ArrayList<>();

        JSONParser parser = new JSONParser();

        try {

            Object obj = parser.parse(new FileReader(filePath));

            JSONArray proteins = (JSONArray) obj;
            for (Object o: proteins) {

                JSONObject protein = (JSONObject) o;

                if (dgeDocsToDBList.size() >= 10000) {
                    Document[] splicDocsArray = new Document[dgeDocsToDBList.size()];
                    splicDocsArray = dgeDocsToDBList.toArray(splicDocsArray);
                    blastCollection.insert(splicDocsArray);
                    dgeDocsToDBList = new ArrayList<>();
                }


                Document doc = new Document(protein);


                dgeDocsToDBList.add(doc);


            }

            if(dgeDocsToDBList.size() > 0){
                Document[] splicDocsArray = new Document[dgeDocsToDBList.size()];
                splicDocsArray = dgeDocsToDBList.toArray(splicDocsArray);
                blastCollection.insert(splicDocsArray);
            }

            blastCollection.createIndex("lowestEvalue", IndexOptions.indexOptions(IndexType.NonUnique, false));
            blastCollection.createIndex("protein", IndexOptions.indexOptions(IndexType.Unique, false));
            blastCollection.createIndex("hasPeptideEvidence", IndexOptions.indexOptions(IndexType.NonUnique, false));

            db.close();



        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }





    public void createIndexes(){

        try{
            Nitrite db = Nitrite.builder().filePath(databasePathAndName).openOrCreate();
            NitriteCollection varsCondSampleCollection = db.getCollection("varsCondSample");

            NitriteCollection genesCollection = db.getCollection("allGenes");
            NitriteCollection readCountCollection = db.getCollection("readCounts");
            NitriteCollection protQuantCollection = db.getCollection("proteinQuant");
            NitriteCollection peptideQuantCollection = db.getCollection("peptideQuant");
            NitriteCollection peptideMapCollection = db.getCollection("peptideMap");
            NitriteCollection genePeptidesCollection = db.getCollection("genePeptides");
            NitriteCollection ptmCollection = db.getCollection("ptm");
            NitriteCollection allTranscriptsCollection = db.getCollection("allTranscripts");


            varsCondSampleCollection.createIndex("gene", IndexOptions.indexOptions(IndexType.NonUnique, false));
            genesCollection.createIndex("symbol", IndexOptions.indexOptions(IndexType.NonUnique, false));
            readCountCollection.createIndex("gene", IndexOptions.indexOptions(IndexType.Unique, false));
            protQuantCollection.createIndex("gene", IndexOptions.indexOptions(IndexType.NonUnique, false));
            peptideQuantCollection.createIndex("peptide", IndexOptions.indexOptions(IndexType.NonUnique, false));

            peptideMapCollection.createIndex("run", IndexOptions.indexOptions(IndexType.NonUnique, false));
            peptideMapCollection.createIndex("peptide", IndexOptions.indexOptions(IndexType.NonUnique, false));

            genePeptidesCollection.createIndex("gene", IndexOptions.indexOptions(IndexType.NonUnique, false));
            genePeptidesCollection.createIndex("run", IndexOptions.indexOptions(IndexType.NonUnique, false));

            allTranscriptsCollection.createIndex("gene", IndexOptions.indexOptions(IndexType.NonUnique, false));
            allTranscriptsCollection.createIndex("transcriptID", IndexOptions.indexOptions(IndexType.Unique, false));

            ptmCollection.createIndex("type", IndexOptions.indexOptions(IndexType.NonUnique, false));
            ptmCollection.createIndex("gene", IndexOptions.indexOptions(IndexType.NonUnique, false));
            ptmCollection.createIndex("id", IndexOptions.indexOptions(IndexType.NonUnique, false));
            ptmCollection.createIndex("run", IndexOptions.indexOptions(IndexType.NonUnique, false));


            db.close();
            System.out.println("indexing end");

        }catch (Exception e){
            e.printStackTrace();
        }


    }

}
