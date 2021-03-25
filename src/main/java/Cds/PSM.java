package Cds;

import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Peptide identified by Mass Spectrometry
 * This is how the json looks like
 *                     {
 *                         "run": "200415_DAS_OT_JAS_R08_230115_FWD_Fxn_0",
 *                         "sequence": "LAVHPSGVALQDR",
 *                         "probability": 0.9043,
 *                         "modifications": NaN
 *                     },
 */
public class PSM {
    private String run;
    private double prob;
    private String label;
    private HashSet<PTM> ptms;
    private int specIndex;
    private HashMap<String, Double> intensities;
    private String file;


    public PSM(String run, String sequence, double prob) {
        this.run = run;
        this.prob = prob;
    }

    public PSM(String run, String modifications) {
        this.run = run;
        ptms = PTM.parsePtms(modifications);
    }



    public PSM(String run, String modifications, double prob, String label, String file) {
        this.run = run;
        this.prob = prob;
        ptms = PTM.parsePtms(modifications);
        this.label = label;
        this.file = file;
    }



    public PSM(String modifications, double prob, String label, int specIndex, String file) {
        this.prob = prob;
        this.label = label;
        this.specIndex = specIndex;
        ptms = PTM.parsePtms(modifications);
        this.file = file;
    }


    public PSM(String modifications, double prob, int specIndex, String file) {
        this.prob = prob;
        this.specIndex = specIndex;
        ptms = PTM.parsePtms(modifications);
        this.file = file;

    }

    public PSM(String mod, double probability, String label, int specIndex, String file, HashMap intensities) {
        this.prob = probability;
        ptms = PTM.parsePtms(mod);
        this.label = label;
        this.file = file;
        this.specIndex = specIndex;
        this.intensities = intensities;
    }




    public String getRun() {
        return run;
    }


    public double getProb() {
        return prob;
    }

    public HashSet<PTM> getModifications() {
        return ptms;
    }



    public String getLabel(){
        return label;
    }

    public boolean contains(PTM ptm){
        for(PTM other: ptms){
            if(ptm.getMod().equals(other.getMod()) && ptm.getResidue().equals(other.getResidue())){
                return true;
            }
        }
        return false;
    }

    public boolean contains(HashSet<PTM> ptmsToTest){

        for(PTM ptm: ptmsToTest){
            for(PTM other: ptms){
                if(ptm.getMassShift()!=other.getMassShift() || !ptm.getResidue().equals(other.getResidue())){
                    return false;
                }
            }

        }
        return true;
    }

    public int getSpecIndex() {
        return specIndex;
    }

    public String getFile() {
        return file;
    }

    public HashMap<String, Double> getIntensities() {
        return intensities;
    }
}
