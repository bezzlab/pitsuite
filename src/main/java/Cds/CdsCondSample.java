package Cds;

import java.util.ArrayList;

public class CdsCondSample {
    private String transcID;
    private String condition;
    private String sample;
    private String cdsID;
    private int start;
    private int end;
    private String type;
    private String strand;
    private String sequence;
    private ArrayList<Pfam> pfams;
    private ArrayList<PSM> PSMS;

    public CdsCondSample(String transcID, String condition, String sample, String cdsID, int start, int end, String type, String strand, String sequence) {
        this.transcID = transcID;
        this.condition = condition;
        this.sample = sample;
        this.cdsID = cdsID;
        this.start = start;
        this.end = end;
        this.type = type;
        this.strand = strand;
        this.sequence = sequence;
        this.pfams = new ArrayList<>();
        this.PSMS = new ArrayList<>();
    }


    public void setPfams(ArrayList<Pfam> pfams) {  // not all cds have pfam
        this.pfams = pfams;
    }

    public void setPeptides(ArrayList<PSM> PSMS) { // not all cds have peptides
        this.PSMS = PSMS;
    }

    public String getTranscID() {
        return transcID;
    }

    public String getCondition() {
        return condition;
    }

    public String getSample() {
        return sample;
    }

    public String getCdsID() {
        return cdsID;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getType() {
        return type;
    }

    public String getStrand() {
        return strand;
    }

    public String getSequence() {
        return sequence;
    }

    public ArrayList<Pfam> getPfams() {
        return pfams;
    }

    public ArrayList<PSM> getPeptides() {
        return PSMS;
    }

    public boolean hasPfam(){
        if (pfams.size() > 0 ){
            return true;
        } else {
            return false;
        }
    }


    public boolean hasPeptides(){
        if (PSMS.size() > 0 ){
            return true;
        } else {
            return false;
        }
    }

}
