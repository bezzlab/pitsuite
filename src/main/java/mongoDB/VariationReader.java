package mongoDB;

public class VariationReader {
    String ref, alt;
    int pos;
    double AF, qual;

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public double getAF() {
        return AF;
    }

    public void setAF(double AF) {
        this.AF = AF;
    }

    public double getQual() {
        return qual;
    }

    public void setQual(double qual) {
        this.qual = qual;
    }
}

