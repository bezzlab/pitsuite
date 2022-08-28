package TablesModels;

import java.util.ArrayList;
import java.util.List;

public class FoldChangeTableModel {
    private String geneSymbol;
    private String type;
    private double logFoldChange;
    private Double pVal;
    private boolean hasPeptideEvidence;
    private ArrayList<ProteinFoldChange> msRuns;

    public FoldChangeTableModel(String geneSymbol, String type,  double logFoldChange, Double pVal){
        this.geneSymbol = geneSymbol;
        this.type = type;
        this.logFoldChange = logFoldChange;
        this.pVal = pVal;
    }

    public FoldChangeTableModel(String geneSymbol,  double logFoldChange, Double pVal, boolean hasPeptideEvidence) {
        this.geneSymbol = geneSymbol;
        this.logFoldChange = logFoldChange;
        this.pVal = pVal;
        this.hasPeptideEvidence = hasPeptideEvidence;

    }

    public void addMsRun(String run, double fc, Double pVal){
        if(msRuns==null){
            msRuns = new ArrayList<>();
        }
        msRuns.add(new ProteinFoldChange(run, fc, pVal));
    }

    public String getGeneSymbol() {
        return geneSymbol;
    }

    public double getLogFoldChange() {
        return logFoldChange;
    }

    public Double getPVal() {
        return pVal;
    }

    public boolean isHasPeptideEvidence() {
        return hasPeptideEvidence;
    }

    public Double getpVal() {
        return pVal;
    }

    public Double getProteinFc(String runName) {
        if(msRuns==null) return null;
        ProteinFoldChange prot =  msRuns.stream().filter(run -> runName.equals(run.getRun())).findFirst().orElse(null);
        if(prot==null){
            return null;
        }
        return prot.getFoldChange();
    }

    public Double getProteinPval(String runName) {
        if(msRuns==null) return null;
        ProteinFoldChange prot = msRuns.stream().filter(run -> runName.equals(run.getRun())).findFirst().orElse(null);
        if(prot==null){
            return null;
        }
        return prot.getPvalue();
    }

    public String getType() {
        return type;
    }

    public class ProteinFoldChange{
        private final double foldChange;
        private final Double pvalue;
        private final String run;

        public ProteinFoldChange(String run, double foldChange, Double pvalue) {
            this.foldChange = foldChange;
            this.pvalue = pvalue;
            this.run = run;
        }

        public double getFoldChange() {
            return foldChange;
        }

        public Double getPvalue() {
            return pvalue;
        }

        public String getRun() {
            return run;
        }
    }
}
