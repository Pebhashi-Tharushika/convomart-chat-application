package lk.mbpt.chatapp.client.controller;


import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import lk.mbpt.chatapp.shared.EChatHeaders;
import lk.mbpt.chatapp.shared.EChatMessage;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ChatSceneController {
    public Label txtUserName;
    public ListView<String> lstUsers;
    public TextField txtMsg;
    public ImageView imgSend;
    public VBox vboxChatArea;
    public AnchorPane rootPane;
    public ImageView imgEmoji;
    public ScrollPane scrollPaneChatArea;

    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private String username;
    emojiSceneController ctrl = null;

    public void initData(String username){
        this.username = username;
        txtUserName.setText(username);
        controlChatAreaSize();
        connect();
        readServerResponses();
        Platform.runLater(() -> closeSocketOnStageCloseRequest());

        /* disable user selection of list view */
        lstUsers.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                Platform.runLater(new Runnable() {
                    public void run() {
                        lstUsers.getSelectionModel().select(-1);

                    }
                });

            }
        });

        /* Customize the default cell factory of the list view */
        lstUsers.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> stringListView) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(String text, boolean empty) {
                        super.updateItem(text, empty);
                        if (!empty) {
                            setGraphic(new Circle(5, Color.LIMEGREEN));
                            setGraphicTextGap(7.5);
                            setText(text);
                        }else {
                            setGraphic(null);
                            setText(null);
                        }
                    }
                };
            }
        });
    }

    private void connect() {
        try {
            socket = new Socket("127.0.0.1", 5050);

            /* Before starting to read from the ObjectInputStream from the server side, setup the ObjectOutputStream first from the client side too */
            oos = new ObjectOutputStream(socket.getOutputStream());
            /* send the newly logged user */
            EChatMessage msg = new EChatMessage(EChatHeaders.USERNAME, username);
            oos.writeObject(msg);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to connect to the server").showAndWait();
            Platform.exit();
        }
    }

    private void closeSocketOnStageCloseRequest() {
        txtMsg.getScene().getWindow().setOnCloseRequest(event -> {
            try {
                if(ctrl!=null) ctrl.pane.getScene().getWindow().hide();
                oos.writeObject(new EChatMessage(EChatHeaders.EXIT, null));
                oos.flush();
                if (!socket.isClosed()) socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void readServerResponses() {
        new Thread(() -> {
            try {
                ois = new ObjectInputStream(socket.getInputStream());

                while (true) {
                    EChatMessage msg = (EChatMessage) ois.readObject();
                    if (msg.getHeader() == EChatHeaders.USERS) { // Display logged users
                        ArrayList<String> loggedUserList = (ArrayList<String>) msg.getBody();
                        Platform.runLater(() -> {
                            lstUsers.getItems().clear();
                            lstUsers.getItems().addAll(loggedUserList);
                        });
                    } else if (msg.getHeader() == EChatHeaders.MSG) { // Display chat history
                        Platform.runLater(() -> {
                            String chatHistory = msg.getBody().toString();
                            if(chatHistory.equals("")) return;

                            vboxChatArea.getChildren().clear();

                            String[] textLine = chatHistory.split("\n");
                            for (String txt : textLine) {
                                String[] arr = txt.split(": ");
                                String textOwner = arr[0];
                                txt = arr[1];

                                Text text = new Text(txt);
                                text.getStyleClass().add("message");
                                Text txtName = (!textOwner.equalsIgnoreCase(txtUserName.getText())) ? new Text(textOwner + "\n") : new Text("You\n") ;
                                txtName.getStyleClass().add("txtName");

                                TextFlow tempFlow = new TextFlow();
                                tempFlow.getChildren().add(txtName);
                                tempFlow.getChildren().add(text);
                                tempFlow.setMaxWidth(200);

                                TextFlow flow = new TextFlow(tempFlow);

                                HBox hBox = new HBox(12);
                                hBox.setPrefWidth(vboxChatArea.getPrefWidth());
                                hBox.prefWidthProperty().bind(vboxChatArea.widthProperty());


                                if (!textOwner.equalsIgnoreCase(txtUserName.getText())) {
                                    tempFlow.getStyleClass().add("tempFlowFlipped");
                                    flow.getStyleClass().add("textFlowFlipped");
                                    vboxChatArea.setAlignment(Pos.TOP_LEFT);
                                    hBox.setAlignment(Pos.CENTER_LEFT);
                                    hBox.getChildren().add(flow);

                                } else {
                                    tempFlow.getStyleClass().add("tempFlow");
                                    flow.getStyleClass().add("textFlow");
                                    hBox.setAlignment(Pos.CENTER_RIGHT);
                                    hBox.getChildren().add(flow);
                                }
                                hBox.getStyleClass().add("hbox");
                                Platform.runLater(() -> vboxChatArea.getChildren().addAll(hBox));
                            }

                        });
                    }
                }
            } catch (Exception e) {
                if (e instanceof EOFException) {
                    Platform.runLater(() -> {
                        new Alert(Alert.AlertType.ERROR, "Connection lost, try again!").showAndWait();
                        Platform.exit();
                    });
                } else if (!socket.isClosed()) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void txtMsgOnAction(ActionEvent event) {
        try {
            EChatMessage msg = new EChatMessage(EChatHeaders.MSG, txtMsg.getText());
            oos.writeObject(msg);
            oos.flush();
            txtMsg.textProperty().unbind();
            txtMsg.clear();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to connect to the server, try again").show();
            e.printStackTrace();
        }
    }

    public void imgSendOnMouseClicked(MouseEvent event) {
        txtMsg.fireEvent(new ActionEvent());
    }


    public void imgEmojiOnMouseClicked(MouseEvent event) throws IOException {
        if(ctrl ==null){
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/scene/emojiScene.fxml"));
            Parent root = fxmlLoader.load();

            ctrl = fxmlLoader.getController();
            SimpleStringProperty observable = new SimpleStringProperty(txtMsg.getText());
            txtMsg.textProperty().bind(observable);

            ctrl.initData(observable);

            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(scene);

            stage.setX(event.getScreenX() - 625);
            stage.setY(event.getScreenY() - 150);

            stage.show();
        }else{
            ctrl.pane.getScene().getWindow().hide();
            ctrl = null;
        }

    }

    private void controlChatAreaSize(){
        scrollPaneChatArea.layoutBoundsProperty().addListener((obs, previous, current) -> {
            // Resize chat area automatically
            vboxChatArea.setPrefWidth(scrollPaneChatArea.getWidth()-20);
            vboxChatArea.setPrefHeight(scrollPaneChatArea.getHeight()-2);
        });
    }
}
