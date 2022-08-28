package FileReading;

import java.util.ArrayList;
import java.util.HashSet;

public class UniprotProtein {
    private String uniprotId;
    private String sequence;
    private String geneName;
    private HashSet<String> pdbIds;
    private String transcriptId;

    public UniprotProtein(String uniprotId, String sequence, String geneName, HashSet<String> pdbIds, String transcriptId) {
        this.uniprotId = uniprotId;
        this.sequence = sequence;
        this.geneName = geneName;
        this.pdbIds = pdbIds;
        this.transcriptId = transcriptId;
    }

    public String getUniprotId() {
        return uniprotId;
    }

    public String getSequence() {
        return sequence;
    }

    public String getGeneName() {
        return geneName;
    }

    public HashSet<String> getPdbIds() {
        return pdbIds;
    }

    public String getTranscriptId() {
        return transcriptId;
    }
}
