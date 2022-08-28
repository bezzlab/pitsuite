package FileReading;

import javafx.util.Pair;

import java.io.*;
import java.util.HashMap;

public class FastaIndex {

    private HashMap<String, Long> chromosomes;
    private String path;
    private int nucleotidesPerLine;


    public FastaIndex(String path){
        this.path = path;
        parseIndex(path);
    }



    private void parseIndex(String path){

        chromosomes = new HashMap<>();

        System.out.println(path);
        try(BufferedReader br = new BufferedReader(new FileReader(path+".fai"))) {

            String line = br.readLine();

            while (line != null) {

                String[] lineSplit = line.split("\t");
                chromosomes.put(lineSplit[0], Long.valueOf(lineSplit[2]));

                line = br.readLine();
            }

            try(BufferedReader br2 = new BufferedReader(new FileReader(path))) {
                br2.readLine();
                line = br2.readLine();
                nucleotidesPerLine = line.length();

            } catch (IOException e) {
                e.printStackTrace();
            }



        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getSequenceAt(String chr, int start, int end){


        System.out.println(chromosomes);
        System.out.println(chr);
        long positionToRead = chromosomes.get(chr) + start - 1 + start/nucleotidesPerLine;
        int amountBytesToRead = (int) (end - start  +
                (start%nucleotidesPerLine +  end-start)     / nucleotidesPerLine);
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
