package TablesModels;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SplicingEventsTableModel {
    private String eventKey;
    private String geneSymbol;
    private String strand;
    private String eventType;
    private Integer exonStart;
    private Integer exonEnd;
    private Double deltaPsi;
    private Double pVal;
    private boolean hasPeptideEvidence;
    private Double geneRatioDiff;


    public SplicingEventsTableModel(String eventKey, String geneSymbol, String strand, String eventType, Integer exonStart,
                                    Integer exonEnd, Double deltaPsi, Double pVal, boolean hasPeptideEvidence, Double geneRatioDiff) {
        this.eventKey = eventKey;
        this.geneSymbol = geneSymbol;
        this.strand = strand;
        this.eventType = eventType;
        this.exonStart = exonStart;
        this.exonEnd = exonEnd;
        this.deltaPsi = deltaPsi;
        this.pVal = pVal;
        this.hasPeptideEvidence = hasPeptideEvidence;
        this.geneRatioDiff = geneRatioDiff;
    }

    public SplicingEventsTableModel(String eventKey, String geneSymbol, String eventType, Double deltaPsi, Double pVal,
                                    boolean hasPeptideEvidence, Double geneRatioDiff) {
        this.eventKey = eventKey;
        this.geneSymbol = geneSymbol;
        this.eventType = eventType;
        this.deltaPsi = deltaPsi;
        this.pVal = pVal;
        this.hasPeptideEvidence = hasPeptideEvidence;
        this.geneRatioDiff = geneRatioDiff;
        parseKey(eventKey, eventType);
    }

    public String getEventKey(){
        return eventKey;
    }

    public String getGeneSymbol() {
        return geneSymbol;
    }

    public String getStrand() {
        return strand;
    }

    public String getEventType() {
        return eventType;
    }

    public Integer getExonStart() {
        return exonStart;
    }

    public Integer getExonEnd() {
        return exonEnd;
    }

    public Double getDeltaPsi() {
        return deltaPsi;
    }

    public Double getPVal() {
        return pVal;
    }

    public Double getpVal() {
        return pVal;
    }

    public boolean isHasPeptideEvidence() {
        return hasPeptideEvidence;
    }

    private void parseKey(String spliceEventKey, String eventType){
        // parse splice event key to extract values (https://github.com/comprna/SUPPA)
        String[] spliceEventKeySplitted = spliceEventKey.split(";",1);

        List<String> spliceEventKeySplittedList = Arrays.asList(spliceEventKey.split(";"));
        String restKey = spliceEventKeySplittedList.get(1);
        List<String> keyElementsColonSplit = Arrays.asList(restKey.split(":"));
        String strand = keyElementsColonSplit.get(keyElementsColonSplit.size() - 1);


        int exonStart = 0;
        int exonEnd = 0;

        // if forward strand
        if (strand.equals("+")) {
            if (eventType.equals("SE")) {
                // eg. "ENSG00000187961.14;SE:chr1:960800-961293:961552-961629:+"
                // we want: 961293, 961552
                List<String> exonStartPartSplit = Arrays.asList(keyElementsColonSplit.get(2).split("-"));
                exonStart = Integer.parseInt(exonStartPartSplit.get(1));
                List<String> exonEndPartSplit = Arrays.asList(keyElementsColonSplit.get(3).split("-"));
                exonEnd = Integer.parseInt(exonEndPartSplit.get(0));
            } else if (eventType.equals("MX")) {
                // eg. "MX:chr1:1315618-1319296:1319524-1324581:1315618-1320996:1321093-1324581:-";
                //      0  1    2       1S      3  0E          4               5
                //      want: 1319296, 1319524  // .-1:2-.:
                List<String> exonStartPartSplit = Arrays.asList(keyElementsColonSplit.get(2).split("-"));
                exonStart = Integer.parseInt(exonStartPartSplit.get(1));
                List<String> exonEndPartSplit = Arrays.asList(keyElementsColonSplit.get(3).split("-"));
                exonEnd = Integer.parseInt(exonEndPartSplit.get(0));
            } else  if (eventType.equals("A5")){
                // eg. "A5:chr1:964180-964349:964167-964349:+";
                //      0  1    2  0E          3 0S
                // want: 964167, 964180    // 2-.:1.
                List<String> exonStartPartSplit = Arrays.asList(keyElementsColonSplit.get(3).split("-"));
                exonStart = Integer.parseInt(exonStartPartSplit.get(0));
                List<String> exonEndPartSplit = Arrays.asList(keyElementsColonSplit.get(2).split("-"));
                exonEnd = Integer.parseInt(exonEndPartSplit.get(0));
            } else  if (eventType.equals("A3")){
                // eg. "A3:chr1:962917-963032:962917-963109:+";
                //      0  1    2         1S  3        1E
                // want: 963032, 963109 //  .-1:.-2
                List<String> exonStartPartSplit = Arrays.asList(keyElementsColonSplit.get(2).split("-"));
                exonStart = Integer.parseInt(exonStartPartSplit.get(1));
                // exon start
                List<String> exonEndPartSplit = Arrays.asList(keyElementsColonSplit.get(3).split("-"));
                exonEnd = Integer.parseInt(exonEndPartSplit.get(1));
            }  else  if (eventType.equals("RI")) {
                // eg. "RI:chr1:961449:961552-961629:961750:+";
                //      0  1    2      3  0S    1E   4
                // // want: 961552, 961629 // .:1-2:.
                List<String> exonStartPartSplit = Arrays.asList(keyElementsColonSplit.get(3).split("-"));
                exonStart = Integer.parseInt(exonStartPartSplit.get(0));
                List<String> exonEndPartSplit = Arrays.asList(keyElementsColonSplit.get(3).split("-"));
                exonEnd = Integer.parseInt(exonEndPartSplit.get(1));
            } else  if (eventType.equals("AF")) {
                // eg. "AF:chr1:1623122:1623482-1623774:1623581:1623699-1623774:+";
                //      0  1    2  S    3 0E            4       5
                // // want: 1623122, 1623482 // 1:2-.:
                exonStart = Integer.parseInt(keyElementsColonSplit.get(2));
                List<String> exonEndPartSplit = Arrays.asList(keyElementsColonSplit.get(3).split("-"));
                exonEnd = Integer.parseInt(exonEndPartSplit.get(0));
            } else  if (eventType.equals("AL")) {
                // eg. "AL:chr1:1060393-1061020:1061117:1060393-1065830:1066274:+";
                //      0  1    2               3       4         1S     5 E
                // want: 1065830, 1066274 // .-.:.:.-1:2
                List<String> exonStartPartSplit = Arrays.asList(keyElementsColonSplit.get(4).split("-"));
                exonStart = Integer.parseInt(exonStartPartSplit.get(1));
                exonEnd = Integer.parseInt(keyElementsColonSplit.get(5));
            }

        } else { // reverse strand
            if (eventType.equals("SE")) {
                // "SE:chr1:1315618-1320996:1321093-1324581:-";
                // want: 1320996, 1321093 // .-1:2-.
                List<String> exonStartPartSplit = Arrays.asList(keyElementsColonSplit.get(2).split("-"));
                exonStart = Integer.parseInt(exonStartPartSplit.get(1));
                List<String> exonEndPartSplit = Arrays.asList(keyElementsColonSplit.get(3).split("-"));
                exonEnd = Integer.parseInt(exonEndPartSplit.get(0));
            } else if (eventType.equals("MX")) {
                //eg. "MX:chr1:183565779-183566920:183566988-183569142:183565779-183567204:183567345-183569142:-";
                // .-1:2-.:
                List<String> exonStartPartSplit = Arrays.asList(keyElementsColonSplit.get(2).split("-"));
                exonStart = Integer.parseInt(exonStartPartSplit.get(1));
                List<String> exonEndPartSplit = Arrays.asList(keyElementsColonSplit.get(3).split("-"));
                exonEnd = Integer.parseInt(exonEndPartSplit.get(0));
            } else  if (eventType.equals("A5")){
                // eg. "A5:chr2:6915967-6916816:6915967-6916850:-";
                //      0  1    2       1S      3        1E
                // want: 6916816, 42659370 // .-1:.-2
                List<String> exonStartPartSplit = Arrays.asList(keyElementsColonSplit.get(2).split("-"));
                exonStart = Integer.parseInt(exonStartPartSplit.get(1));
                List<String> exonEndPartSplit = Arrays.asList(keyElementsColonSplit.get(3).split("-"));
                exonEnd = Integer.parseInt(exonEndPartSplit.get(1));
            } else  if (eventType.equals("A3")){
                // eg. "A3:chr1:42659370-42659522:42659251-42659522:-";
                //      0  1    2  0E             3 0S
                // want: 42659251, 42659370 // 2-.:1-.
                List<String> exonStartPartSplit = Arrays.asList(keyElementsColonSplit.get(3).split("-"));
                exonStart = Integer.parseInt(exonStartPartSplit.get(0));
                List<String> exonEndPartSplit = Arrays.asList(keyElementsColonSplit.get(2).split("-"));
                exonEnd = Integer.parseInt(exonEndPartSplit.get(0));
            }  else  if (eventType.equals("RI")) {
                // eg. "RI:chr1:42658443:42658512-42658844:42658908:-";
                //      0  1    2        3  0S    1E       4
                // want:  42658512, 42658844 //  .:1-2:.
                List<String> exonStartPartSplit = Arrays.asList(keyElementsColonSplit.get(3).split("-"));
                exonStart = Integer.parseInt(exonStartPartSplit.get(0));
                List<String> exonEndPartSplit = Arrays.asList(keyElementsColonSplit.get(3).split("-"));
                exonEnd = Integer.parseInt(exonEndPartSplit.get(1));
            } else  if (eventType.equals("AF")) {
                // eg. "AF:chr2:9420100-9422479:9423228:9420100-9423373:9423480:-";
                //      0  1    2               3       4           1S  5  E
                // want: 9423373, 9423480  // .-.:.:.-1:2
                List<String> exonStartPartSplit = Arrays.asList(keyElementsColonSplit.get(4).split("-"));
                exonStart = Integer.parseInt(exonStartPartSplit.get(1));
                exonEnd = Integer.parseInt(keyElementsColonSplit.get(5));
            } else  if (eventType.equals("AL")) {
                // eg. "AL:chr2:9405684:9406905-9408113:9407572:9407598-9408113:-";
                //      0  1    2  S    3  0E           4       5
                // want: 9405684:9406905 // 1:2-.:
                exonStart= Integer.parseInt(keyElementsColonSplit.get(2));
                List<String> exonEndPartSplit = Arrays.asList(keyElementsColonSplit.get(3).split("-"));
                exonEnd = Integer.parseInt(exonEndPartSplit.get(0));
            }
        }

//        splicingEventsTableView.getItems().add(new SplicingEventsTableModel(
//        spliceEventKey, geneSymbol, strand, eventType, exonStart, exonEnd, deltaPsi, pVal,hasPeptideEvidence
//        ));

        this.exonStart = exonStart;
        this.exonEnd = exonEnd;
        this.strand = strand;
    }

    public Double getGeneRatioDiff() {
        return geneRatioDiff;
    }
}

