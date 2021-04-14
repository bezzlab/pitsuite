package TablesModels;

public class PeptideSampleModel {

    private String sample;
    private double probability;

    public PeptideSampleModel(String sample, double probability) {
        this.sample = sample;
        this.probability = probability;
    }

    public String getSample() {
        return sample;
    }

    public void setSample(String sample) {
        this.sample = sample;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }
}
