package Cds;

import org.json.JSONObject;

public class Mutation {

    String gene;
    double pos;
    String ref;
    String alt;
    String chr;
    boolean inCDS;
    boolean silent;
    JSONObject conditions;
    JSONObject transcripts;

    public Mutation(String gene, double pos, String ref, String alt) {
        this.gene = gene;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;
    }

    public Mutation(String gene, String chr, double pos, String ref, String alt, JSONObject conditions, JSONObject transcripts, boolean inCDS, boolean silent) {
        this.gene = gene;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;
        this.chr = chr;
        this.inCDS = inCDS;
        this.silent = silent;
        this.conditions = conditions;
        this.transcripts = transcripts;
    }

    public Mutation(String gene, String chr, double pos, String ref, String alt, JSONObject conditions, JSONObject transcripts) {
        this.gene = gene;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;
        this.chr = chr;

        this.conditions = conditions;
        this.transcripts = transcripts;
    }



    public String getGene() {
        return gene;
    }

    public void setGene(String gene) {
        this.gene = gene;
    }

    public double getPos() {
        return pos;
    }

    public void setPos(double pos) {
        this.pos = pos;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    @Override
    public String toString() {
        return "gene=" + gene + '\n' +
                "chromosome: " + chr + '\n' +
                "position: " + pos + '\n' +
                "ref: " + ref + '\n' +
                "alt: " + alt + '\n' +
                "in coding region: " + inCDS + '\n' +
                "silent: " + silent;
    }

    public String getChr() {
        return chr;
    }

    public boolean isInCDS() {
        return inCDS;
    }

    public boolean isSilent() {
        return silent;
    }

    public JSONObject getConditions() {
        return conditions;
    }

    public JSONObject getTranscripts() {
        return transcripts;
    }
}
