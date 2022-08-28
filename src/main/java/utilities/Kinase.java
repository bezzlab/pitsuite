package utilities;

import java.util.ArrayList;
import java.util.HashMap;

public class Kinase {
    private final double log2fc;
    private final double pval;
    private final String name;
    private final HashMap<String, Double> targets;

    public Kinase(String name, double log2fc, double pval, HashMap<String, Double> targets) {
        this.log2fc = log2fc;
        this.pval = pval;
        this.name = name;
        this.targets = targets;
    }

    public double getLog2fc() {
        return log2fc;
    }

    public double getPval() {
        return pval;
    }

    public String getName() {
        return name;
    }

    public HashMap<String, Double> getTargets() {
        return targets;
    }
}
