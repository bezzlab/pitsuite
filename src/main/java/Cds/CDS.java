package Cds;

import TablesModels.Variation;
import javafx.util.Pair;
import utilities.MSRun;

import java.util.*;

public class CDS {

    private String strand;
    private String sequence;
    private HashMap<Transcript, Pair<Integer, Integer>> transcripts;
    private ArrayList<Pfam> pfams;
    private ArrayList<Peptide> peptides;
    private ArrayList<Variation> variations;
    private String uniprotId;
    private String id;

    public CDS(String strand, String sequence, String id) {
        this.strand = strand;
        this.sequence = sequence;
        this.id = id;
        transcripts = new HashMap<>();
        peptides = new ArrayList<>();
        pfams = new ArrayList<>();
    }

    public void addTranscript(Transcript cdsTranscript, int startInTranscrit, int endInTranscript){
        transcripts.put(cdsTranscript, new Pair<>(startInTranscrit, endInTranscript));
    }

    public ArrayList<Pfam> getPfams() {
        return pfams;
    }

    public String getSequence() {
        return sequence;
    }

    public void setPfams(ArrayList<Pfam> pfams) {
        this.pfams = pfams;
    }

    public ArrayList<Peptide> getPeptides() {
        return peptides;
    }

    public void setPeptides(ArrayList<Peptide> peptides) {
        this.peptides = peptides;
    }

    public boolean containsTranscript(String transcriptID){
        for(Transcript transcript: transcripts.keySet()){
            if(transcript.getTranscriptId().equals(transcriptID)){
                return true;
            }
        }
        return false;
    }

    public String getRnaSeq(){
        Map.Entry<Transcript, Pair<Integer, Integer>> transcript = transcripts.entrySet().iterator().next();
        return transcript.getKey().getSequence(transcript.getValue().getKey()-1, transcript.getValue().getValue());
    }

    public Set<String> getTranscriptsIDs(){
        HashSet<String> ids = new HashSet<>();

        for(Transcript transcript: transcripts.keySet()){
            ids.add(transcript.getTranscriptId());
        }
        return ids;
    }

    public String getUniprotId() {
        return uniprotId;
    }

    public void setUniprotId(String uniprotId) {
        this.uniprotId = uniprotId;
    }

    public Set<Transcript> getTranscripts(){
        return transcripts.keySet();
    }

    public HashMap<Transcript, Pair<Integer, Integer>> getTranscriptsWithCdsPos(){
        return transcripts;
    }

    public Pair<Integer, Integer> getTranscriptWithCdsPos(Transcript transcript){
        return transcripts.get(transcript);
    }

    public void addVariation(Variation variation){
        variations.add(variation);
    }

    public String getStrand() {
        return strand;
    }

    public Pair<Integer, Integer> getGenomicPos(Transcript transcript){

        int start = transcript.getStartGenomCoord();
        int end = transcript.getStartGenomCoord();
        boolean startSet=false;

        int posOnTranscript = 1;
        int posOnGenome = transcript.getStartGenomCoord();
        int sequenceStart = transcripts.get(transcript).getKey();
        int sequenceEnd = transcripts.get(transcript).getValue();
        for(Exon exon: transcript.getExons()){
            posOnGenome = exon.getStart();
            if(posOnTranscript+exon.getEnd()-exon.getStart() + 1 > sequenceStart && !startSet){
                start = posOnGenome + sequenceStart-posOnTranscript  +1;
                startSet=true;

            } if(posOnTranscript+exon.getEnd()-exon.getStart() + 1 > sequenceEnd){
                end = posOnGenome + sequenceEnd-posOnTranscript  +1;
                break;
            }
            posOnTranscript += exon.getEnd()-exon.getStart()+1;

        }


        return new Pair<>(start, end-1);
    }

    public void addPeptide(String sequence, String mod, double probability, String run, String condition, String sample){
        for(Peptide peptide: peptides){
            if(peptide.getSequence().equals(sequence)){
                peptide.addPsm(new PSM(run, mod, probability, condition, sample), run);
                return;
            }
        }
        Peptide peptide = new Peptide(sequence, new MSRun(run));
        peptide.addPsm(new PSM(run, mod, probability, condition, sample), run);
        peptides.add(peptide);
    }

    public void addPeptide(String sequence, String mod, String run){
        for(Peptide peptide: peptides){
            if(peptide.getSequence().equals(sequence)){
                peptide.addPsm(new PSM(run, mod), run);
                return;
            }
        }
        Peptide peptide = new Peptide(sequence, new MSRun(run));
        peptide.addPsm(new PSM(run, mod), run);
        peptides.add(peptide);
    }

    public boolean hasPfam(){
        return pfams.size()>0;
    }

    public void addPfam(Pfam pfam){
        pfams.add(pfam);
    }

    public Pair<String, Integer[]> getSubStringWithOffset(Transcript transcript, double genomicStart, double genomicEnd){
        Pair<Integer, Integer> startEnd = transcripts.get(transcript);


        int transcriptStart = transcript.getStartGenomCoord();
        int transcriptEnd  = transcript.getStartGenomCoord();

        int startOffset = 0;

        boolean startFound=false;
        boolean endFound=false;

        for(Exon exon: transcript.getExons()){
            if(genomicStart>=exon.getStart() && genomicStart<=exon.getEnd()){
                transcriptStart += genomicStart-exon.getStart()+1;
                startFound=true;
                break;
            }else if(genomicStart<exon.getStart() && genomicEnd>exon.getStart()){
                transcriptStart += 1;
                startFound=true;
                startOffset = (int) (exon.getStart() - genomicStart - 1);
                break;
            }else{
                transcriptStart+=exon.getEnd()-exon.getStart()+1;
            }
        }



        for(Exon exon: transcript.getExons()){
            if(genomicEnd>=exon.getStart() && genomicEnd<=exon.getEnd()){
                transcriptEnd += genomicEnd-exon.getStart()+1;
                endFound=true;
                break;
            }else if(genomicEnd < exon.getStart()){
                break;
            }
            else{
                transcriptEnd+=exon.getEnd()-exon.getStart()+1;
            }
        }


        transcriptStart = transcriptStart - transcript.getStartGenomCoord();
        transcriptEnd = transcriptEnd - transcript.getStartGenomCoord();

        transcriptStart = transcriptStart-startEnd.getKey();
        transcriptEnd = transcriptEnd-startEnd.getKey();
        int rnaLength = transcriptEnd-transcriptStart;


        int cdsStart = transcriptStart / 3;
        int cdsEnd = (transcriptStart + rnaLength) / 3;


        if(startFound && cdsEnd > 0 & cdsStart < sequence.length()){
            String subseq;
            if(strand.equals("+")){
                subseq = sequence.substring(transcriptStart / 3, Math.min(sequence.length(),
                        (transcriptStart + rnaLength) / 3 + 1));
            }else{
                String revSeq = new StringBuilder(sequence).reverse().toString();
                subseq = revSeq.substring(transcriptStart / 3, Math.min((transcriptStart + rnaLength) / 3 + 1,
                        revSeq.length()));
            }


            int offset = transcriptStart % 3;
            if(offset==2){
                subseq = subseq.substring(1);
            } else if(offset==1){
                offset=0;
            } else if(offset == 0){
                offset = 1;
            }


            return new Pair<>(subseq, new Integer[]{offset, startOffset});
        }
        return null;

    }

    public String getId() {
        return id;
    }
}
