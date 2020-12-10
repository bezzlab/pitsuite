package utilities.spectrumviewer;


import java.util.HashMap;

class AminoAcid {

    String code;
    String shortName;
    String name;
    double mono;
    double avg;
    HashMap<String, AminoAcid> aminoAcids;
    public AminoAcid(String aaCode, String aaShortName, String aaName, double monoMass, double avgMass) {
        this.code = aaCode;
        this.shortName = aaShortName;
        this.name = aaName;
        this.mono = monoMass;
        this.avg = avgMass;

    }

    public AminoAcid(String aaCode, String aaShortName, double monoMass, double avgMass) {
        this.code = aaCode;
        this.shortName = aaShortName;
        this.mono = monoMass;
        this.avg = avgMass;

    }

    public double getMass(String type){
        return type.equals("mono")?mono:avg;
    }

    public AminoAcid(){
        aminoAcids = new HashMap<>();

        aminoAcids.put("A", new

                AminoAcid("A","Ala","Alanine",71.037113805,71.0779));

        aminoAcids.put("R",new

                AminoAcid("R","Arg","Arginine",156.101111050,156.18568));

        aminoAcids.put("N",new

                AminoAcid("N","Asn","Asparagine",114.042927470,114.10264));

        aminoAcids.put("D",new

                AminoAcid("D","Asp","Aspartic Acid",115.026943065,115.0874));

        aminoAcids.put("C",new

                AminoAcid("C","Cys","Cysteine",103.009184505,103.1429));

        aminoAcids.put("E",new

                AminoAcid("E","Glu","Glutamine",129.042593135,129.11398));

        aminoAcids.put("Q",new

                AminoAcid("Q","Gln","Glutamic Acid",128.058577540,128.12922));

        aminoAcids.put("G",new

                AminoAcid("G","Gly","Glycine",57.021463735,57.05132));

        aminoAcids.put("H",new

                AminoAcid("H","His","Histidine",137.058911875,137.13928));

        aminoAcids.put("I",new

                AminoAcid("I","Ile","Isoleucine",113.084064015,113.15764));

        aminoAcids.put("L",new

                AminoAcid("L","Leu","Leucine",113.084064015,113.15764));

        aminoAcids.put("K",new

                AminoAcid("K","Lys","Lysine",128.094963050,128.17228));

        aminoAcids.put("M",new

                AminoAcid("M","Met","Methionine",131.040484645,131.19606));

        aminoAcids.put("F",new

                AminoAcid("F","Phe","Phenylalanine",147.068413945,147.17386));

        aminoAcids.put("P",new

                AminoAcid("P","Pro","Proline",97.052763875,97.11518));

        aminoAcids.put("S",new

                AminoAcid("S","Ser","Serine",87.032028435,87.0773));

        aminoAcids.put("T",new

                AminoAcid("T","Thr","Threonine",101.047678505,101.10388));

        aminoAcids.put("W",new

                AminoAcid("W","Trp","Tryptophan",186.079312980,186.2099));

        aminoAcids.put("Y",new

                AminoAcid("Y","Tyr","Tyrosine",163.063328575,163.17326));

        aminoAcids.put("V",new

                AminoAcid("V","Val","Valine",99.068413945,99.13106));
    }

    public String getCode() {
        return code;
    }

    public AminoAcid getAA(String aaCode) {
        if (aminoAcids.containsKey(aaCode))
            return aminoAcids.get(aaCode);
        else
            return new AminoAcid(aaCode, aaCode, 0.0, 0.0);
    }
}