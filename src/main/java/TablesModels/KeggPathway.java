package TablesModels;

public class KeggPathway {

    private String id;
    private String name;

    public KeggPathway(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj.getClass() != this.getClass()) {
            return false;
        }

        final KeggPathway other = (KeggPathway) obj;
        if (!this.name.equals(other.getName()) || !this.id.equals(other.getId())) {
            return false;
        }


        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 53 * hash + this.id.hashCode();
        return hash;
    }

    @Override
    public String toString(){
        return id+": "+name;
    }
}
