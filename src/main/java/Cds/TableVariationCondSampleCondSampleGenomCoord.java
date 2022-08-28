package Cds;

public class TableVariationCondSampleCondSampleGenomCoord extends VariationCondSample {

    private String transcId;

    public TableVariationCondSampleCondSampleGenomCoord(String transcId, VariationCondSample variationCondSample){
        super(variationCondSample.getPos(), variationCondSample.getQual(), variationCondSample.getRef(), variationCondSample.getAlt(), variationCondSample.getAf());
        this.transcId = transcId;

    }

    public String getTranscId() {
        return transcId;
    }


}
