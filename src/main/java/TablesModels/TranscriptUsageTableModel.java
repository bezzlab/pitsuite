package TablesModels;

public class TranscriptUsageTableModel {

    private String gene;
    private String transcript;
    private double deltaPsi;
    private double pval;


    public TranscriptUsageTableModel(String transcript, String gene, double deltaPsi, double pval) {
        this.gene = gene;
        this.transcript = transcript;
        this.deltaPsi = deltaPsi;
        this.pval = pval;
    }

    public String getGene() {
        return gene;
    }

    public void setGene(String gene) {
        this.gene = gene;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public double getDeltaPsi() {
        return deltaPsi;
    }

    public void setDeltaPsi(double deltaPsi) {
        this.deltaPsi = deltaPsi;
    }

    public double getPval() {
        return pval;
    }

    public void setPval(double pval) {
        this.pval = pval;
    }
}
