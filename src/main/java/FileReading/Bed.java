package FileReading;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Bed {

    ArrayList<Bedrow> rows = new ArrayList<>();


    public Bed(String path){
        read(path);
    }

    public void read(String path){
        try(BufferedReader br = new BufferedReader(new FileReader(path))) {
            for(String line; (line = br.readLine()) != null; ) {
                String[] lineSplit = line.split("\t");
                rows.add(new Bedrow(lineSplit[0], Integer.parseInt(lineSplit[1]), Integer.parseInt(lineSplit[2])));
            }
            // line is not visible here.
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public ArrayList<Bedrow> getRows() {
        return rows;
    }

    public class Bedrow{
        private String chr;
        private int start;
        private int end;

        public Bedrow(String chr, int start, int end) {
            this.chr = chr;
            this.start = start;
            this.end = end;
        }

        public String getChr() {
            return chr;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }
    }
}
