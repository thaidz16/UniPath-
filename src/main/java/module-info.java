module com.thai.unipath {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.google.gson;
    opens com.thai.unipath to javafx.fxml, com.google.gson,dev.langchain4j;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires org.apache.logging.log4j;

    exports com.thai.unipath;
    exports com.thai.unipath.model;
    exports com.thai.unipath.service;
    opens com.thai.unipath.service;
    requires langchain4j;
    requires langchain4j.core;
    requires langchain4j.google.ai.gemini;
}