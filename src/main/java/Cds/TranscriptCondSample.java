package Cds;

public class TranscriptCondSample {

    private String condition;
    private String sample;
    private Double tpm;

    public TranscriptCondSample(String condition, String sample, Double tpm) {
        this.condition = condition;
        this.sample = sample;
        this.tpm = tpm;
    }

    public String getCondition() {
        return condition;
    }

    public String getSample() {
        return sample;
    }

    public Double getTpm() {
        return tpm;
    }
}
