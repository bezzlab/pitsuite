package utilities.spectrumviewer;


import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Float.isNaN;

public class Peptide {


    String sequence;
    double ntermModification;
    double ctermModification;
    int maxNeutralLossCount;
    private HashMap<Integer, ArrayList<NeutralLoss>> potentialLossesAtIndex = new HashMap<>();
    private HashMap<String, NeutralLoss> potentialLosses_lorikeet = new HashMap<>();
    private HashMap<String, NeutralLoss> potentialLosses_custom = new HashMap<>();
    private HashMap<String, Modification> staticMods = new HashMap<>();
    private HashMap<Integer, Modification> varMods = new HashMap<>();
    private ArrayList<ArrayList<LossCombinationList>> nterm_totalLossOptions = new ArrayList<>();
    private ArrayList<ArrayList<LossCombinationList>> cterm_totalLossOptions = new ArrayList<>();
    boolean debug = false;
    double ntermMod;
    double ctermMod;


    public String getSequence() {
        return sequence;
    }

    public double getNtermModification() {
        return ntermModification;
    }

    public double getCtermModification() {
        return ctermModification;
    }

    public int getMaxNeutralLossCount() {
        return maxNeutralLossCount;
    }

    public Peptide(String seq, ArrayList<Modification> staticModifications, ArrayList<Modification> varModifications,
                   double ntermModification, double ctermModification, int maxNeutralLossCount) {

        sequence = seq;
        ntermMod = ntermModification;
        ctermMod = ctermModification;


        if (sequence == null) {
            sequence = "";
        }

        this.ntermModification = ntermModification;
        this.ctermModification = ctermModification;

        this.maxNeutralLossCount = maxNeutralLossCount;

        var debug = false;

        initNeutralLosses();
        initStaticMods(staticModifications);
        initVarMods(varModifications);
        // calculate the total loss options at each index using only custom neutral losses.
        calculateTotalLossOptions(null, maxNeutralLossCount);


    }

    public void initNeutralLosses() {
        for (var i = 0; i < sequence.length(); i++) {
            potentialLossesAtIndex.put(i, new ArrayList<>()); // potential neutral loss possibilities at an index in the peptide sequence
        }

        var ammoniaLoss = NeutralLoss.AmmoniaLoss();
        var waterLoss = NeutralLoss.WaterLoss();
        var phosphoLoss = NeutralLoss.PhosphoLoss();
        potentialLosses_lorikeet.put(ammoniaLoss.getLabel(), ammoniaLoss);
        potentialLosses_lorikeet.put(waterLoss.getLabel(), waterLoss);
        potentialLosses_lorikeet.put(phosphoLoss.getLabel(), phosphoLoss);

        for (var i = 0; i < sequence.length(); i += 1) {
            var aa = sequence.charAt(i);
            if (aa == 'K' || aa == 'R' || aa == 'Q' || aa == 'N') {
                potentialLossesAtIndex.get(i).add(ammoniaLoss);
            }
            if (aa == 'S' || aa == 'T' || aa == 'E' || aa == 'D') {
                potentialLossesAtIndex.get(i).add(waterLoss);
            }
            if (aa == 'S' || aa == 'T' || aa == 'Y') {
                potentialLossesAtIndex.get(i).add(phosphoLoss);
            }
        }
    }


    public static class NeutralLoss {

        private double monoLossMass;
        private double avgLossMass;
        private String formula;
        private String userLabel;
        private String htmlLabel;

        public double getMonoLossMass() {
            return monoLossMass;
        }

        public double getAvgLossMass() {
            return avgLossMass;
        }

        NeutralLoss(double lossMassMono, double lossMassAvg, String formula, String label) {
            this.monoLossMass = lossMassMono;
            if (this.monoLossMass<0)
                this.monoLossMass = 0.0;
            this.avgLossMass = lossMassAvg;
            if (this.avgLossMass<0)
                this.avgLossMass = 0.0;
            this.formula = formula;
            this.userLabel = label;
        }

        public String getLongLabel(){
            if (formula!=null) {
                return formula + " (" + userLabel+ ")";
            } else {
                return this.userLabel;
            }
        }


        public String getLabel()
        {
            if (this.userLabel!=null) return this.userLabel;
            return "-" + Math.round(this.monoLossMass);
        }

        public String getHtmlLabel()
        {
            if (htmlLabel!=null)
                return htmlLabel;

            htmlLabel = "";
            // html += H<sub>2</sub>O (<span style="font-weight: bold;">o</span>)
            if (formula!=null) {
                for (var i = 0; i < formula.length(); i++) {
                    var charAt = formula.charAt(i);
                    if (!isNaN(charAt)) {
                        htmlLabel += "<sub>" + charAt + "</sub>";
                    } else {
                        htmlLabel += charAt;
                    }
                }
                htmlLabel += " (";
            }
            htmlLabel += "<span style=\"font-weight: bold;\">" + userLabel+ "</span>";
            if (formula!=null) {
                htmlLabel += ")";
            }

            return htmlLabel;
        }

        public static NeutralLoss AmmoniaLoss(){
            return new NeutralLoss(Ion.AmmoniaLossMass_mono,Ion.AmmoniaLossMass_avg,"NH3","*");
        }

        public static NeutralLoss WaterLoss()
        {
            return new NeutralLoss(Ion.WaterLossMass_mono,Ion.WaterLossMass_avg,"H2O","o");
        }
        public static NeutralLoss PhosphoLoss()
        {
            return new NeutralLoss(Ion.PhosphoLossMass_mono,Ion.PhosphoLossMass_avg,"H3PO4","p");
        }
    }

    public double getSeqMassMono(int index, String term) {
        return getSeqMass(index, term, "mono");
    }

    public HashMap<Integer, Modification> getVarMods(){
        return varMods;
    }
    public double getSeqMassAvg(int index, String term){
        return getSeqMass(index,term,"avg");
    }

    double getNeutralMassMono(){

        double mass=0;
        AminoAcid aa_obj=new AminoAcid();
        if(sequence!=null){
            for(var i=0;i<sequence.length();i++){
                var aa=aa_obj.getAA(String.valueOf(sequence.charAt(i)));
                mass+=aa.mono;
            }
        }

        mass=addTerminalModMass(mass,"n");
        mass=addTerminalModMass(mass,"c");
        mass=addResidueModMasses(mass,sequence.length(),"n");
        // add N-terminal H
        mass=mass+Ion.MASS_H_1;
        // add C-terminal OH
        mass=mass+Ion.MASS_O_16+Ion.MASS_H_1;

        return mass;
    }

    public double getNeutralMassAvg(){

        double mass=0;
        AminoAcid aa_obj=new AminoAcid();
        if(sequence!=null){
            for(var i=0;i<sequence.length();i++){
                var aa=aa_obj.getAA(String.valueOf(sequence.charAt(i)));
                mass+=aa.avg;
            }
        }

        mass=addTerminalModMass(mass,"n");
        mass=addTerminalModMass(mass,"c");
        mass=addResidueModMasses(mass,sequence.length(),"n");
        // add N-terminal H
        mass=mass+Ion.MASS_H;
        // add C-terminal OH
        mass=mass+Ion.MASS_O+Ion.MASS_H;

        return mass;
    }

    public ArrayList<LossCombinationList> getPotentialLosses(Ion sion)
    {
        String term=sion.getTerm();
        int fragmentIndex=sion.getFragmentIndex();
        if(term.equals("n"))
        {
            return nterm_totalLossOptions.get(fragmentIndex-1);
        }
        if(term.equals("c"))
        {
            return cterm_totalLossOptions.get(sequence.length()-fragmentIndex);
        }
        return null;
    }

    public NeutralLoss getLossForLabel(String lossLabel)
    {
        NeutralLoss loss = potentialLosses_custom.get(lossLabel);
        if(loss!=null)return loss;
        loss=potentialLosses_lorikeet.get(lossLabel);
        return loss;
    }

    public void recalculateLossOptions(ArrayList<NeutralLoss> selectedLossOptions,int maxLossCount)
    {
        calculateTotalLossOptions(selectedLossOptions,maxLossCount);
    }
    public void initStaticMods(ArrayList<Modification> staticModifications)
    {
        if(staticModifications!=null){
            for(var i=0;i<staticModifications.size();i+=1){
                Modification mod=staticModifications.get(i);
                staticMods.put(mod.getAa().getCode(),mod);
            }

            for(var i=0;i<sequence.length();i+=1){
                Modification mod=staticMods.get(sequence.charAt(i));
                if(mod!=null&&mod.getLosses().size()>0){
                    for(var j=0;j<mod.getLosses().size();j++){
                        addCustomLoss(i,mod.getLosses().get(j));
                    }
                }
            }
        }
    }

    public void initVarMods(ArrayList<Modification> varModifications)
    {
        if(varModifications!=null){
            for(var i=0;i<varModifications.size();i+=1){
                Modification mod=varModifications.get(i);
                if(mod!=null){
                    varMods.put(mod.getPosition(),mod);

                    if(mod.getLosses().size()>0){
                        for(var j=0;j<mod.getLosses().size();j++){
                            var modIndex_0based=mod.position-1; // mod.position is a 1-based index
                            addCustomLoss(modIndex_0based,mod.getLosses().get(j));
                        }
                    }
                }
            }
        }
    }

    public void addCustomLoss(int index, NeutralLoss loss)
    {
        // Example: {avgLossMass: "97.995", monoLossMass: "97.977", formula: "H3PO4"}
        if(loss!=null&&loss.avgLossMass>0.0&&loss.monoLossMass>0.0)
        {
            var neutralLoss=new NeutralLoss(loss.monoLossMass,loss.avgLossMass,loss.formula,loss.getLabel());
            potentialLossesAtIndex.get(index).add(neutralLoss);
            potentialLosses_custom.put(neutralLoss.getLabel(),neutralLoss);
        }
    }

    public void calculateTotalLossOptions(ArrayList<NeutralLoss> selectedLosses, int maxLossCount)
    {
        if(maxLossCount==0)
            maxLossCount=1;


        HashMap<String, Boolean> selectedLossLabels= new HashMap<>();
        if(selectedLosses==null)
        {
            for(String lossLabel: potentialLosses_custom.keySet())
            {
                selectedLossLabels.put(lossLabel, true);
            }
        }
        else
        {
            for(var i=0;i<selectedLosses.size();i+=1)
            {
                if(selectedLosses.get(i)!=null)
                {
                    selectedLossLabels.put(selectedLosses.get(i).getLabel(), true);
                }
            }
        }
        initNeutralLossArrays(maxLossCount);
        calculateTotalLossOptionsForTerm("n",selectedLossLabels,maxLossCount);
        calculateTotalLossOptionsForTerm("c",selectedLossLabels,maxLossCount);

        if(debug)
        {
            printPotentialLosses();
            printNeutralLossCombinations();
        }
    }

    public void initNeutralLossArrays(int maxLossCount)
    {
        for(var i=0;i<sequence.length();i+=1)
        {
            nterm_totalLossOptions.add(i, new ArrayList<>());
            cterm_totalLossOptions.add(i, new ArrayList<>());
            for(var j=0;j<=maxLossCount;j+=1)
            {
                nterm_totalLossOptions.get(i).add(j, new LossCombinationList(0));
                cterm_totalLossOptions.get(i).add(j, new LossCombinationList(0));
            }
        }
    }

    public void calculateTotalLossOptionsForTerm(String term, HashMap<String, Boolean> selectedLossLabels, int maxLossCount)
    {
        if(term.equals("n"))
        {
            for(int i=0;i<sequence.length();i+=1){
                calculate(i,0,1,selectedLossLabels,nterm_totalLossOptions,maxLossCount);
            }
        }
        if(term.equals("c"))
        {
            var s=sequence.length()-1;
            for(int i=s;i>=0;i-=1){
                calculate(i,s,-1,selectedLossLabels,cterm_totalLossOptions,maxLossCount);
            }
        }
    }

    public void calculate(int i, int s, int incr, HashMap<String, Boolean> selectedLossLabels,
                          ArrayList<ArrayList<LossCombinationList>> totalLossOptionsArray, int maxLossCount){

        ArrayList<NeutralLoss> lossOptions=potentialLossesAtIndex.get(i);
        ArrayList<NeutralLoss> validLossOptions= new ArrayList<>();
        for(var l=0;l<lossOptions.size();l+=1){  // these are the potential losses at index i in the sequence.
            NeutralLoss loss=lossOptions.get(l);
            if(!selectedLossLabels.containsKey(loss.getLabel()))
                continue;
            else
                validLossOptions.add(loss);
        }

        if(validLossOptions.size()==0)
        {
            if(i!=s)
            {
                totalLossOptionsArray.set(i, totalLossOptionsArray.get(i-incr));
            }
            else
            {
                for(var j=1;j<=maxLossCount;j+=1)
                {
                    totalLossOptionsArray.get(i).set(j,new LossCombinationList(j));
                }
            }
            return;
        }

        ArrayList<LossCombinationList> loss_options_at_i= new ArrayList<>();
        loss_options_at_i.add(null);
        totalLossOptionsArray.set(i,loss_options_at_i);
        if(i==s)
        {
            for(int j=1;j<=maxLossCount;j+=1)
            {
                loss_options_at_i.add(j, new LossCombinationList(j));
            }
        }
        else
        {
            ArrayList<LossCombinationList> loss_options_before_i=totalLossOptionsArray.get(i-incr);
            for(int j=1;j<=maxLossCount;j+=1)
            {
                loss_options_at_i.add(j, LossCombinationList.copyLossCombinationList(loss_options_before_i.get(j)));
            }
        }

        for(var l=0;l<validLossOptions.size();l+=1)
        {
            var loss=validLossOptions.get(l);

            // iterate in reverse order so that we don't add the loss
            // in each iteration.
            for(var j=maxLossCount;j>=1;j-=1)
            {
                if(j==1)
                {
                    var loss_combi_list_at_j=loss_options_at_i.get(j);
                    var loss_combi=new LossCombination();
                    loss_combi.addLoss(loss);
                    loss_combi_list_at_j.addLossCombination(loss_combi);
                }
                else
                {
                    var loss_combi_list_at_jminus1=loss_options_at_i.get(j-1);
                    var loss_combi_list_at_j=LossCombinationList.copyLossCombinationList(loss_combi_list_at_jminus1);
                    loss_options_at_i.set(j, loss_combi_list_at_j);
                    for(var k=0;k<loss_combi_list_at_j.lossCombinationCount();k+=1)
                    {
                        loss_combi_list_at_j.getLossCombination(k).addLoss(loss);
                    }
                }
            }
        }
    }

    public ArrayList<Pair<Double, Double>> getMassesWithLoss(ArrayList<Double> lossOptions, double mass){
        ArrayList<Pair<Double, Double>> massesWithLoss=new ArrayList<>();
        var j=0;
        for(var i=0;i<lossOptions.size();i+=1){
            double loss=lossOptions.get(i);
            if(loss>0.0){
                var massWithLoss=mass-loss;
                massesWithLoss.add(j, new Pair<>(massWithLoss, loss));
                j++;
            }
        }
        return massesWithLoss;
    }

    public double addResidueModMasses(double seqMass, int index, String term){

        double mass=seqMass;
        Slice slice=new Slice(index,term);
        for(int i=slice.from;i<slice.to;i+=1){
            // add any static modifications
            Modification mod=staticMods.get(String.valueOf(sequence.charAt(i)));
            if(mod!=null){
                mass+=mod.getMass();
            }
            // add any variable modifications
            mod=varMods.get(i+1); // varMods index in the sequence is 1-based
            if(mod!=null){
                mass+=mod.getMass();
            }
        }

        return mass;
    }

    public double getSeqMass(int index, String term, String massType){

        double mass=0;
        var aa_obj=new AminoAcid();
        if(sequence!=null){
            Slice slice=new Slice(index,term);
            for(int i=slice.from;i<slice.to;i+=1){
                AminoAcid aa=aa_obj.getAA(String.valueOf(sequence.charAt(i)));
                mass+=aa.getMass(massType);
            }
        }

        mass=addTerminalModMass(mass,term);
        mass=addResidueModMasses(mass,index,term);
        return mass;
    }

    public double addTerminalModMass(double seqMass, String term){

        double mass=seqMass;
        // add any terminal modifications
        if(term.equals("n"))
            mass+=ntermMod;
        if(term.equals("c"))
            mass+=ctermMod;

        return mass;
    }

    class Slice {
        int from;
        int to;

        public Slice(int index, String term) {
            if (term.equals("n")) {
                this.from = 0;
                this.to = index;
            }
            if (term.equals("c")) {
                this.from = index;
                this.to = sequence.length();
            }
        }
    }

    public void printNeutralLossCombinations(){

        for(var i=1;i<sequence.length();i+=1)
        {
            String subseq=sequence.substring(0,i);
            ArrayList<LossCombinationList> lossOpts=nterm_totalLossOptions.get(i);
            _log(subseq+" -- ");
            for(var j=1;j<lossOpts.size();j+=1)
            {
                _log(" -- "+j);
                var lossesAt_j=lossOpts.get(j);
                var count=lossesAt_j.lossCombinationCount();
                for(var k=0;k<count; k+=1)
                {
                    var lossCombo=lossesAt_j.getLossCombination(k);
                    _log("---- "+lossCombo.getLabels());
                }
            }
        }

        for(var i=sequence.length()-1;i>=0;i-=1)
        {
            var subseq=sequence.substring(i,sequence.length());
            ArrayList<LossCombinationList> lossOpts=cterm_totalLossOptions.get(i);
            _log(subseq+" -- ");
            for(var j=1;j<lossOpts.size();j+=1)
            {
                _log(" -- "+j);
                var lossesAt_j=lossOpts.get(j);
                var count=lossesAt_j.lossCombinationCount();
                for(var k=0;k<count; k+=1)
                {
                    var lossCombo=lossesAt_j.getLossCombination(k);
                    _log("---- "+lossCombo.getLabels());
                }
            }
        }
    }

    public void printPotentialLosses(){
        for(var i=0;i<potentialLossesAtIndex.size();i+=1){
            ArrayList<NeutralLoss> losses=potentialLossesAtIndex.get(i);
            if(losses.size()>0)
                _log("Potential Losses at "+i+" "+sequence.charAt(i));
            for(var j=0;j<losses.size();j++){
                NeutralLoss loss=losses.get(j);
                _log(loss.monoLossMass+", "+loss.avgLossMass+", "+loss.formula+", "+loss.getLabel());
            }
        }
    }

    public void _log(String message)
    {
        if(debug)
            System.out.println(message);
    }


    public static class LossCombination {

        ArrayList<Peptide.NeutralLoss> losses = new ArrayList<>();

        public void addLoss(Peptide.NeutralLoss loss) {
            this.losses.add(loss);
        }

        public String getLabels(){
            String label = "";
            for(var l=0;l< this.losses.size();l+=1)
            {
                label+=", "+this.losses.get(l).getLabel();
            }
            return label;

        }

        public String getLabel()
        {
            if(losses.size()==1)
            {
                return" "+this.losses.get(0).getLabel();
            }
            else
            {
                var lossMass=0;
                for(var i=0;i< this.losses.size();i+=1)
                {
                    lossMass+=this.losses.get(i).getMonoLossMass();
                }
                return" -"+Math.round(lossMass);
            }
        }

        public double getTotalLossMass(String massType)
        {
            var totalLoss=0;
            for(var l=0;l< this.losses.size();l+=1)
            {
                if(massType.equals("mono"))
                {
                    totalLoss+=this.losses.get(l).getMonoLossMass();
                }
                else if(massType.equals("avg"))
                {
                    totalLoss+=this.losses.get(l).getAvgLossMass();
                }
            }
            return totalLoss;
        }

        public static LossCombination copyLossCombination(LossCombination originalLossCombination)
        {
            var newLossCombination=new LossCombination();
            var losses=originalLossCombination.losses;
            for(var l=0;l<losses.size();l+=1)
            {
                newLossCombination.addLoss(losses.get(l));
            }
            return newLossCombination;
        }
    }

    public static class LossCombinationList {

        int numLosses;
        ArrayList<LossCombination> lossCombinations = new ArrayList<>();

        LossCombinationList(int numLosses) {
            this.numLosses = numLosses;
        }

        public int lossCombinationCount()
        {
            return this.lossCombinations.size();
        }

        public void addLossCombination(LossCombination lossCombination)
        {
            var found=false;

            for(var l=0;l< this.lossCombinationCount();l+=1)
            {
                if(this.lossCombinations.get(l).getTotalLossMass("mono")==
                        lossCombination.getTotalLossMass("mono"))
                {
                    found=true;
                }
            }
            if(!found)
            {
                this.lossCombinations.add(lossCombination);
            }
        }

        public LossCombination getLossCombination(int index)
        {
            if(index>=0&&index< this.lossCombinationCount())
            {
                return this.lossCombinations.get(index);
            }
            return null;
        }

        public static LossCombinationList copyLossCombinationList(LossCombinationList originalList)
        {
            var newLossCombinationList = new LossCombinationList(originalList.numLosses);
            var count=originalList.lossCombinationCount();
            for(var l=0;l<count; l+=1)
            {
                var originalLossCombination=originalList.getLossCombination(l);
                var lossCombination=LossCombination.copyLossCombination(originalLossCombination);
                newLossCombinationList.addLossCombination(lossCombination);
            }
            return newLossCombinationList;
        }
    }

}




