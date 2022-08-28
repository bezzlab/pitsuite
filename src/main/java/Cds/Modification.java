package Cds;

/**
 * Class that stores information about modifications
 * Position (1 indexed)
 * "16C(57.0215), 18C(57.0215), 3C(57.0215)"
 */
public class Modification {
    private int position;
    private String aminoacid;
    private double massShift;

    public Modification(int position, String aminoacid, double massShift) {
        this.position = position;
        this.aminoacid = aminoacid;
        this.massShift = massShift;
    }

    public int getPosition() {
        return position;
    }

    public String getAminoacid() {
        return aminoacid;
    }

    public double getMassShift() {
        return massShift;
    }
}
