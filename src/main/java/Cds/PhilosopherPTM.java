package Cds;

import javafx.scene.paint.Color;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhilosopherPTM extends PTM{

    public PhilosopherPTM(String residue, double massShift, boolean assigned) {
        super(residue, massShift, assigned);
    }

    public PhilosopherPTM(String residue, int pos, double massShift, boolean assigned) {
        super(residue, pos, massShift, assigned);
    }



    public static HashSet<PTM> parsePtms(String modsStr){
        HashSet<PTM> ptms = new HashSet<>();
        if(modsStr.length()>0){


            Pattern pattern = Pattern.compile("(.*)\\((.*)\\)");
            Pattern residuePattern = Pattern.compile("(\\d+)([A-Z]+)");
            String[] modsSplit = modsStr.split(", ");

            for(String mod: modsSplit){
                if(mod.length()>0 && !mod.equals("none") && !mod.equals("Unmodified")){
                    Matcher matcher = pattern.matcher(mod);
                    matcher.matches();
                    Matcher residueMatcher = residuePattern.matcher(matcher.group(1));
                    if(residueMatcher.matches()){
                        ptms.add(new PhilosopherPTM(residueMatcher.group(2), Integer.parseInt(residueMatcher.group(1)),
                                Double.parseDouble(matcher.group(2)), residueMatcher.matches()));
                    }else{
                        ptms.add(new PhilosopherPTM(matcher.group(1), Double.parseDouble(matcher.group(2)), false));
                    }
                }


            }
        }else{
            ptms.add(new PhilosopherPTM("None", -1, true));
        }

        return ptms;
    }

    @Override
    public String getShape() {
        return null;
    }

    @Override
    public Color getColor() {
        return null;
    }
}
