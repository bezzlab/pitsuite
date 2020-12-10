package mongoDB;

import java.util.ArrayList;
import java.util.HashMap;

public class TranscriptReader {
    String seq, strand, chr;
    int start, end;
    double TPM;
    String symbol;
    ArrayList<ArrayList<Double>> exons;
    ArrayList<VariationReader> variations;
    HashMap<String, CdsJsonReader> CDS;


    public HashMap<String, CdsJsonReader> getCDS() {
        return CDS;
    }

    public void setCDS(HashMap<String, CdsJsonReader> CDS) {
        this.CDS = CDS;
    }

    public ArrayList<VariationReader> getVariations() {
        return variations;
    }

    public void setVariations(ArrayList<VariationReader> variations) {
        this.variations = variations;
    }

    public String getSeq() {
        return seq;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }

    public String getStrand() {
        return strand;
    }

    public void setStrand(String strand) {
        this.strand = strand;
    }

    public String getChr() {
        return chr;
    }

    public void setChr(String chr) {
        this.chr = chr;
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

    public double getTpm() {
        return TPM;
    }

    public void setTpm(double tpm) {
        this.TPM = tpm;
    }

    public ArrayList<ArrayList<Double>> getExons() {
        return exons;
    }

    public void setExons(ArrayList<ArrayList<Double>> exons) {
        this.exons = exons;
    }

    public String getSymbol() {
        return symbol;
    }
}



