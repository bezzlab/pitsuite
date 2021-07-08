package pathway;

public class SearchResult {

    String type;
    String id;
    String summation;
    String name;

    public SearchResult(String type, String id, String name, String summation) {
        this.type = type;
        this.id = id;
        this.name = name;
        this.summation = summation;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getSummation() {
        return summation;
    }

    public String getName(){
        return name;
    }
}
