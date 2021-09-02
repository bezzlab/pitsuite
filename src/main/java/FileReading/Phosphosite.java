package FileReading;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Phosphosite {

    String residue;
    String gene;
    String pos;
    double fc;
    double pval;


    public Phosphosite(String id, String fc, String pval) {

        Pattern pattern = Pattern.compile("(.*)\\(([STY])(\\d+)");
        Matcher matcher = pattern.matcher(id);
        if(matcher.find()) {
            gene = matcher.group(1).replace("\"", "");
            residue = matcher.group(2);
            pos = matcher.group(3);
            this.fc = Double.parseDouble(fc);
            if(!pval.equals("NA"))
                this.pval=Double.parseDouble(pval);
            else
                this.pval=Double.NaN;
        }
    }

    public String getResidue() {
        return residue;
    }

    public String getGene() {
        return gene;
    }

    public String getPos() {
        return pos;
    }

    public double getFc() {
        return fc;
    }

    public double getPval() {
        return pval;
    }

    public boolean matchesElement(String name){
        Pattern pattern = Pattern.compile("p-([A-Z])(\\d+)(?:,([A-Z])(\\d+))*-([A-Z0-9]*)");
        Matcher matcher = pattern.matcher(name);

        if(matcher.find()) {


            if(!matcher.group(matcher.groupCount()).equals(gene))
                return false;

            for (int i = 1; i < matcher.groupCount()-1; i+=2) {
                if(matcher.group(i)!=null && matcher.group(i).equals(residue) && matcher.group(i+1).equals(pos)) {
                    return true;
                }
            }
            return false;
        }else
            return false;
    }
    
    public static int getPhosphositesNumberInElement(String label){
        Pattern pattern = Pattern.compile("p-([A-Z])(\\d+)(?:,([A-Z])(\\d+))*-(.*)");
        Matcher matcher = pattern.matcher(label);

        if(matcher.find()) {
            int count = 0;
            for (int i = 1; i < matcher.groupCount()-1; i+=2) {
                if(matcher.group(i)!=null )
                    count++;
            }
            return count;
            
        }else
            return 0;
    }
}
