package utilities;

import Cds.PSM;
import Cds.PTM;
import Cds.Peptide;

import java.util.ArrayList;
import java.util.HashSet;

public class MassSpecModificationSample {


    HashSet<PTM> ptms;
    ArrayList<String> samplesContained = new ArrayList<>();
    ArrayList<PSM> psms = new ArrayList<>();

    public MassSpecModificationSample(HashSet<PTM> ptms) {
        this.ptms = ptms;
    }


    public void addPSM(PSM psm){
        psms.add(psm);
        if(!samplesContained.contains(psm.getLabel())){
            samplesContained.add(psm.getLabel());
        }
    }

    public boolean isInSample(String sample){
        return samplesContained.contains(sample);
    }



    public String ptmsToString(){

        if(ptms.size()==0) return "None";

        StringBuilder sb = new StringBuilder();
        int i=0;
        for(PTM ptm: ptms){
            sb.append(ptm.toString());
            if(i<ptms.size()-1){
                sb.append(", ");
            }
            i++;
        }
        return sb.toString();
    }

    public Double getProbability(){
        double probability = 1;
        for(PSM psm: psms){
            probability*=1-psm.getProb();
        }
        return 1 - probability;
    }

    public ArrayList<PSM> getPsms() {
        return psms;
    }

    public HashSet<PTM> getPtms() {
        return ptms;
    }
}
