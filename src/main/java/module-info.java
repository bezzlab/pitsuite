/**
 *
 */
module pitguivTwo {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.web;
    requires javafx.fxml;
    requires org.json;
    requires org.controlsfx.controls;
    requires org.apache.commons.io;
    requires commons.math3;
    requires jdk.jsobject;
    requires json.simple;
    requires com.google.gson;  //to read json files
    requires com.jfoenix; // to stylized visual elements
    requires htsjdk; // for samtools
    requires nitrite;
    requires mongo.java.driver;
    exports Controllers;
    exports mongoDB;
    exports pitguiv2;
    exports pathway;
    exports pathway.alerts;
    exports Controllers.drawerControllers;
    requires grep4j;

    opens Cds;
    opens Controllers;
    opens mongoDB;
    opens TablesModels;
    opens graphics;
    opens Controllers.drawerControllers;
    opens pathway.alerts;
    opens utilities;

    requires org.kordamp.iconli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome;
    requires diff.match.patch;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires org.apache.httpcomponents.httpmime;
    requires commons.fileupload;
    requires java.desktop;
    requires javafx.swing;
    requires JavaFXSmartGraph;


}