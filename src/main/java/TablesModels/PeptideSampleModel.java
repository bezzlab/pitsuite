package TablesModels;

public class PeptideSampleModel {

    private String sample;
    private Double probability;

    public PeptideSampleModel(String sample, Double probability) {
        this.sample = sample;
        this.probability = probability;
    }

    public String getSample() {
        return sample;
    }

    public void setSample(String sample) {
        this.sample = sample;
    }

    public Double getProbability() {
        return probability;
    }

    public void setProbability(Double probability) {
        this.probability = probability;
    }
}
