package TablesModels;

import Cds.Exon;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Group;
import javafx.util.Pair;
import utilities.BioFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BamFile extends BioFile {

    private int[] depth;
    private HashMap<Pair<Exon, Exon>, Integer> junctions;
    private HashMap<Pair<Exon, Exon>, Group> junctionsGraphicGroup;


    public BamFile(String path, String condition, String sample) {
        super(path, condition, sample);
        selected=new SimpleBooleanProperty();
        selected.setValue(true);
    }

    public BamFile(String path, String condition) {
        super(path, condition);
        selected=new SimpleBooleanProperty();
        selected.setValue(true);
    }





    public int[] getDepth() {
        return depth;
    }

    public void setDepth(int[] depth) {
        this.depth = depth;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + this.path.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object v) {
        boolean retVal = false;

        if (v instanceof BamFile){
            BamFile ptr = (BamFile) v;
            retVal = ptr.getPath().equals(path);
        }

        return retVal;
    }

    public HashMap<Pair<Exon, Exon>, Integer> getJunctions() {
        return junctions;
    }

    public void setJunctions(HashMap<Pair<Exon, Exon>, Integer> junctions) {
        this.junctions = junctions;
    }

    public HashMap<Pair<Exon, Exon>, Group> getJunctionsGraphicGroup() {
        return junctionsGraphicGroup;
    }

    public void setJunctionsGraphicGroup(HashMap<Pair<Exon, Exon>, Group> junctionsGraphicGroup) {
        this.junctionsGraphicGroup = junctionsGraphicGroup;
    }

    public Group getGroupForJunction(Pair<Exon, Exon> junction){
        for(Map.Entry<Pair<Exon, Exon>, Group> junctionToCompare: junctionsGraphicGroup.entrySet()){
            if((junctionToCompare.getKey().getKey().equals(junction.getKey()) && junctionToCompare.getKey().getValue().equals(junction.getValue())) ||
                    (junctionToCompare.getKey().getKey().equals(junction.getValue()) && junctionToCompare.getKey().getValue().equals(junction.getKey()))){
                return junctionToCompare.getValue();
            }
        }
        return null;
    }
}
