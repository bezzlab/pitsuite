package mongoDB;

import java.util.ArrayList;

public class CdsJsonReader {
    int start, end;
    String type, strand, sequence;
    ArrayList<PeptideReader> peptides;
    ArrayList<PfamReader> pfam;

    public ArrayList<PfamReader> getPfam() {
        return pfam;
    }

    public void setPfam(ArrayList<PfamReader> pfam) {
        this.pfam = pfam;
    }

    public ArrayList<PeptideReader> getPeptides() {
        return peptides;
    }

    public void setPeptides(ArrayList<PeptideReader> peptides) {
        this.peptides = peptides;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStrand() {
        return strand;
    }

    public void setStrand(String strand) {
        this.strand = strand;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }
}
