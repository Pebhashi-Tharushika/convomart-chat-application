package lk.mbpt.chatapp.client.controller;

import javafx.scene.layout.VBox;
import lk.mbpt.chatapp.client.Emoji;
import lk.mbpt.chatapp.client.EmojiParser;
import lk.mbpt.chatapp.client.EmojiImageCache;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;


/**
 * some code of this file is written by UltimateZero and Pavlo Buidenkov
 * UltimateZero github page: https://github.com/UltimateZero
 * Pavlo Buidenkov github page: https://github.com/pavlobu
 * and
 * modified by Pebhashi Tharushika
 */

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EmojiListController {
    @FXML
    private ScrollPane searchScrollPane;
    @FXML
    private FlowPane searchFlowPane;
    @FXML
    private TabPane tabPane;
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<Image> boxTone;
    private ChatSceneController chatController;
    public VBox emojiVBox;


    @FXML
    void initialize() {
        setupToneSelection();
        setupEmojiSearch();
        setupTabs();
        boxTone.getSelectionModel().select(5);
        tabPane.getSelectionModel().select(1);
    }

    private void setupToneSelection() {
        ObservableList<Image> tonesList = FXCollections.observableArrayList();
        for (int i = 1; i <= 5; i++) {
            Emoji emoji = EmojiParser.getInstance().getEmoji(":thumbsup_tone" + i + ":");
            Image image = EmojiImageCache.getInstance().getImage(getEmojiImagePath(emoji.getHex()));
            tonesList.add(image);
        }
        Emoji em = EmojiParser.getInstance().getEmoji(":thumbsup:"); //default tone
        Image image = EmojiImageCache.getInstance().getImage(getEmojiImagePath(em.getHex()));
        tonesList.add(image);

        boxTone.setItems(tonesList);
        boxTone.setCellFactory(e -> new ToneCell());
        boxTone.setButtonCell(new ToneCell());
        boxTone.getSelectionModel().selectedItemProperty().addListener(e -> refreshTabs());
    }

    private void setupEmojiSearch() {
        searchScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        searchFlowPane.prefWidthProperty().bind(searchScrollPane.widthProperty().subtract(5));
        searchFlowPane.setHgap(5);
        searchFlowPane.setVgap(5);

        txtSearch.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.length() < 2) {
                searchFlowPane.getChildren().clear();
                searchScrollPane.setVisible(false);
            } else {
                searchScrollPane.setVisible(true);
                List<Emoji> results = EmojiParser.getInstance().search(newText);
                searchFlowPane.getChildren().setAll(createFoundEmojiNodes(results));
            }
        });
    }

    private List<Node> createFoundEmojiNodes(List<Emoji> emojis) {
        return emojis.stream().map(this::createEmojiNode).collect(Collectors.toList());
    }

    private void setupTabs() {
        tabPane.getTabs().forEach(tab -> {
            ScrollPane scrollPane = (ScrollPane) tab.getContent();
            FlowPane pane = (FlowPane) scrollPane.getContent();
            pane.setPadding(new Insets(5));
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            pane.prefWidthProperty().bind(scrollPane.widthProperty().subtract(5));
            pane.setHgap(5);
            pane.setVgap(5);

            tab.setId(tab.getText());
            ImageView icon = new ImageView(getTabIcon(tab.getText().toLowerCase()));
            icon.setFitWidth(20);
            icon.setFitHeight(20);

            if (icon.getImage() != null) {
                tab.setText("");
                tab.setGraphic(icon);
            }
            tab.setTooltip(new Tooltip(tab.getId()));
            tab.selectedProperty().addListener(e -> {
                if (tab.getGraphic() == null) return;
                tab.setText(tab.isSelected() ? tab.getId() : "");
            });
        });
    }

    // retrieve appropriate tab icon image based on tab name.
    private Image getTabIcon(String tabName) {
        Image image = null;
        switch (tabName) {
            case "frequently used":
                image = EmojiImageCache.getInstance().getImage(getEmojiImagePath(EmojiParser.getInstance().getEmoji(":heart:").getHex()));
                break;
            case "people":
                image = EmojiImageCache.getInstance().getImage(getEmojiImagePath(EmojiParser.getInstance().getEmoji(":smiley:").getHex()));
                break;
            case "nature":
                image = EmojiImageCache.getInstance().getImage(getEmojiImagePath(EmojiParser.getInstance().getEmoji(":dog:").getHex()));
                break;
            case "food":
                image = EmojiImageCache.getInstance().getImage(getEmojiImagePath(EmojiParser.getInstance().getEmoji(":apple:").getHex()));
                break;
            case "activity":
                image = EmojiImageCache.getInstance().getImage(getEmojiImagePath(EmojiParser.getInstance().getEmoji(":soccer:").getHex()));
                break;
            case "travel":
                image = EmojiImageCache.getInstance().getImage(getEmojiImagePath(EmojiParser.getInstance().getEmoji(":airplane:").getHex()));
                break;
            case "objects":
                image = EmojiImageCache.getInstance().getImage(getEmojiImagePath(EmojiParser.getInstance().getEmoji(":bulb:").getHex()));
                break;
            case "symbols":
                image = EmojiImageCache.getInstance().getImage(getEmojiImagePath(EmojiParser.getInstance().getEmoji(":atom:").getHex()));
                break;
            case "flags":
                image = EmojiImageCache.getInstance().getImage(getEmojiImagePath(EmojiParser.getInstance().getEmoji(":flag_eg:").getHex()));
                break;
        }
        return image;
    }

    public void setChatController(ChatSceneController chatController) {
        this.chatController = chatController;
    }

    private Node createEmojiNode(Emoji emoji) {
        StackPane stackPane = new StackPane();
        stackPane.setMaxSize(32, 32);
        stackPane.setPrefSize(32, 32);
        stackPane.setMinSize(32, 32);
        stackPane.setPadding(new Insets(3));

        ImageView imageView = new ImageView();
        imageView.setFitWidth(32);
        imageView.setFitHeight(32);
        imageView.setImage(EmojiImageCache.getInstance().getImage(getEmojiImagePath(emoji.getHex())));

        stackPane.getChildren().add(imageView);

        Tooltip tooltip = new Tooltip(emoji.getShortname());
        Tooltip.install(stackPane, tooltip);
        stackPane.setCursor(Cursor.HAND);
        ScaleTransition st = new ScaleTransition(Duration.millis(90), imageView);

        stackPane.setOnMouseEntered(e -> {
            imageView.setEffect(new DropShadow());
            st.setToX(1.2);
            st.setToY(1.2);
            st.playFromStart();
            if (txtSearch.getText().isEmpty())
                txtSearch.setPromptText(emoji.getShortname());
        });
        stackPane.setOnMouseExited(e -> {
            imageView.setEffect(null);
            st.setToX(1.);
            st.setToY(1.);
            st.playFromStart();
        });

        stackPane.setOnMouseClicked(e -> {
            String emojiImagePath = getEmojiImagePath(emoji.getHex()); // Get the image path for the emoji
            if (emojiImagePath == null) {
                System.err.println("Failed to get emoji image path.");
                return;
            }
            // Use chatController’s WebEngine to insert the emoji
            chatController.getWebEngine().executeScript("insertEmoji('" + emojiImagePath + "')");

        });

        return stackPane;
    }

    private void refreshTabs() {
        Map<String, List<Emoji>> map = EmojiParser.getInstance().getCategorizedEmojis(boxTone.getSelectionModel().getSelectedIndex() + 1);
        for (Tab tab : tabPane.getTabs()) {
            ScrollPane scrollPane = (ScrollPane) tab.getContent();
            FlowPane pane = (FlowPane) scrollPane.getContent();
            pane.getChildren().clear();
            String category = tab.getId().toLowerCase();
            if (map.get(category) == null) continue;
            map.get(category).forEach(emoji -> pane.getChildren().add(createEmojiNode(emoji)));
        }
    }

    private String getEmojiImagePath(String hexStr) {
        URL resource = this.getClass().getResource("/emoji_images/" + hexStr + ".png");
        return resource.toExternalForm();
    }

    class ToneCell extends ListCell<Image> {
        private final ImageView imageView;

        public ToneCell() {
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            imageView = new ImageView();
            imageView.setFitWidth(20);
            imageView.setFitHeight(20);
        }

        @Override
        protected void updateItem(Image item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null || empty) {
                setText(null);
                setGraphic(null);
            } else {
                imageView.setImage(item);
                setGraphic(imageView);
            }
        }
    }


}