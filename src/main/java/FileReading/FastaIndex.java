package FileReading;

import javafx.util.Pair;

import java.io.*;
import java.util.HashMap;

public class FastaIndex {

    HashMap<String, Long> chromosomes;
    String path;


    public FastaIndex(String path){
        this.path = path;
        parseIndex(path);
    }



    private void parseIndex(String path){

        chromosomes = new HashMap<>();

        try(BufferedReader br = new BufferedReader(new FileReader(path+".fai"))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {

                String[] lineSplit = line.split("\t");
                chromosomes.put(lineSplit[0], Long.valueOf(lineSplit[2]));

                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getSequenceAt(String chr, int start, int end){


        long positionToRead = chromosomes.get(chr) + start - 1 + start/60;
        int amountBytesToRead = (int) (end - start  + (end - start + positionToRead % 61) / 60);
        //int amountBytesToRead = (int) (end - start );
        try {
            RandomAccessFile f = new RandomAccessFile(new File(path),"r");
            byte[] b = new byte[amountBytesToRead];
            f.seek(positionToRead);

            f.read(b);
            return new String(b).replaceAll("\n", "");


        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }



}
