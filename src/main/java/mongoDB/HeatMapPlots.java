package mongoDB;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class HeatMapPlots {

    public static void heatMapPlotsPng(String path) throws IOException {

        // get the location of the required files
        HashMap<String, Path> dgePathsMap = new HashMap<>();
        HashMap<String, Path> normalizedReadCountPathsMap = new HashMap<>();

        fileWalkwerSetMaps(path, dgePathsMap, normalizedReadCountPathsMap);

        for (Map.Entry<String, Path> dgeEntry: dgePathsMap.entrySet()){
            String comparisonKey = dgeEntry.getKey();

            if (dgePathsMap.containsKey(comparisonKey) && normalizedReadCountPathsMap.containsKey(comparisonKey)){
                String dgePath = dgePathsMap.get(comparisonKey).toString();
                String normReadCountPath = normalizedReadCountPathsMap.get(comparisonKey).toString();

                String outputGraphPath = path + "/" + comparisonKey + "_heatmap.png";

                dgeHeatMaps( outputGraphPath, dgePath, normReadCountPath);

            }

        }
    }

        private static void fileWalkwerSetMaps(String path, HashMap<String, Path> dgePathsMap, HashMap<String, Path> normalizedReadCountPathsMap) {

            Stream<Path> filePathStream = null;
            try {

                filePathStream = Files.walk(Paths.get(String.valueOf(path)));

            } catch (IOException e) {
                e.printStackTrace();
            }

            filePathStream.forEach(filePath -> {

                if (Files.isRegularFile(filePath)) {
                    if (filePath.getFileName().toString().contains("geneReadCount_normalised.csv")) {
                        String fileNameString = filePath.getFileName().toString();
                        String[] fileNameStringArray = fileNameString.split("_");
                        String comparisonName = fileNameStringArray[0];

                        normalizedReadCountPathsMap.put(comparisonName, filePath);

                    } else if (filePath.getFileName().toString().contains("_dge")) {

                        String fileNameString = filePath.getFileName().toString();
                        String[] fileNameStringArray = fileNameString.split("_");
                        String comparisonName = fileNameStringArray[0];

                        dgePathsMap.put(comparisonName, filePath);

                    }
                }

            });
        }

    private static void dgeHeatMaps(String outputGraphPath, String dgePath, String normalizedReadsPath){

        String output = "";
        try {
            String rScriptPath = System.getProperty("user.dir") + "/Rscripts/heatMapReadCountNormalized.R";

            ProcessBuilder pb=new ProcessBuilder("Rscript", rScriptPath, outputGraphPath, normalizedReadsPath, dgePath);
            pb.redirectErrorStream(true);
            Process process=pb.start();
            BufferedReader inStreamReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line = inStreamReader.readLine();
            while(line != null){
                line = inStreamReader.readLine();
                System.out.println(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
