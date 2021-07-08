package pathway;

public class Gene {

    private String name;
    private String uniprot;
    private String id;

    public Gene(String id, String name, String uniprot) {
        this.id = id;
        this.name = name;
        this.uniprot = uniprot;
    }

    public String getId(){
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUniprot() {
        return uniprot;
    }
}
