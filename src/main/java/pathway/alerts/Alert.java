package pathway.alerts;

public abstract class Alert {

    private String gene;

    public Alert(String gene) {
        this.gene = gene;
    }

    public String getGene() {
        return gene;
    }

    public void setGene(String gene) {
        this.gene = gene;
    }

    public abstract String getType();
}
