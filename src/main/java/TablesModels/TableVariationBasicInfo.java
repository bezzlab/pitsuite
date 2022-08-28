package TablesModels;

public class TableVariationBasicInfo {
    private String transcId;
    private int pos;
    private String ref;
    private String alt;

    public TableVariationBasicInfo(String transcId, int pos, String ref, String alt) {
        this.transcId = transcId;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;
    }

    public String getTranscId() {
        return transcId;
    }

    public int getPos() {
        return pos;
    }

    public String getRef() {
        return ref;
    }

    public String getAlt() {
        return alt;
    }
}
