package ca.uwo20.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {
    public static boolean isRunning = true;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Font.loadFont(Main.class.getResource("/font/roboto/Roboto-Regular.ttf").toExternalForm(), 10);
        Font.loadFont(Main.class.getResource("/font/roboto/Roboto-Italic.ttf").toExternalForm(), 10);
        Font.loadFont(Main.class.getResource("/font/roboto/Roboto-Bold.ttf").toExternalForm(), 10);

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
        root.getStylesheets().add("/css/main.css");
        primaryStage.setTitle("Power Hour");
        Scene scene = new Scene(root, 800, 480);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreen(true);
        if (!System.getProperty("os.name").equals("Windows 10")) {
            scene.setCursor(Cursor.NONE);
        }
    }

    @Override
    public void stop() {
        isRunning = false;
    }
}
