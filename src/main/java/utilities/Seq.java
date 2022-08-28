package utilities;

import java.util.HashMap;

public class Seq {

    private static HashMap<String, String> translationTable;

    static {
        translationTable = new HashMap();
        translationTable.put("ATA","I");
        translationTable.put("ATC","I");
        translationTable.put("ATT","I");
        translationTable.put("ATG","M");
        translationTable.put("ACA","T");
        translationTable.put("ACC","T");
        translationTable.put("ACG","T");
        translationTable.put("ACT","T");
        translationTable.put("AAC","N");
        translationTable.put("AAT","N");
        translationTable.put("AAA","K");
        translationTable.put("AAG","K");
        translationTable.put("AGC","S");
        translationTable.put("AGT","S");
        translationTable.put("AGA","R");
        translationTable.put("AGG","R");
        translationTable.put("CTA","L");
        translationTable.put("CTC","L");
        translationTable.put("CTG","L");
        translationTable.put("CTT","L");
        translationTable.put("CCA","P");
        translationTable.put("CCC","P");
        translationTable.put("CCG","P");
        translationTable.put("CCT","P");
        translationTable.put("CAC","H");
        translationTable.put("CAT","H");
        translationTable.put("CAA","Q");
        translationTable.put("CAG","Q");
        translationTable.put("CGA","R");
        translationTable.put("CGC","R");
        translationTable.put("CGG","R");
        translationTable.put("CGT","R");
        translationTable.put("GTA","V");
        translationTable.put("GTC","V");
        translationTable.put("GTG","V");
        translationTable.put("GTT","V");
        translationTable.put("GCA","A");
        translationTable.put("GCC","A");
        translationTable.put("GCG","A");
        translationTable.put("GCT","A");
        translationTable.put("GAC","D");
        translationTable.put("GAT","D");
        translationTable.put("GAA","E");
        translationTable.put("GAG","E");
        translationTable.put("GGA","G");
        translationTable.put("GGC","G");
        translationTable.put("GGG","G");
        translationTable.put("GGT","G");
        translationTable.put("TCA","S");
        translationTable.put("TCC","S");
        translationTable.put("TCG","S");
        translationTable.put("TCT","S");
        translationTable.put("TTC","F");
        translationTable.put("TTT","F");
        translationTable.put("TTA","L");
        translationTable.put("TTG","L");
        translationTable.put("TAC","Y");
        translationTable.put("TAT","Y");
        translationTable.put("TAA","*");
        translationTable.put("TAG","*");
        translationTable.put("TGC","C");
        translationTable.put("TGT","C");
        translationTable.put("TGA","*");
        translationTable.put("TGG","W");
    }

    public static String translateSequence(String seq){
        StringBuilder aaSeq = new StringBuilder();
        for(int i = 0; i < seq.length(); i+= 3){
            if (i+3 > seq.length()){
                break;
            }
            String codon = seq.substring(i, i+3);
            aaSeq.append(translationTable.get(codon));

            if (translationTable.get(codon).equals("*")){
                break;
            }
        }

        return aaSeq.toString();
    }

    public static String getReverseComplementRna(String rnaSeq){
        StringBuilder output  = new StringBuilder();
        for (int i = rnaSeq.length() -1 ; i >= 0; i --){
            String nuclUpper = String.valueOf(rnaSeq.charAt(i)).toUpperCase();
            boolean isUpperCase = Character.isUpperCase(rnaSeq.charAt(i));

            switch (nuclUpper) {
                case "G":
                    output.append((isUpperCase) ? "C" : "c");
                    break;
                case "C":
                    output.append((isUpperCase) ? "G" : "g");
                    break;
                case "A":
                    output.append((isUpperCase) ? "T" : "t");
                    break;
                case "T":
                    output.append((isUpperCase) ? "A" : "a");
                    break;
            }
        }
        return output.toString();
    }

}
