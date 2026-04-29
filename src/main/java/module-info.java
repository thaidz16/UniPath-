module com.thai.unipath {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.google.gson;
    opens com.thai.unipath to javafx.fxml, com.google.gson;

    // Bộ đồ chơi UI xịn xò ông tích lúc đầu
    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    // "Vũ khí" Apache POI để đọc/ghi file Excel
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires org.apache.logging.log4j;

    exports com.thai.unipath;
    exports com.thai.unipath.model; // Nhớ export model để giao diện đọc được dữ liệu
}