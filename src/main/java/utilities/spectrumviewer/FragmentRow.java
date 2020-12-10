package utilities.spectrumviewer;

import java.util.HashMap;

public class FragmentRow {

    HashMap<String, Ion> ions = new HashMap<>();
    char aa;

    public FragmentRow(){

    }

    public void put(String label, Ion ion){
        ions.put(label, ion);
    }

    public void setAa(char aa){
        this.aa = aa;
    }

    public Double getMass(String ion){

        if(!ions.containsKey(ion) || ions.get(ion)==null){
            return 0.;
        }



        return ions.get(ion).getMz();
    }

    public String getAA(){
        return String.valueOf(aa);
    }
}
