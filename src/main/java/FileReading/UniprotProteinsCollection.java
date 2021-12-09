package FileReading;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class UniprotProteinsCollection  {


    private static final HashMap<String, ArrayList<UniprotProtein>> proteins = new HashMap<>();

    public UniprotProteinsCollection(){
    }

    public static void load(){
        BufferedReader br;

        try {
            br = new BufferedReader(new FileReader("/media/esteban/b0b05e8c-7bfc-4553-ae68-f70bfe910d3e/PTEN/uniprotIds.csv"));
            StringBuilder sb = new StringBuilder();
            br.readLine();
            String line = br.readLine();

            while (line != null) {
                String[] lineSplit = line.split(",");
                UniprotProtein protein;
                if(lineSplit.length==7){
                    protein = new UniprotProtein(lineSplit[1], lineSplit[0], lineSplit[3], new HashSet<>(List.of(lineSplit[6].split(";"))), lineSplit[5]);
                }else{
                    protein = new UniprotProtein(lineSplit[1], lineSplit[0], lineSplit[3], new HashSet<>(), lineSplit[5]);
                }

                if(!proteins.containsKey(protein.getGeneName())){
                    proteins.put(protein.getGeneName(), new ArrayList<>());
                }
                proteins.get(protein.getGeneName()).add(protein);
                line = br.readLine();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static HashMap<String, ArrayList<UniprotProtein>> getProteins() {
        return proteins;
    }

    public static HashSet<String> getPDBIds(String gene) {
        final HashSet<String> ids = new HashSet<>();
        if(proteins.containsKey(gene)){
            for(UniprotProtein protein: proteins.get(gene)){
                ids.addAll(protein.getPdbIds());
            }
        }
        return ids;
    }

    public static HashSet<String> getTranscriptIds(String gene) {
        final HashSet<String> ids = new HashSet<>();
        if(proteins.containsKey(gene)){
            for(UniprotProtein protein: proteins.get(gene)){
                ids.add(protein.getTranscriptId());
            }
        }
        return ids;
    }

    public static HashSet<String> getSequences(String gene) {
        final HashSet<String> ids = new HashSet<>();
        if(proteins.containsKey(gene)){
            for(UniprotProtein protein: proteins.get(gene)){
                ids.add(protein.getSequence());
            }
        }
        return ids;
    }
}

