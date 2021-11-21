package pitguiv2;


import org.json.JSONObject;

public class Settings {

    private static Settings instance = null;
    private JSONObject settings;
    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    public void setSetting(JSONObject settings){
        this.settings = settings;
    }


    public int getMinSashimiReads(){
        return settings.getJSONObject("Browser").getInt("minSashimiReads");
    }

    public String getPathToR(){
        return settings.getJSONObject("tools").getString("R");
    }

    public JSONObject getSettings() {
        return settings;
    }
}