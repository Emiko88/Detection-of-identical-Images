package com.eugenegladkiy;

import javafx.application.Application;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import org.apache.commons.codec.digest.DigestUtils;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class HelloApplication extends Application {

    private TextArea resultArea;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Обнаружение одинаковых изображений");

        Button folder1Button = new Button("Выбрать папку 1");
        Button folder2Button = new Button("Выбрать папку 2");
        Button startButton = new Button("Начать проверку");
        resultArea = new TextArea();
        resultArea.setEditable(false);

        folder1Button.setOnAction(e -> chooseFolder(primaryStage, folder1Button));
        folder2Button.setOnAction(e -> chooseFolder(primaryStage, folder2Button));
        startButton.setOnAction(e -> {
            try {
                compareFolders(folder1Button.getText(), folder2Button.getText());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        VBox vbox = new VBox(10, folder1Button, folder2Button, startButton, resultArea);
        vbox.setPadding(new Insets(10));

        Scene scene = new Scene(vbox, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void chooseFolder(Stage primaryStage, Button folderButton) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(primaryStage);

        if (selectedDirectory != null) {
            folderButton.setText(selectedDirectory.getAbsolutePath());
        }
    }

    private void compareFolders(String folderPath1, String folderPath2) throws IOException {
        if (folderPath1.equals("Выбрать папку 1") || folderPath2.equals("Выбрать папку 2")) {
            resultArea.setText("Пожалуйста, выберите обе папки.");
            return;
        }

        File folder1 = new File(folderPath1);
        File folder2 = new File(folderPath2);

        if (!folder1.isDirectory() || !folder2.isDirectory()) {
            resultArea.setText("Выбранный путь не является папкой.");
            return;
        }

        File[] folder1Files = folder1.listFiles((dir, name) -> {
            String lowerName = name.toLowerCase();
            return lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".png");
        });

        File[] folder2Files = folder2.listFiles((dir, name) -> {
            String lowerName = name.toLowerCase();
            return lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".png");
        });

        if (folder1Files == null || folder2Files == null) {
            resultArea.setText("Не удалось получить файлы из одной из папок.");
            return;
        }

        resultArea.clear();
        for (File file1 : folder1Files) {
            try {
                String hash1 = getHash(file1);

                for (File file2 : folder2Files) {
                    try {
                        String hash2 = getHash(file2);

                        if (hash1.equals(hash2)) {
                            resultArea.appendText("Изображение " + file1.getName() + " из папки " + folderPath1 +
                                    " уже находится в папке " + folderPath2 + " как " + file2.getName() + "\n");
                            break;
                        }
                    } catch (IOException ex) {
                        resultArea.appendText("Не удалось обработать файл " + file2.getName() + ": " + ex.getMessage() + "\n");
                    }
                }
            } catch (IOException ex) {
                resultArea.appendText("Не удалось обработать файл " + file1.getName() + ": " + ex.getMessage() + "\n");
            }
        }

        resultArea.appendText("Проверка завершена.");
    }


    private static String getHash(File file) throws IOException {
        BufferedImage img = ImageIO.read(file);
        if (img == null) {
            throw new IOException("Не удается прочитать изображение: " + file.getAbsolutePath());
        }
        BufferedImage scaledImg = Scalr.resize(img, Scalr.Method.QUALITY, Scalr.Mode.FIT_EXACT, 8, 8);

        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int rgb = scaledImg.getRGB(x, y);
                int gray = (rgb >> 16 & 0xff + rgb >> 8 & 0xff + rgb & 0xff) / 3;
                sb.append(gray);
            }
        }

        return DigestUtils.md5Hex(sb.toString());
    }
}
