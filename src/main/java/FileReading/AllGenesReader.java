package FileReading;

import Gene.Gene;
import TablesModels.KeggPathway;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.util.Pair;
import org.dizitart.no2.Cursor;
import org.dizitart.no2.Document;
import org.dizitart.no2.Nitrite;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class AllGenesReader {

    private ArrayList<KeggPathway> allPathways;
    private HashSet<String> allKeggNames;
    private HashMap<String, Gene> allGenes;
    private HashMap<String, ArrayList<String>> goTermsIsAMap;
    private HashMap<String, String> goTermsMap;
    private HashMap<String, ArrayList<String>> genesKegg;
    private Nitrite db;
    private SimpleBooleanProperty geneLoadedProperty;
    private LinkedList<String> goTermsStringForAutoCompletion;

    public AllGenesReader(String keggPath, String goTermsPath,  Nitrite db){
        this.db = db;
        geneLoadedProperty = new SimpleBooleanProperty(false);
        read(keggPath, goTermsPath);
    }


    private void read(String keggPath, String goTermsPath) {

        new Thread(() -> {
            allPathways = new ArrayList<>(337);
            allKeggNames = new HashSet<>();
            allGenes = new HashMap<>();
            goTermsIsAMap = new HashMap<>();
            goTermsMap = new HashMap<>();

            // read kegg pathways
            try {
                BufferedReader br = new BufferedReader(new FileReader(keggPath));

                br.readLine();
                String line = br.readLine();
                while (line != null) {

                    String[] lineSplit = line.split(",");
                    allPathways.add(new KeggPathway(lineSplit[0], lineSplit[1]));
                    allKeggNames.add(lineSplit[1]);
                    line = br.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // read GO terms is_a relations
            try {
                BufferedReader br = new BufferedReader(new FileReader(goTermsPath));


                String line;
                String goTerm = "";
                String goName = "";
                ArrayList<String> isATerms = new ArrayList<>();

                while ((line = br.readLine()) != null) {

                    if (line.contains("[Term]")){ // reestart

                        if (!goTerm.equals("")){ // not first item
                            goTermsIsAMap.put(goTerm, isATerms);
                            goTermsMap.put(goTerm, goName);
                        }


                        goTerm = "";
                        goName = "";
                        isATerms = new ArrayList<>();
                    }

                    if (line.startsWith("id: ")){
                        goTerm  = line.split(" ")[1];
                    } else if (line.startsWith("name: ")){
                        goName  = line.replace("name: ", "").replace("_", " ");
                    } else if (line.startsWith("is_a: ")){
                        isATerms.add(line.split(" ")[1]);
                    }


                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            genesKegg = new HashMap<>();


            Cursor genesCursor = db.getCollection("allGenes").find();


            for (Document geneGoc : genesCursor) {
                String symbol = (String) geneGoc.get("symbol");
                ArrayList<String> kegg = (ArrayList<String>) geneGoc.get("kegg");

                ArrayList<String> transcripts = (ArrayList<String>) geneGoc.get("transcripts");

                allGenes.put(symbol, new Gene(symbol, (String) geneGoc.get("chr"),(int)geneGoc.get("start"),
                                (int)geneGoc.get("end"), transcripts));

                if(kegg!=null){
                    genesKegg.put(symbol, kegg);
                }
            }

            goTermsStringForAutoCompletion = getGoTermsStringList();
            geneLoadedProperty.setValue(true);



        }).start();

    }


    public ArrayList<KeggPathway> getAllPathways() {
        return allPathways;
    }

    public HashSet<String> getAllKeggNames(){
        return allKeggNames;
    }

    public ArrayList<KeggPathway> getKegg(String geneSymbol){

        if(genesKegg.containsKey(geneSymbol)){
            ArrayList<String> ids =  genesKegg.get(geneSymbol);
            ArrayList<KeggPathway> pathways = new ArrayList<>(ids.size());
            for(String id: ids){
                pathways.add(allPathways.stream().filter(kegg -> id.equals(kegg.getId())).findFirst().orElse(null));
            }
            return pathways;
        }
        return null;
    }

    public Set<String> getAllGeneNames(){
        return allGenes.keySet();
    }

    public Gene getGene(String name){
        return allGenes.get(name);
    }

    public SimpleBooleanProperty getGenesLoadedProperty(){
        return geneLoadedProperty;
    }

    public HashMap<String, String> getGoTermsMap() {
        return goTermsMap;
    }


    private LinkedList<String> getGoTermsStringList(){
        LinkedList<String > goTermsList = new LinkedList<>();
        for(Map.Entry<String, String> kvGo: goTermsMap.entrySet()){
            String tmpGoString = kvGo.getKey() + ": " + kvGo.getValue();
            goTermsList.add(tmpGoString);
        }
        return goTermsList;
    }

    public LinkedList<String> getGoTermsStringForAutoCompletion() {
        return goTermsStringForAutoCompletion;
    }

    public HashMap<String, ArrayList<String>> getGoTermsIsAMap() {
        return goTermsIsAMap;
    }

}
