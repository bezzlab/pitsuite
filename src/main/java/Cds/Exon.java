package Cds;

public class Exon {
    private int start;
    private int end;


    public Exon(int start, int end) {
        this.start = start;
        this.end = end;

    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + this.start + this.end;
        return hash;
    }

    @Override
    public boolean equals(Object v) {
        boolean retVal = false;

        if (v instanceof Exon){
            Exon ptr = (Exon) v;
            retVal = ptr.getStart()==start && ptr.getEnd() == end;
        }

        return retVal;
    }


}
