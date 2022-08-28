package Controllers;

import javafx.concurrent.Worker;
import javafx.scene.Node;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class WebViewController {

    WebView webView;
    WebEngine webEngine;
    String html;
    String[] javascript;
    String pathCss;
    boolean isLoaded=false;
    private double width;
    private double height;
    private String title;
    private double fontsize;
    private Controller sourceController;
    private String lastCommand;
    private String type;



    public WebViewController(String html, String[] javascript, double width, double height, String title, double fontsize) {
        this.html = html;
        this.javascript = javascript;
        this.width = width;
        this.height = height;
        this.title = title;
        this.fontsize = fontsize;
        webView =  new WebView();
        webView.setStyle("-fx-background-color: #757FDB");
        webView.setPrefHeight(height);
        webView.setPrefWidth(width);
        webEngine = webView.getEngine();
    }

    public WebViewController(String html, String[] javascript, double width, double height, String title, double fontsize, Controller sourceController) {
        this.html = html;
        this.javascript = javascript;
        this.width = width;
        this.height = height;
        this.title = title;
        this.fontsize = fontsize;
        this.sourceController = sourceController;
        webView =  new WebView();
        webView.setStyle("-fx-background-color: #757FDB");
        webView.setPrefHeight(height);
        webView.setPrefWidth(width);
        webEngine = webView.getEngine();
    }


    public WebViewController(String html, String[] javascript, double width, double height,  double fontsize, Controller sourceController, String type) {
        this.html = html;
        this.javascript = javascript;
        this.width = width;
        this.height = height;
        this.fontsize = fontsize;
        this.sourceController = sourceController;
        webView =  new WebView();
        webView.setStyle("-fx-background-color: #757FDB;");
        webView.setPrefHeight(height);
        webView.setPrefWidth(width);
        webEngine = webView.getEngine();
        this.type = type; // used in case of GO webview
    }


    public void initialise(String command){
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                try {
                    for (String s : javascript) {
                        webEngine.executeScript(new String(IOUtils.toByteArray(getClass().getResourceAsStream("/javascript/" + s + ".js"))));
                    }
                    JSObject jsobj = (JSObject) webEngine.executeScript("window");
                    if (type != null) {
                        if (type.equals("GO")) {
                            jsobj.setMember("java", new SendGoToJavaBridge());
                        }
                    } else {
                        jsobj.setMember("java", new DisplayGeneBrowserBridge());
                    }

                    webEngine.executeScript(command);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                isLoaded = true;
            }
        });
        webEngine.load(getClass().getResource("/html/"+html+".html").toExternalForm());
    }

    public void execute(String command){


        lastCommand = command;
        if ( type != null ){
            if (type.equals("GO")){
                if (command.startsWith("plotNetwork")){
                    command = command.substring(0, command.length()-1) + "," + width +","+ height +", "+ fontsize+")";
                } else {
                    command = command.substring(0, command.length()-1)  + width +","+ height + ")";
                }
            }
        } else {
            command = command.substring(0, command.length()-1) + "," + width +","+ height +",\""+ title +"\","+ fontsize+")";
        }

        if(isLoaded){
            webEngine.executeScript(command);
        }else{
            initialise(command);
        }
    }

    public WebView getWebView() {
        return webView;
    }

    public WebEngine getWebEngine() {
        return webEngine;
    }

    public void resize(){
        if(lastCommand!=null){
            width = webView.getWidth();
            height = webView.getHeight();
            execute(lastCommand);
        }
    }
    public void resize(double fontsize){
        this.fontsize = fontsize;
        if(lastCommand!=null){
            execute(lastCommand);
        }
    }


    public class DisplayGeneBrowserBridge {
        public void callbackFromJavaScript(String gene) {
            System.out.println(gene);
            sourceController.goToGeneBrowser(gene);
        }

    }

    public class SendGoToJavaBridge{
        public void callbackFromJavaScript(String goId) {
            System.out.println("WebCtrl L161 selected node: " + goId);
            GoTermsController goTermController = (GoTermsController) sourceController;
            goTermController.getGoIdFromJs(goId);
        }
    }

}
