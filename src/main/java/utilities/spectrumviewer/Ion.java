package utilities.spectrumviewer;

// $LastChangedDate$
// $LastChangedBy$
// $LastChangedRevision$

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Ion{

    String type;
    String color;
    int charge;
    String label;
    String term;
    int fragmentIndex;
    boolean match;
    protected double mz;

    // charge +1

    static Ion A_1 = new Ion("a", "#008000", 1, "n"); // green
    static Ion B_1 = new Ion("b", "#0000ff", 1, "n"); // blue
    static Ion C_1 = new Ion("c", "#008B8B", 1, "n"); // dark cyan
    static Ion X_1 = new Ion("x", "#4B0082", 1, "c"); // indigo
    static Ion Y_1 = new Ion("y", "#ff0000", 1, "c"); // red
    static Ion Z_1 = new Ion("z", "#FF8C00", 1, "c"); // dark orange



// charge +2
    static Ion A_2 = new Ion("a", "#2E8B57", 2, "n"); // sea green
    static Ion B_2 = new Ion("b", "#4169E1", 2, "n"); // royal blue
    static Ion C_2 = new Ion("c", "#20B2AA", 2, "n"); // light sea green
    static Ion X_2 = new Ion("x", "#800080", 2, "c"); // purple
    static Ion Y_2 = new Ion("y", "#FA8072", 2, "c"); // salmon
    static Ion Z_2 = new Ion("z", "#FFA500", 2, "c"); // orange

// charge +3
    static Ion A_3 = new Ion("a", "#9ACD32", 3, "n"); // yellow green
    static Ion B_3 = new Ion("b", "#00BFFF", 3, "n"); // deep sky blue
    static Ion C_3 = new Ion("c", "#66CDAA", 3, "n"); // medium aquamarine
    static Ion X_3 = new Ion("x", "#9932CC", 3, "c"); // dark orchid
    static Ion Y_3 = new Ion("y", "#FFA07A", 3, "c"); // light salmon
    static Ion Z_3 = new Ion("z", "#FFD700", 3, "c"); // gold

    //-----------------------------------------------------------------------------
// Ion Series
//-----------------------------------------------------------------------------
    static double MASS_H_1 = 1.00782503207;  	 // H(1)  Source: http://en.wikipedia.org/wiki/Isotopes_of_hydrogen
    static double MASS_C_12 = 12.0;            // C(12) Source: http://en.wikipedia.org/wiki/Isotopes_of_carbon
    static double MASS_C_13 = 13.0033548378;   // C(13) Source: http://en.wikipedia.org/wiki/Isotopes_of_carbon
    static double MASS_N_14 = 14.0030740048;   // N(14) Source: http://en.wikipedia.org/wiki/Isotopes_of_nitrogen
    static double MASS_N_15 = 15.0001088982;   // N(15) Source: http://en.wikipedia.org/wiki/Isotopes_of_nitrogen
    static double MASS_O_16 = 15.99491461956;  // O(16) Source: http://en.wikipedia.org/wiki/Isotopes_of_oxygen
    static double MASS_O_18 = 17.9991610;      // O(18) Source: http://en.wikipedia.org/wiki/Isotopes_of_oxygen
    static double MASS_P_31 = 30.97376163;     // P(31) Source: http://en.wikipedia.org/wiki/Isotopes_of_phosphorus

    // average masses
    static double MASS_H = 1.00794; 	 // Source: http://www.unimod.org/masses.html
    static double MASS_C = 12.0107;    // Source: http://en.wikipedia.org/wiki/Isotopes_of_carbon
    static double MASS_N = 14.0067;	 // Source: http://en.wikipedia.org/wiki/Isotopes_of_nitrogen
    static double MASS_O = 15.9994;	 // Source: http://en.wikipedia.org/wiki/Isotopes_of_oxygen
    static double MASS_P = 30.9738;	 // Source: http://en.wikipedia.org/wiki/Isotopes_of_phosphorus

    public static double AmmoniaLossMass_mono = MASS_H_1 * 3 + MASS_N_14;
    public static double AmmoniaLossMass_avg = MASS_H * 3 + MASS_N;

    public static double WaterLossMass_mono = MASS_H_1 * 2 + MASS_O_16;
    public static double WaterLossMass_avg = MASS_H * 2 + MASS_O;

    public static double PhosphoLossMass_mono = MASS_H_1 * 3 + MASS_P_31 + MASS_O_16 * 4;
    public static double PhosphoLossMass_avg = MASS_H * 3 + MASS_P + MASS_O * 4;

    static double MASS_PROTON = 1.007276;
    private static final Map<String, HashMap<Integer, Ion>> _ions ;

    static {

        Map<String, HashMap<Integer, Ion>> map = new HashMap<>();
        map.put("a", new HashMap<>());
        map.put("b", new HashMap<>());
        map.put("c", new HashMap<>());
        map.put("x", new HashMap<>());
        map.put("y", new HashMap<>());
        map.put("z", new HashMap<>());

        map.get("a").put(1, A_1);
        map.get("a").put(2, A_2);
        map.get("a").put(3, B_3);
        map.get("b").put(1, B_1);
        map.get("b").put(2, B_2);
        map.get("b").put(3, C_3);
        map.get("c").put(1, C_1);
        map.get("c").put(2, C_2);
        map.get("c").put(3, A_3);
        map.get("x").put(1, X_1);
        map.get("x").put(2, X_2);
        map.get("x").put(3, X_3);
        map.get("y").put(1, Y_1);
        map.get("y").put(2, Y_2);
        map.get("y").put(3, Y_3);
        map.get("z").put(1, Z_1);
        map.get("z").put(2, Z_2);
        map.get("z").put(3, Z_3);

        _ions = Collections.unmodifiableMap(map);
    }


    //_ions.put("a", new HashMap);

    public Ion(){

    }


    public Ion(String t, String color, int charge, String terminus){
        this.type = t;
        this.color = color;
        this.charge = charge;
        this.label = this.type;
        if(this.charge > 1)
            this.label += charge;
        this.label += "+";
        this.term = terminus;





    }

    public boolean isMatch() {
        return match;
    }

    public void setMatch(boolean match) {
        this.match = match;
    }

    public String getLabel() {
        return label;
    }

    public String getColor() {
        return color;
    }

    public Ion get(String type, int charge){
        return _ions.get(type).get(charge);
    }

    public static String getSeriesColor(Ion ion){

        return _ions.get(ion.type).get(ion.charge).color;
    }

    public String getType() {
        return type;
    }

    public int getCharge() {
        return charge;
    }

    public double getMz(){ return mz;}

    public String getTerm() {
        return term;
    }

    public int getFragmentIndex() {
        return fragmentIndex;
    }

    public static double getMz(double neutralMass, int charge) {
        return ( neutralMass + (charge * MASS_PROTON) ) / charge;
    }

    public String makeIonLabel(String type, int index, int charge) {
        var label = type+""+index;
        for(var i = 1; i <= charge; i+=1)
            label += "+";
        return label;
    }

    // massType can be "mono" or "avg"
    public static Ion getSeriesIon(Ion ion, Peptide peptide, int idxInSeq, String massType) {
        if(ion.type.equals("a"))
            return new Ion_A (peptide, idxInSeq, ion.charge, massType);
        if(ion.type.equals("b"))
            return new Ion_B (peptide, idxInSeq, ion.charge, massType);
        if(ion.type.equals("c"))
            return new Ion_C (peptide, idxInSeq, ion.charge, massType);
        if(ion.type.equals("x"))
            return new Ion_X (peptide, idxInSeq, ion.charge, massType);
        if(ion.type.equals("y"))
            return new Ion_Y (peptide, idxInSeq, ion.charge, massType);
        if(ion.type.equals("z"))
            return new Ion_Z (peptide, idxInSeq, ion.charge, massType);
        return null;
    }

    public static double getIonMzWithLoss(Ion sion, Peptide.LossCombination neutralLosses, String massType) {
        var neutralMass = (sion.mz * sion.charge) - (sion.charge * MASS_PROTON);
        var lossMass = 0;
        if(neutralLosses!=null)
        {
            if(massType.equals("mono")) lossMass += neutralLosses.getTotalLossMass("mono");
            else if(massType.equals("avg")) lossMass += neutralLosses.getTotalLossMass("avg");
        }
        return getMz((neutralMass - lossMass), sion.charge);
    }


    static class Ion_A extends Ion{

        int charge;

        Ion_A(Peptide peptide, int endIdxPlusOne, int charge, String massType) {
            // Neutral mass:  	 [N]+[M]-CHO  ; N = mass of neutral N terminal group
            double mass = 0;
            if(massType.equals("mono"))
                mass = peptide.getSeqMassMono(endIdxPlusOne, "n") - (MASS_C_12 + MASS_O_16);
            else if(massType.equals("avg"))
                mass = peptide.getSeqMassAvg(endIdxPlusOne, "n") - (MASS_C + MASS_O);
            this.charge = charge;
            this.mz = getMz(mass, charge);
            this.fragmentIndex = endIdxPlusOne;
            this.label = makeIonLabel("a",this.fragmentIndex, charge);
            this.match = false;
            this.term = "n";
            this.type = "a";

        }
    }

    static class Ion_B extends Ion {


        Ion_B(Peptide peptide, int endIdxPlusOne, int charge, String massType) {
            // Neutral mass:    [N]+[M]-H  ; N = mass of neutral N terminal group
            double mass = 0;
            if (massType.equals("mono"))
                mass = peptide.getSeqMassMono(endIdxPlusOne, "n");
            else if (massType.equals("avg"))
                mass = peptide.getSeqMassAvg(endIdxPlusOne, "n");
            this.charge = charge;
            this.mz = getMz(mass, charge);
            this.fragmentIndex = endIdxPlusOne;
            this.label = makeIonLabel("b", this.fragmentIndex, charge);
            this.match = false;
            this.term = "n";
            this.type = "b";
        }
    }

    static class Ion_C extends Ion {

        Ion_C(Peptide peptide, int endIdxPlusOne, int  charge, String massType) {
            // Neutral mass:    [N]+[M]+NH2  ; N = mass of neutral N terminal group
            double mass = 0;
            if (massType.equals("mono"))
                mass = peptide.getSeqMassMono(endIdxPlusOne, "n") + MASS_H_1 + (MASS_N_14 + 2 * MASS_H_1);
            else if (massType.equals("avg"))
                mass = peptide.getSeqMassAvg(endIdxPlusOne, "n") + MASS_H + (MASS_N + 2 * MASS_H);
            this.charge = charge;
            this.mz = getMz(mass, charge);
            this.fragmentIndex = endIdxPlusOne;
            this.label = makeIonLabel("c", this.fragmentIndex, charge);
            this.match = false;
            this.term = "n";
            this.type = "c";
        }
    }

    static class Ion_X extends Ion{

        Ion_X(Peptide peptide, int startIdx, int charge, String massType) {
            // Neutral mass = [C]+[M]+CO-H ; C = mass of neutral C-terminal group (OH)
            double mass = 0;
            if(massType.equals("mono"))
                mass = peptide.getSeqMassMono(startIdx, "c") + 2*MASS_O_16 + MASS_C_12;
            else if(massType.equals("avg"))
                mass = peptide.getSeqMassAvg(startIdx, "c") + 2*MASS_O + MASS_C;
            this.charge = charge;
            this.mz = getMz(mass, charge);
            this.fragmentIndex = peptide.getSequence().length() - startIdx;
            this.label = makeIonLabel("x", this.fragmentIndex, charge);
            this.match = false;
            this.term = "c";
            this.type = "x";
        }
    }

    static class Ion_Y extends Ion {

        Ion_Y(Peptide peptide, int startIdx, int charge, String massType) {
            // Neutral mass = [C]+[M]+H ; C = mass of neutral C-terminal group (OH)
            double mass = 0;
            if (massType.equals("mono"))
                mass = peptide.getSeqMassMono(startIdx, "c") + 2 * MASS_H_1 + MASS_O_16;
            else if (massType.equals("avg"))
                mass = peptide.getSeqMassAvg(startIdx, "c") + 2 * MASS_H + MASS_O;
            this.charge = charge;
            this.mz = getMz(mass, charge);
            this.fragmentIndex = peptide.getSequence().length() - startIdx;
            this.label = makeIonLabel("y", this.fragmentIndex, charge);
            this.match = false;
            this.term = "c";
            this.type = "y";
        }
    }

    static class Ion_Z extends Ion {

        Ion_Z(Peptide peptide, int startIdx, int charge, String massType) {
            // Neutral mass = [C]+[M]-NH2 ; C = mass of neutral C-terminal group (OH)
            // We're really printing Z-dot ions so we add an H to make it OH+[M]-NH2 +H = [M]+O-N
            double mass = 0;
            if (massType.equals("mono"))
                mass = peptide.getSeqMassMono(startIdx, "c") + MASS_O_16 - MASS_N_14;
            else if (massType.equals("avg"))
                mass = peptide.getSeqMassAvg(startIdx, "c") + MASS_O - MASS_N;
            this.charge = charge;
            this.mz = getMz(mass, charge);
            this.fragmentIndex = peptide.getSequence().length() - startIdx;
            this.label = makeIonLabel("z", this.fragmentIndex, charge);
            this.match = false;
            this.term = "c";
            this.type = "z";
        }
    }


}



















