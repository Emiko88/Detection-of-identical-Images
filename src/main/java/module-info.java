module com.eugenegladkiy.test
{
        requires javafx.controls;
        requires javafx.fxml;
        requires java.desktop;
        requires org.apache.commons.codec;
        requires imgscalr.lib;

        opens com.eugenegladkiy to javafx.graphics;
        exports com.eugenegladkiy.test;
}
