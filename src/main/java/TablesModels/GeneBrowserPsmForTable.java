package TablesModels;

public class GeneBrowserPsmForTable {
    String sequence;
    double prob;
    String modifications;

    public GeneBrowserPsmForTable(String sequence, double prob, String modifications) {
        this.sequence = sequence;
        this.prob = prob;
        this.modifications = modifications;
    }


    public String getSequence() {
        return sequence;
    }

    public double getProb() {
        return prob;
    }

    public String getModifications() {
        return modifications;
    }
}
