package Cds;

public class VariationCondSample {
    private String transcId;
    private String condition;
    private String sample;
    private int pos; // this is sequence position, not genomic coordinate
    private double qual;
    private String ref;
    private String alt;
    private double af;

    public VariationCondSample(String transcriptId, String condition, String sample, int pos, double qual, String ref, String alt, double af) {
        this.transcId = transcriptId;
        this.condition = condition;
        this.sample = sample;
        this.pos = pos;
        this.qual = qual;
        this.ref = ref;
        this.alt = alt;
        this.af = af;
    }
    public VariationCondSample(String condition, String sample, int pos, double qual, String ref, String alt, double af) {
        this.condition = condition;
        this.sample = sample;
        this.pos = pos;
        this.qual = qual;
        this.ref = ref;
        this.alt = alt;
        this.af = af;
    }

    public VariationCondSample(int pos, double qual, String ref, String alt, double af) {
    }

    public String getTranscId() {
        return transcId;
    }

    public String getCondition() {
        return condition;
    }

    public String getSample() {
        return sample;
    }

    public int getPos() {
        return pos;
    }

    public double getQual() {
        return qual;
    }

    public String getRef() {
        return ref;
    }

    public String getAlt() {
        return alt;
    }

    public double getAf() {
        return af;
    }
}
