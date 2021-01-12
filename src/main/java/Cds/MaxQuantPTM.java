package Cds;

import javafx.scene.paint.Color;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MaxQuantPTM extends PTM{
    public MaxQuantPTM(String residue, double massShift, boolean assigned) {
        super(residue, massShift, assigned);
    }

    public MaxQuantPTM(String residue, int pos, double massShift, boolean assigned) {
        super(residue, pos, massShift, assigned);
    }


    public static HashSet<PTM> parsePtms(String modsStr){
        HashSet<PTM> ptms = new HashSet<>();


        Pattern pattern = Pattern.compile("[A-Z]*(\\(.*?\\)\\))[A-Z]*");
        Matcher matcher = pattern.matcher(modsStr);

        int charOffset = 1;
        if(matcher.find()){
            for (int i = 0; i < matcher.groupCount(); i++) {
                String mod = matcher.group(i+1);
                int pos = matcher.start(i+1) - charOffset;
                ptms.add(new MaxQuantPTM(mod, pos, 2, true));
                charOffset+=mod.length();
            }
        }

        return ptms;
    }

    public String getShape(){
        if(residue.contains("Oxidation (M)")){
            return "triangle";
        }else if(residue.contains("Acetyl (Protein N-term)")){
            return "triangle";
        }else{
            return "square";
        }
    }

    public Color getColor(){
        if(residue.contains("Oxidation (M)")){
            return Color.BLUE;
        }else if(residue.contains("Acetyl (Protein N-term)")){
            return Color.GREEN;
        }else{
            return Color.BLACK;
        }
    }

    public String getName(){
        if(residue.contains("Oxidation (M)")){
            return "Oxidation of M";
        }else if(residue.contains("Acetyl (Protein N-term)")){
            return "Acetylation of Protein N-terminal";
        }else{
            return "Unknown modification";
        }
    }
}