package pathway.alerts;

public class DgeAlert extends Alert {

    double fc;
    double pval;

    public DgeAlert(String gene, double fc, double pval) {
        super(gene);
        this.fc =fc;
        this.pval = pval;
    }

    public double getFc() {
        return fc;
    }

    public double getPval() {
        return pval;
    }

    @Override
    public String getType() {
        return "dge";
    }
}
