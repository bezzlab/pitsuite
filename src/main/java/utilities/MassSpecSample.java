package utilities;

import Cds.PSM;

import java.util.ArrayList;

public class MassSpecSample extends Sample{

    ArrayList<PSM> psms = new ArrayList<>();

    public MassSpecSample(String condition) {
        super(condition);
    }

    public MassSpecSample(String condition, String sample) {
        super(condition, sample);
    }

    public void addPsm(PSM psm){
        psms.add(psm);
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

    public int getSpectralCount(){
        return psms.size();
    }
}
