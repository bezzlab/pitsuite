package utilities.spectrumviewer;

import java.util.ArrayList;

import static java.lang.Integer.parseInt;

public class Modification {

    AminoAcid aa;
    ArrayList<Peptide.NeutralLoss> losses = new ArrayList<>();
    int position;
    double mass;

    Modification(AminoAcid aminoAcid, double mass, ArrayList<Peptide.NeutralLoss> losses){
        this.aa=aminoAcid;
        this.mass=mass;
        this.losses=losses;
    }

    Modification(String pos, double mass, AminoAcid aminoAcid, ArrayList<Peptide.NeutralLoss> losses){
        this.position=parseInt(pos);
        this.aa=aminoAcid;
        this.mass=mass;
        this.losses=losses;
    }

    public AminoAcid getAa() {
        return aa;
    }

    public ArrayList<Peptide.NeutralLoss> getLosses() {
        return losses;
    }

    public int getPosition() {
        return position;
    }

    public double getMass() {
        return mass;
    }
}
