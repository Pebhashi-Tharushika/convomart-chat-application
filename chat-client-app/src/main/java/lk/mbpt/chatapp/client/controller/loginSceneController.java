package lk.mbpt.chatapp.client.controller;

import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;


public class loginSceneController {

    public TextField txtUsername;
    public JFXButton btnGo;

    public void btnGoOnAction(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/scene/ChatScene.fxml"));
        AnchorPane root = fxmlLoader.load();

        ChatSceneController ctrl = fxmlLoader.getController();
        ctrl.initData(txtUsername.getText().trim());

        Scene chatScene = new Scene(root);
        Stage stage = new Stage();
        stage.setScene(chatScene);
        stage.setTitle("Chat App");
        stage.show();
        stage.centerOnScreen();
        btnGo.getScene().getWindow().hide();
    }

}
