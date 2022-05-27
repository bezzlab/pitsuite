package utilities;

import javafx.scene.control.TextArea;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class CopyFiles {



    public static void copy(String configPath, TextArea area){
        JSONParser parser = new JSONParser();
        try {
            JSONObject config = new JSONObject(parser.parse(new FileReader(configPath)).toString());
            JSONObject configCopy = new JSONObject(config);
            String output = config.getString("output");
            config.put("output", "/project");


            JSONObject conditions = config.getJSONObject("conditions");
            for(String condition: conditions.keySet()){
                File condDir = new File(output+"/"+condition);
                if (!condDir.exists()){
                    condDir.mkdirs();
                }

                JSONObject samples = conditions.getJSONObject(condition);
                for(String sample: samples.keySet()){
                    File sampleDir = new File(output+"/"+condition+"/"+sample);
                    if (!sampleDir.exists()){
                        sampleDir.mkdirs();
                    }

                    JSONObject sampleObj = samples.getJSONObject(sample);
                    if(sampleObj.has("left")){
                        Path p = Paths.get(sampleObj.getString("left"));
                        String file = p.getFileName().toString();
                        System.out.println(sampleObj.getString("left"));
                        if(!sampleObj.getString("left").equals(output+"/"+condition+"/"+sample+"/"+file)){

                            //Files.copy(p, Paths.get(output+"/"+condition+"/"+sample+"/"+file), StandardCopyOption.REPLACE_EXISTING);
                            area.setText(area.getText()+"Copying "+file+"\n");
                        }
                        sampleObj.put("left", "/project/"+condition+"/"+sample+"/"+file);
                    }
                    if(sampleObj.has("right")){
                        Path p = Paths.get(sampleObj.getString("right"));
                        String file = p.getFileName().toString();
                        System.out.println(sampleObj.getString("right"));
                        if(!sampleObj.getString("right").equals(output+"/"+condition+"/"+sample+"/"+file)){

                            //Files.copy(p, Paths.get(output+"/"+condition+"/"+sample+"/"+file), StandardCopyOption.REPLACE_EXISTING);
                            area.setText(area.getText()+"Copying "+file+"\n");
                        }
                        sampleObj.put("right", "/project/"+condition+"/"+sample+"/"+file);
                    }
                    if(sampleObj.has("single")){
                        Path p = Paths.get(sampleObj.getString("single"));
                        String file = p.getFileName().toString();
                        System.out.println(sampleObj.getString("single"));
                        if(!sampleObj.getString("single").equals(output+"/"+condition+"/"+sample+"/"+file)){

                            //Files.copy(p, Paths.get(output+"/"+condition+"/"+sample+"/"+file), StandardCopyOption.REPLACE_EXISTING);
                            area.setText(area.getText()+"Copying "+file+"\n");
                        }
                        sampleObj.put("single", "/project/"+condition+"/"+sample+"/"+file);
                    }
                }
            }

            if(config.has("reference_fasta")){
                Path p = Paths.get(config.getString("reference_fasta"));
                String file = p.getFileName().toString();
                System.out.println(config.getString("reference_fasta"));
                if(!config.getString("reference_fasta").equals(output+"/"+file)){

                    //Files.copy(p, Paths.get(output+"/"+file), StandardCopyOption.REPLACE_EXISTING);
                    area.setText(area.getText()+"Copying "+file+"\n");
                }
                config.put("reference_fasta", "/project/"+file);
            }
            if(config.has("reference_gff")){
                Path p = Paths.get(config.getString("reference_gff"));
                String file = p.getFileName().toString();
                System.out.println(config.getString("reference_gff"));
                if(!config.getString("reference_gff").equals(output+"/"+file)){
                    //Files.copy(p, Paths.get(output+"/"+file), StandardCopyOption.REPLACE_EXISTING);
                    area.setText(area.getText()+"Copying "+file+"\n");
                }
                config.put("reference_gff", "/project/"+file);
            }

            File msDir = new File(output+"/ms");
            if (!msDir.exists()){
                msDir.mkdirs();
            }

            JSONObject runs = config.getJSONObject("ms").getJSONObject("runs");
            for(String run: runs.keySet()){
                File runDir = new File(output+"/ms");
                if (!runDir.exists()){
                    runDir.mkdirs();
                }
                JSONObject runObj = runs.getJSONObject(run);
                if(runObj.get("files").getClass()== JSONArray.class){
                    JSONArray files = runObj.getJSONArray("files");
                    ArrayList<String> newPaths = new ArrayList<>();
                    for(Object f: files){
                        String filePath = (String) f;
                        Path p = Paths.get(filePath);
                        String file = p.getFileName().toString();
                        System.out.println(filePath);
                        if(!filePath.equals(output+"/ms/"+run+"/"+file)){
                            //Files.copy(p, Paths.get(output+"/ms/"+run+"/"+file), StandardCopyOption.REPLACE_EXISTING);
                            area.setText(area.getText()+"Copying "+file+"\n");
                        }
                        newPaths.add( "/project/ms/"+run+"/"+file);
                    }
                    runObj.put("files", newPaths);
                }else{
                    String filePath = runObj.getString("files");
                    Path p = Paths.get(filePath);
                    String file = p.getFileName().toString();
                    System.out.println(filePath);
                    if(!filePath.equals(output+"/ms/"+run+"/"+file)){
                        //Files.copy(p, Paths.get(output+"/ms/"+run+"/"+file), StandardCopyOption.REPLACE_EXISTING);
                        area.setText(area.getText()+"Copying "+file+"\n");
                    }
                    runObj.put("files", "/project/ms/"+run+"/"+file);
                }
            }

            FileWriter file = new FileWriter(output+"/config_docker.json");
            file.write(config.toString(4));
            file.close();



        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}
