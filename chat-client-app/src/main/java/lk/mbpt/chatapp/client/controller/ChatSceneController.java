package lk.mbpt.chatapp.client.controller;


import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.Duration;
import lk.mbpt.chatapp.shared.EChatHeaders;
import lk.mbpt.chatapp.shared.EChatMessage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ChatSceneController {
    public Label txtUserName;
    public ListView<String> lstUsers;
    public ImageView imgSend;
    public VBox vboxChatArea;
    public AnchorPane rootPane;
    public ImageView imgEmoji;
    public ScrollPane scrollPaneChatArea;
    public VBox textAreaPlaceholder;

    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private String username;
    private WebEngine webEngine;
    private Stage emojiStage;
    private Stage ownerStage;
    private PauseTransition pauseTransition = new PauseTransition(Duration.millis(50)); // Adjust delay if needed

    EmojiListController ctrl = null;

    public void initData(String username) {
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
                return new ListCell<String>() {
                    @Override
                    protected void updateItem(String text, boolean empty) {
                        super.updateItem(text, empty);
                        if (!empty) {
                            setGraphic(new Circle(5, Color.LIMEGREEN));
                            setGraphicTextGap(7.5);
                            setText(text);
                        } else {
                            setGraphic(null);
                            setText(null);
                        }
                    }
                };
            }
        });
    }

    public void initialize() {
        WebView webView = new WebView();
        webEngine = webView.getEngine();
        webEngine.loadContent(getHtmlContent());
        textAreaPlaceholder.getChildren().add(webView);
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
        imgSend.getScene().getWindow().setOnCloseRequest(event -> {
            try {
                if (ctrl != null) ctrl.emojiVBox.getScene().getWindow().hide();
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
                            if (chatHistory.equals("")) return;

                            vboxChatArea.getChildren().clear();

                            String[] textLine = chatHistory.split("\n");
                            for (String txt : textLine) {
                                String[] arr = txt.split(": ");
                                String textOwner = arr[0];
                                txt = arr[1];

                                TextFlow tempFlow = new TextFlow();

                                Text txtName = (!textOwner.equalsIgnoreCase(txtUserName.getText())) ? new Text(textOwner + "\n") : new Text("You\n");
                                txtName.getStyleClass().add("txtName");
                                tempFlow.getChildren().add(txtName);

                                parseHtmlToTextFlow(txt, tempFlow);

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

    public static void parseHtmlToTextFlow(String htmlContent, TextFlow textFlow) {
        Document doc = Jsoup.parse(htmlContent); // Parse the HTML content using JSoup

        // Traverse each node in the HTML
        doc.body().traverse(new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {

                if(node instanceof  Element && ((Element)node).tagName().equals("div")){
                    textFlow.getChildren().add(new Text("\n"));
                }
                if (node instanceof TextNode) {
                    // For plain text nodes, add as Text in TextFlow
                    TextNode textNode = (TextNode) node;
                    Text text = new Text(textNode.text());
                    text.getStyleClass().add("message");
                    textFlow.getChildren().add(text);
                } else if (node instanceof Element) {
                    Element element = (Element) node;
                    if (element.tagName().equals("img") && element.hasAttr("src")) {
                        // For <img> tags, load the image and add as ImageView in TextFlow
                        String imageUrl = element.attr("src");
                        try {
                            Image image = new Image(imageUrl, true);
                            ImageView imageView = new ImageView(image);
                            imageView.setFitHeight(16);  // Adjust emoji/image size as needed
                            imageView.setPreserveRatio(true);
                            textFlow.getChildren().add(imageView);
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Failed to load image from URL: " + imageUrl);
                        }
                    }
                }
            }

            @Override
            public void tail(Node node, int depth) {
                // Optional: Handle closing tags or further processing if needed
            }
        });
    }

    public void imgSendOnMouseClicked(MouseEvent event) {
        try {
            String message = (String) webEngine.executeScript("getContent()");  // Get HTML content from the WebView
            EChatMessage msg = new EChatMessage(EChatHeaders.MSG, message);
            oos.writeObject(msg);
            oos.flush();
            webEngine.executeScript("document.getElementById('editor').innerHTML = '';"); // Clear the content in the WebView and clear the editor

        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to connect to the server, try again").show();
            e.printStackTrace();
        }
    }

    public void imgEmojiOnMouseClicked(MouseEvent event) throws IOException {
        if (emojiStage == null || !emojiStage.isShowing()) {  // Load EmojiList.fxml only if the emoji list is not already showing
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/scene/EmojiList.fxml"));
            Parent root = fxmlLoader.load();

            ctrl = fxmlLoader.getController();
            ctrl.setChatController(this);

            Scene scene = new Scene(root);
            emojiStage = new Stage();
            emojiStage.setWidth(390);
            emojiStage.initStyle(StageStyle.UNDECORATED);
            emojiStage.setResizable(false);
            emojiStage.initOwner(rootPane.getScene().getWindow());
            emojiStage.setScene(scene);

            ownerStage = (Stage) rootPane.getScene().getWindow();

            updateEmojiStagePosition(); // Position emoji list near the imgEmoji icon
            emojiStage.show();

            // Listen for size changes of the chatScene
            ownerStage.widthProperty().addListener((obs, oldVal, newVal) -> updateEmojiStagePosition());
            ownerStage.heightProperty().addListener((obs, oldVal, newVal) -> updateEmojiStagePosition());

            // Move emoji list along with `chatScene`
            ownerStage.xProperty().addListener((obs, oldVal, newVal) -> updateEmojiStagePosition());
            ownerStage.yProperty().addListener((obs, oldVal, newVal) -> updateEmojiStagePosition());

            // Listen for Maximize of the chatScene
            ownerStage.maximizedProperty().addListener((obs, oldVal, newVal) -> {
                pauseTransition.setOnFinished(e -> updateEmojiStagePosition());
                pauseTransition.playFromStart();
            });

        } else {
            // Hide the emoji stage if it's already open
            emojiStage.hide();
            emojiStage = null;
        }
    }

    // Method to update the emoji list position based on `chatScene` and `imgEmoji` position
    private void updateEmojiStagePosition() {
        if (emojiStage == null) return;
        // Position the emoji stage near imgEmoji
        double emojiButtonX = imgEmoji.localToScene(0, 0).getX() + ownerStage.getX();
        double emojiButtonY = imgEmoji.localToScene(0, 0).getY() + ownerStage.getY();

        emojiStage.setX(emojiButtonX - 380);
        emojiStage.setY(emojiButtonY - 235);
    }

    public WebEngine getWebEngine() {
        return webEngine;
    }

    private void controlChatAreaSize() {
        scrollPaneChatArea.layoutBoundsProperty().addListener((obs, previous, current) -> {
            // Resize chat area automatically
            vboxChatArea.setPrefWidth(scrollPaneChatArea.getWidth() - 20);
            vboxChatArea.setPrefHeight(scrollPaneChatArea.getHeight() - 2);
        });
    }

    public String getHtmlContent() {
        return "<!DOCTYPE html>" +
                "<html><head><title>Rich Text Editor</title>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; min-height: 100vh; display: flex; align-items: center; background-color: #383a50; margin: 0; padding-left:5px;} " +
                "#editor { width: 100%; flex: 1; overflow-y: auto; background-color: #cad0db; height:35px; padding: 0px 15px; border-radius:35px; outline: none; box-sizing: border-box; font-size: 20px;} " +
                "#editor::-webkit-scrollbar { display: none;}" + // Hide scrollbar
                "#editor { scrollbar-width: none;}" + // Hide scrollbar for Firefox
                ".emoji { width: 20px; height: 20px; vertical-align: middle;}" +
                "</style>" +
                "</head><body>" +
                "    <div id=\"editor\" contenteditable=\"true\"></div>\n" +
                "    <script>\n" +
                "        function insertEmoji(imgUrl) {\n" +
                "           var imgTag = '<img src=\"' + imgUrl + '\" class=\"emoji\">';" +
                "           document.execCommand('insertHTML', false, imgTag);"+ // Insert the emoji at the current cursor position
                "        }\n" +
                "        function getContent() {\n" +
                "            return document.getElementById('editor').innerHTML;\n" + // Return the HTML content of the editor, including emojis
                "        }\n" +
                "    </script>\n" +
                "</body></html>";
    }

}
