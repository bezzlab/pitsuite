package FileReading;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class PhosphoParser {

    private ArrayList<Phosphosite> phosphosites = new ArrayList<>();

    public PhosphoParser(){

        try {
            BufferedReader br = new BufferedReader(new FileReader("/media/esteban/data/PTEN/ms/phospho/deseq2.csv"));
            StringBuilder sb = new StringBuilder();
            br.readLine();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                String[] lineSplit = line.split(",");
                phosphosites.add(new Phosphosite(lineSplit[0], lineSplit[2], lineSplit[6]));
                line = br.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Phosphosite> getPhosphosites() {
        return phosphosites;
    }
}
