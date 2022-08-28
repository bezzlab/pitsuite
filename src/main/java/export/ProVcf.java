package export;

import Cds.CDS;
import Cds.Transcript;
import TablesModels.Variation;
import javafx.collections.ObservableList;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.dizitart.no2.Document;
import org.dizitart.no2.Filter;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.filters.Filters;
import fun.mike.dmp.Diff;
import fun.mike.dmp.DiffMatchPatch;
import org.json.simple.JSONObject;
import utilities.Seq;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.util.*;

public class ProVcf {


    //String path, ObservableList<Variation> variations, Nitrite db
    public static void generate(String path, ObservableList<Variation> variations, Nitrite db){


        DiffMatchPatch dmp = new DiffMatchPatch();

        HashMap<String, CDS> allCds = new HashMap<>();

        System.out.println("length "+variations.size());

        StringBuilder sb;

        try {
            PrintWriter writer = new PrintWriter("/media/esteban/data/outputVariationPeptide2/provcf/variants.provcf", StandardCharsets.UTF_8);
            for(Variation variation: variations){

                HashSet<ProVCFRecord> records = new HashSet<>();

                if(!variation.isSilent()){

                    for(String transcriptID: variation.getTranscriptIds()){
                        Document transcriptDoc = db.getCollection("allTranscripts").find(Filters.eq("transcriptID", transcriptID)).firstOrDefault();
                        Transcript transcript = new Transcript(transcriptDoc, allCds);
                        for(CDS cds: transcript.getCdss()){
                            if(cds.getUniprotId()!=null){
                                int varPos = variation.getPositionInTranscript(transcript.getTranscriptId());
                                Pair<Integer, Integer> cdsPos = cds.getTranscriptWithCdsPos(transcript);
                                if(varPos>=cdsPos.getKey() && varPos<=cdsPos.getValue()){
                                    ProVCFRecord record = new ProVCFRecord(cds.getUniprotId(), variation.getPositionInCds(cds, transcript),
                                            variation.getRefAA(), variation.getRefAA());
                                    if(!records.contains(record)){

                                        JSONObject transcriptObj = variation.getTranscriptObj(transcriptID);
                                        String ref = (String) transcriptObj.get("aaRef");
                                        String alt = (String) transcriptObj.get("aaAlt");

                                        sb = new StringBuilder();

                                        sb.append(cds.getUniprotId()).append("\t").append(variation.getPositionInCds(cds, transcript)).append("\t")
                                                .append(".\t").append(ref).append("\t").append(alt).append("\t");

                                        if(transcriptObj.containsKey("peptides")){
                                            sb.append("\tPASS");
                                        }else {
                                            sb.append("\tTRANS");
                                        }

                                        sb.append("\tCDSId=").append(cds.getId()).append(";TYPE=");
                                        if(ref.length()==1 && alt.length()==1){
                                            sb.append("SAP;");
                                        }else if(ref.length()==0){
                                            sb.append("INS;");
                                        }else if(alt.length()==0){
                                            sb.append("DEL;");
                                        }else{
                                            sb.append("ALT;");
                                        }

                                        sb.append("PeptideCount=0;");
                                        if(transcriptObj.containsKey("peptides")){
                                            String peptides = (String) transcriptObj.get("peptides");
                                            sb.append("UniquePeptideCount=").append(peptides.split(";").length+1)
                                                    .append(";Peptides=").append(peptides).append(";Score=")
                                                    .append((String) transcriptObj.get("peptideProb")).append("\n");
                                        }else {
                                            sb.append("UniquePeptideCount=0;Peptides=-;Score=0\n");
                                        }
                                        records.add(record);
                                        writer.write(sb.toString());
                                    }
                                }
                            }

                        }
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }






    private static String getAlternativeCDS(CDS cds, Transcript transcript, Variation variation){

        String rnaSeq;
        if (cds.getStrand().equals("+")) {
            rnaSeq = cds.getRnaSeq();
        }else{
            rnaSeq = Seq.getReverseComplementRna(cds.getRnaSeq());
        }

        char[] charArray = rnaSeq.toCharArray();
        ArrayList<String> charList = new ArrayList<>(Arrays.asList(rnaSeq.split("")));


        int pos = variation.getPositionInTranscript(transcript.getTranscriptId())
                -cds.getTranscriptsWithCdsPos().get(transcript).getKey()+1;

        if(cds.getStrand().equals("+")) {
            if (variation.getRef().length() == 0) {
                for (int i = variation.getAlt().length() - 1; i >= 0; i--) {
                    charList.add(pos, String.valueOf(variation.getAlt().charAt(i)));
                }

            } else if (variation.getAlt().length() == 0) {
                for (int i = 0; i < variation.getRef().length(); i++) {
                    charList.remove(pos);
                }
            } else {

                for (int i = 0; i < variation.getRef().length(); i++) {
                    charList.remove(pos);
                }
                for (int i = 0; i < variation.getAlt().length(); i++) {
                    charList.add(pos, String.valueOf(variation.getAlt().charAt(i)));
                }

            }
        }else{
            if (variation.getRef().length() == 0) {
                for (int i = variation.getAlt().length() - 1; i >= 0; i--) {
                    charList.add(rnaSeq.length()-pos-1,
                            Seq.getReverseComplementRna(String.valueOf(variation.getAlt().charAt(i))));
                }

            } else if (variation.getAlt().length() == 0) {
                for (int i = 0; i < variation.getRef().length(); i++) {
                    charList.remove(rnaSeq.length()-pos-1);
                }
            } else {

                for (int i = 0; i < variation.getRef().length(); i++) {
                    charList.remove(rnaSeq.length()-pos-1);
                }
                for (int i = 0; i < variation.getAlt().length(); i++) {
                    charList.add(rnaSeq.length()-pos-1,
                            Seq.getReverseComplementRna(String.valueOf(variation.getAlt().charAt(i))));
                }

            }
        }





        StringBuilder sb = new StringBuilder();
        for(String nc: charList){
            sb.append(nc);
        }

        String newAASeq = Seq.translateSequence(sb.toString());

        if(!newAASeq.endsWith("*")){
            return extendSequence(cds, variation, transcript);
        }else {
            return newAASeq;
        }




    }

    public static String extendSequence(CDS cds, Variation variation, Transcript transcript){


        String rna;

        Pair<Integer, Integer> cdsPos = cds.getTranscriptWithCdsPos(transcript);
        if(cds.getStrand().equals("+")){
            rna = transcript.getSequence(cdsPos.getKey()-1);
        }else{
            rna = Seq.getReverseComplementRna(transcript.getSequence(0, cdsPos.getValue()));
        }


        ArrayList<String> charList = new ArrayList<>(Arrays.asList(rna.split("")));


        int pos = variation.getPositionInTranscript(transcript.getTranscriptId())
                -cds.getTranscriptsWithCdsPos().get(transcript).getKey()+1;

        if(cds.getStrand().equals("+")) {
            if (variation.getRef().length() == 0) {
                for (int i = variation.getAlt().length() - 1; i >= 0; i--) {
                    charList.add(pos, String.valueOf(variation.getAlt().charAt(i)));
                }

            } else if (variation.getAlt().length() == 0) {
                for (int i = 0; i < variation.getRef().length(); i++) {
                    charList.remove(pos);
                }
            } else {

                for (int i = 0; i < variation.getRef().length(); i++) {
                    charList.remove(pos);
                }
                for (int i = 0; i < variation.getAlt().length(); i++) {
                    charList.add(pos, String.valueOf(variation.getAlt().charAt(i)));
                }

            }
        }else{
            if (variation.getRef().length() == 0) {
                for (int i = variation.getAlt().length() - 1; i >= 0; i--) {
                    charList.add(rna.length()-pos-1,
                            Seq.getReverseComplementRna(String.valueOf(variation.getAlt().charAt(i))));
                }

            } else if (variation.getAlt().length() == 0) {
                for (int i = 0; i < variation.getRef().length(); i++) {
                    charList.remove(rna.length()-pos-1);
                }
            } else {

                for (int i = 0; i < variation.getRef().length(); i++) {
                    charList.remove(rna.length()-pos-1);
                }
                for (int i = 0; i < variation.getAlt().length(); i++) {
                    charList.add(rna.length()-pos-1,
                            Seq.getReverseComplementRna(String.valueOf(variation.getAlt().charAt(i))));
                }

            }
        }



        StringBuilder sb = new StringBuilder();
        for(String nc: charList){
            sb.append(nc);
        }

        return Seq.translateSequence(sb.toString());

    }

    public static class ProVCFRecord{
        private String chrom;
        private int pos;
        private String ref;
        private String alt;

        public ProVCFRecord(String chrom, int pos, String ref, String alt) {
            this.chrom = chrom;
            this.pos = pos;
            this.ref = ref;
            this.alt = alt;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ProVCFRecord)) return false;
            ProVCFRecord that = (ProVCFRecord) o;
            return pos == that.pos &&
                    chrom.equals(that.chrom) &&
                    ref.equals(that.ref) &&
                    alt.equals(that.alt);
        }

        @Override
        public int hashCode() {
            return Objects.hash(chrom, pos, ref, alt);
        }
    }

}
