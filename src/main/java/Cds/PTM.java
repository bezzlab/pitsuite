package Cds;

import javafx.scene.paint.Color;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PTM {

    protected String residue;
    protected int pos;
    protected double massShift;
    protected boolean assigned;
    protected String mod;
    protected String gene;
    protected double log2fc;
    protected double pval;
    JSONObject intensities;
    private ArrayList<String> kinases;
    protected String type = "unknown";


    public PTM(){

    }

    public PTM(String residue, double massShift, boolean assigned) {
        this.residue = residue;
        this.massShift = massShift;
        this.assigned = assigned;
        pos = -1;
    }



    public PTM(String residue, int pos, double massShift, boolean assigned) {
        this.residue = residue;
        this.massShift = massShift;
        this.assigned = assigned;
        this.pos = pos;
    }

    public PTM(String residue, String mod) {
        this.mod = mod;
        this.residue = residue;
    }

    public PTM(String residue, int pos, String gene, double log2fc, double pval, JSONObject intensities, String type) {
        this.residue = residue;
        this.pos = pos;
        this.gene = gene;
        this.log2fc = log2fc;
        this.pval = pval;
        this.intensities = intensities;
        this.type = type;
    }

    public String getResidue() {
        return residue;
    }

    public double getMassShift() {
        return massShift;
    }

    public int getPos() {
        return pos;
    }

    @Override
    public String toString(){

        if(assigned){
            if (massShift == 57.0215) {
                return "Carboxyamidomethylation (57.0215)";
            } else if(massShift==15.9949){
                return "Oxidation of Met(15.9949)";
            }
        }

        return residue + "(" + massShift + ")";
    }

    public String getName(){
        if(assigned){
            if (massShift == 57.0215) {
                return "Carboxyamidomethylation";
            } else if(massShift==15.9949){
                return "Oxidation of Met";
            } else if(massShift==79.96633){
                return "Phosphorylation of "+ residue;
            }
        }

        return residue;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj.getClass() != this.getClass()) {
            return false;
        }

        final PTM other = (PTM) obj;
        return residue.equals(other.getResidue()) && massShift == other.getMassShift() && pos == other.getPos();

    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.residue != null ? this.residue.hashCode() : 0);
        hash = (int) (53 * hash + this.massShift);
        return hash;
    }

    public static HashSet<PTM> parsePtms(String modsStr){

        if(modsStr.length()==0){
            return PhilosopherPTM.parsePtms(modsStr);
        }else{
            if(modsStr.matches("(.+)\\((\\d+\\.\\d+)\\)")){
                return PhilosopherPTM.parsePtms(modsStr);
            }else{
                return MaxQuantPTM.parsePtms(modsStr);
            }
        }
    }

    public String getShape() {
        return null;
    }

    public Color getColor() {
        return null;
    }

    public String getMod() {
        return mod;
    }

    public String getType(){
        return type;
    }

    public String getGene() {
        return gene;
    }

    public boolean isAssigned() {
        return assigned;
    }

    public double getLog2fc() {
        return log2fc;
    }

    public double getPval() {
        return pval;
    }

    public JSONObject getIntensities() {
        return intensities;
    }

    public void addKinase(String kinase){
        if(kinases==null)
            kinases = new ArrayList<>();
        kinases.add(kinase);
    }

    public ArrayList<String> getKinases() {
        return kinases;
    }

    public String getId() {
        return gene+"("+residue+pos+")";
    }
}
