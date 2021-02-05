package Singletons;

import FileReading.Bed;
import TablesModels.BamFile;
import javafx.util.Pair;
import utilities.BioFile;

import java.util.ArrayList;
import java.util.HashMap;

public class TrackFiles {

    private static ArrayList<BamFile> bamFiles = null;
    private static ArrayList<Bed> bedFiles = null;



    public static void reset(){
        bamFiles = null;
        bedFiles = null;
    }

    public static void addBam(BamFile bamFile){
        if(bamFiles==null)
            bamFiles = new ArrayList<>();
        bamFiles.add(bamFile);
    }
    public static void addBed(Bed bedFile){
        if(bedFiles==null)
            bedFiles = new ArrayList<>();
        bedFiles.add(bedFile);
    }

    public static ArrayList<BamFile> getBamFiles(){
        return bamFiles;
    }
    public static ArrayList<Bed> getBedFiles(){
        return bedFiles;
    }

}
