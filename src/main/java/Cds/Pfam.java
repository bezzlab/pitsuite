package Cds;

/**
 * Pfam domains
 * Pfam website: https://pfam.xfam.org/
 *  this is how the Json looks like:
 * //    {
 * //        "start": 5,
 * //            "end": 76,
 * //            "desc": "Ubiquitin family"
 * //    },
 */
public class Pfam {

    private int aaStart;
    private int aaEnd;
    private String desc;
    private String id;

    public Pfam(int aaStart, int aaEnd, String desc, String id) {
        this.aaStart = aaStart;
        this.aaEnd = aaEnd;
        this.desc = desc;
        this.id = id;
    }

    public int getAaStart() {
        return aaStart;
    }

    public int getAaEnd() {
        return aaEnd;
    }

    public String getDesc() {
        return desc;
    }

    public String getId() {
        return id;
    }
}
