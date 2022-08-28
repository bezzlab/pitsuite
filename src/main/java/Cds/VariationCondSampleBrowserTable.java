package Cds;

public class VariationCondSampleBrowserTable extends VariationCondSample {


    private Integer genomicCoordinate;


    public VariationCondSampleBrowserTable(VariationCondSample variationCondSample, int genomicCoordinate){

        super(variationCondSample.getTranscId(), variationCondSample.getCondition(), variationCondSample.getSample(), variationCondSample.getPos(), variationCondSample.getQual(), variationCondSample.getRef(), variationCondSample.getAlt(), variationCondSample.getAf());

        this.genomicCoordinate = genomicCoordinate;

    }


    public Integer getGenomicCoordinate() {
        return genomicCoordinate;
    }
}
