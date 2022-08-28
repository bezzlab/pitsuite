package pathway;

public class Gene extends Entity{


    private String uniprot;

    private double value;

    public Gene(String id, String name, String uniprot) {
        super(name, "Gene", id);
        this.uniprot = uniprot;
    }

    public Gene(String id, String name) {
        super(name, "Gene", id);
    }
    public Gene(String name) {
        super(name, "Gene");
    }


    public String getUniprot() {
        return uniprot;
    }

    @Override
    public Double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
