package utilities;

public class Sample {

    private final String condition;
    private String sample;


    public Sample(String condition) {
        this.condition = condition;
    }

    public Sample(String condition, String sample) {
        this.condition = condition;
        this.sample = sample;
    }

    public String getSample(){
        if(sample==null){
            return condition;
        }

        return condition+"/"+sample;
    }


}
