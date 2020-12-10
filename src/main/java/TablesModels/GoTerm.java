package TablesModels;

public class GoTerm {
    private String goId;
    private String name;

    public GoTerm(String goId, String name) {
        this.goId = goId;
        this.name = name;
    }

    public String getGoId() {
        return goId;
    }

    public String getName() {
        return name;
    }
}
