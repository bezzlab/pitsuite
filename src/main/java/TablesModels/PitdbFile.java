package TablesModels;

public class PitdbFile {

    private String name;
    private double size;
    private boolean uploaded;

    public PitdbFile(String name, double size, boolean uploaded) {
        this.name = name;
        this.size = size;
        this.uploaded = uploaded;
    }

    public PitdbFile(String name) {
        this.name = name;
    }

    public PitdbFile(String name, boolean uploaded) {
        this.name = name;
        this.uploaded = uploaded;
    }

    public String getName() {
        return name;
    }

    public double getSize() {
        return size;
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }
}
