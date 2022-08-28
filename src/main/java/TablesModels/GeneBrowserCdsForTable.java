package TablesModels;

public class GeneBrowserCdsForTable {
    String transcId;
    String cdsId;
    String condition;
    String sample;
    Integer nPsm;

    public GeneBrowserCdsForTable(String transcId, String cdsId, String condition, String sample, Integer nPsm) {
        this.transcId = transcId;
        this.cdsId = cdsId;
        this.condition = condition;
        this.sample = sample;
        this.nPsm = nPsm;
    }

    public String getTranscId() {
        return transcId;
    }

    public String getCdsId() {
        return cdsId;
    }

    public String getCondition() {
        return condition;
    }

    public String getSample() {
        return sample;
    }

    public Integer getNPsm() {
        return nPsm;
    }
}
