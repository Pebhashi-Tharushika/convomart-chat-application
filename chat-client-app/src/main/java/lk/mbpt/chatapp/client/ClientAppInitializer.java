package lk.mbpt.chatapp.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientAppInitializer extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/scene/LoginScene.fxml"));
        BorderPane root = fxmlLoader.load();
        Scene chatScene = new Scene(root);
        primaryStage.setScene(chatScene);
        primaryStage.setTitle("ConvoMart");
        primaryStage.show();
        primaryStage.centerOnScreen();
    }
}
