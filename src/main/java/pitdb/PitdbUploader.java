package pitdb;

import com.mongodb.*;
import org.dizitart.no2.Cursor;
import org.dizitart.no2.Document;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteCollection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PitdbUploader {

    Nitrite db;
    String projectName;



    public PitdbUploader(Nitrite db, String projectName){
        this.db = db;
        this.projectName = projectName;
    }


    public void upload(){

        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));


        DB database = mongoClient.getDB("pitdb");

        for(String collectionName: db.listCollectionNames()){
            if(collectionName.equals("allTranscripts")){
                uploadAllTranscripts(database);
            }else if (collectionName.equals("alluploadTranscriptCountsuploadTranscriptCountsGenes")){
                uploadAllGenes(database);
            }else if (collectionName.equals("readCounts")){
                uploadReadCount(database);
            }else if (collectionName.equals("mutations")){
                uploadMutations(database);
            }else if (collectionName.contains("transcriptUsageDPSI")){
                uploadtranscriptUsageDPSI(database, collectionName);
            }else if (collectionName.contains("SplicingEvents_")){
                uploadSplicingEvent(database, collectionName);
            }else if (collectionName.equals("SplicingPsi")){
                uploadSplicingPsi(database);
            }else if (collectionName.contains("SplicingDPSI")){
                uploadSplicingDPSI(database, collectionName);
            }else if (collectionName.equals("transcriptUsage")){
                uploadTranscriptUsage(database);
            }else if (collectionName.equals("transcriptCounts")){
                uploadTranscriptCounts(database);
            }else if (collectionName.equals("config")){
                uploadConfig(database);
            }
        }


    }


    private void uploadAllTranscripts(DB mongoConnection){
        Cursor transcripts = db.getCollection("allTranscripts").find();

        DBCollection mongoCollection = mongoConnection.getCollection("allTranscripts");

        ArrayList<BasicDBObject> docsToInsert = new ArrayList<>();
        for(Document doc: transcripts){

            if (doc.containsKey("CDS")){
                HashMap<String, Object> allCds = (HashMap<String, Object>) doc.get("CDS");
                HashMap<String, Object> newCds = new HashMap<>();

                for(Map.Entry<String, Object> cds: allCds.entrySet()){
                    String newKey = cds.getKey().replaceAll("\\.", "_");
                    newCds.put(newKey, cds.getValue());
                }

                doc.replace("CDS", newCds);
            }

            doc.put("project", projectName);
            BasicDBObject mongoDoc = new BasicDBObject(doc);
            mongoDoc.remove("_modified");
            mongoDoc.remove("_revision");
            mongoDoc.remove("_id");
            docsToInsert.add(mongoDoc);
        }
        mongoCollection.insert(docsToInsert);

    }

    private void uploadAllGenes(DB mongoConnection){
        Cursor allGenes = db.getCollection("allGenes").find();

        DBCollection mongoCollection = mongoConnection.getCollection("allGenes");

        ArrayList<BasicDBObject> docsToInsert = new ArrayList<>();
        for(Document doc: allGenes){
            doc.put("project", projectName);
            BasicDBObject mongoDoc = new BasicDBObject(doc);
            mongoDoc.remove("_modified");
            mongoDoc.remove("_revision");
            mongoDoc.remove("_id");
            docsToInsert.add(mongoDoc);
        }
        mongoCollection.insert(docsToInsert);
    }

    private void uploadReadCount(DB mongoConnection){
        Cursor readCounts = db.getCollection("readCounts").find();

        DBCollection mongoCollection = mongoConnection.getCollection("readCounts");

        ArrayList<BasicDBObject> docsToInsert = new ArrayList<>();
        for(Document doc: readCounts){
            doc.put("project", projectName);
            BasicDBObject mongoDoc = new BasicDBObject(doc);
            mongoDoc.remove("_modified");
            mongoDoc.remove("_revision");
            mongoDoc.remove("_id");
            docsToInsert.add(mongoDoc);
        }
        mongoCollection.insert(docsToInsert);
    }

    private void uploadMutations(DB mongoConnection){
        Cursor mutations = db.getCollection("mutations").find();

        DBCollection mongoCollection = mongoConnection.getCollection("mutations");

        ArrayList<BasicDBObject> docsToInsert = new ArrayList<>();


        for(Document doc: mutations){
            doc.put("project", projectName);
            HashMap<String, Object> transcriptsPos = (HashMap<String, Object>) doc.get("transcriptsPos");
            HashMap<String, Object> newTranscriptsPos = new HashMap<>();

            for(Map.Entry<String, Object> entry: transcriptsPos.entrySet()){
                newTranscriptsPos.put(entry.getKey().replaceAll("\\.", "_"), entry.getValue());
            }

            doc.replace("transcriptsPos", newTranscriptsPos);


            BasicDBObject mongoDoc = new BasicDBObject(doc);
            mongoDoc.remove("_modified");
            mongoDoc.remove("_revision");
            mongoDoc.remove("_id");
            docsToInsert.add(mongoDoc);
        }
        mongoCollection.insert(docsToInsert);
    }

    private void uploadtranscriptUsageDPSI(DB mongoConnection, String collectionName){
        String comparison = collectionName.split("_")[1];

        Cursor transcriptsUsageDpsi = db.getCollection(collectionName).find();

        DBCollection mongoCollection = mongoConnection.getCollection("transcriptUsageDPSI");

        ArrayList<BasicDBObject> docsToInsert = new ArrayList<>();
        for(Document doc: transcriptsUsageDpsi){
            doc.put("project", projectName);
            BasicDBObject mongoDoc = new BasicDBObject(doc);
            mongoDoc.remove("_modified");
            mongoDoc.remove("_revision");
            mongoDoc.remove("_id");
            mongoDoc.put("comparison", comparison);
            docsToInsert.add(mongoDoc);
        }
        mongoCollection.insert(docsToInsert);

    }

    private void uploadSplicingEvent(DB mongoConnection, String collectionName){
        String comparison = collectionName.split("_")[1];

        Cursor events = db.getCollection(collectionName).find();

        DBCollection mongoCollection = mongoConnection.getCollection("SplicingEvents");

        ArrayList<BasicDBObject> docsToInsert = new ArrayList<>();
        for(Document doc: events){
            doc.put("project", projectName);
            BasicDBObject mongoDoc = new BasicDBObject(doc);
            mongoDoc.remove("_modified");
            mongoDoc.remove("_revision");
            mongoDoc.remove("_id");
            mongoDoc.put("comparison", comparison);
            docsToInsert.add(mongoDoc);
        }
        mongoCollection.insert(docsToInsert);
    }

    private void uploadSplicingPsi(DB mongoConnection){
        Cursor eventsPsi = db.getCollection("SplicingPsi").find();

        DBCollection mongoCollection = mongoConnection.getCollection("SplicingPsi");

        ArrayList<BasicDBObject> docsToInsert = new ArrayList<>();
        for(Document doc: eventsPsi){
            doc.put("project", projectName);
            BasicDBObject mongoDoc = new BasicDBObject(doc);
            mongoDoc.remove("_modified");
            mongoDoc.remove("_revision");
            mongoDoc.remove("_id");
            docsToInsert.add(mongoDoc);
        }
        mongoCollection.insert(docsToInsert);
    }

    private void uploadSplicingDPSI(DB mongoConnection, String collectionName){
        String comparison = collectionName.split("_")[1];

        Cursor eventsDpsi = db.getCollection(collectionName).find();

        DBCollection mongoCollection = mongoConnection.getCollection("SplicingDPSI");

        ArrayList<BasicDBObject> docsToInsert = new ArrayList<>();
        for(Document doc: eventsDpsi){
            doc.put("project", projectName);
            BasicDBObject mongoDoc = new BasicDBObject(doc);
            mongoDoc.remove("_modified");
            mongoDoc.remove("_revision");
            mongoDoc.remove("_id");
            mongoDoc.put("comparison", comparison);
            docsToInsert.add(mongoDoc);
        }
        mongoCollection.insert(docsToInsert);
    }

    private void uploadTranscriptUsage(DB mongoConnection){
        Cursor transcritpUsages = db.getCollection("transcriptUsage").find();

        DBCollection mongoCollection = mongoConnection.getCollection("transcriptUsage");

        ArrayList<BasicDBObject> docsToInsert = new ArrayList<>();
        for(Document doc: transcritpUsages){
            doc.put("project", projectName);
            BasicDBObject mongoDoc = new BasicDBObject(doc);
            mongoDoc.remove("_modified");
            mongoDoc.remove("_revision");
            mongoDoc.remove("_id");
            docsToInsert.add(mongoDoc);
        }
        mongoCollection.insert(docsToInsert);
    }

    private void uploadTranscriptCounts(DB mongoConnection){
        Cursor transcriptsCounts = db.getCollection("transcriptCounts").find();

        DBCollection mongoCollection = mongoConnection.getCollection("transcriptCounts");

        ArrayList<BasicDBObject> docsToInsert = new ArrayList<>();
        for(Document doc: transcriptsCounts){
            doc.put("project", projectName);
            BasicDBObject mongoDoc = new BasicDBObject(doc);
            mongoDoc.remove("_modified");
            mongoDoc.remove("_revision");
            mongoDoc.remove("_id");
            docsToInsert.add(mongoDoc);
        }
        mongoCollection.insert(docsToInsert);
    }

    private void uploadPeptideMap(DB mongoConnection){

    }

    private void uploadGenePeptides(DB mongoConnection){

    }

    private void uploadConfig(DB mongoConnection){
        Document doc = db.getCollection("config").find().firstOrDefault();

        DBCollection mongoCollection = mongoConnection.getCollection("config");

        BasicDBObject mongoDoc = new BasicDBObject(doc);
        mongoDoc.remove("_modified");
        mongoDoc.remove("_revision");
        mongoDoc.remove("_id");
        mongoDoc.put("project", projectName);
        mongoCollection.insert(mongoDoc);
    }

}
