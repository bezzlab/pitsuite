package Cds;

public class TpmCondSample {
    private String transcId;
    private String condition;
    private String sample;
    private double tpm;

    public TpmCondSample(String transcId, String condition, String sample, double tpm) {
        this.transcId = transcId;
        this.condition = condition;
        this.sample = sample;
        this.tpm = tpm;
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

    public double getTpm() {
        return tpm;
    }
}
