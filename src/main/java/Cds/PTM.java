package Cds;

import javafx.scene.paint.Color;

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
}
