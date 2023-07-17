package lk.mbpt.chatapp.client.controller;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import java.util.Collection;

public class emojiSceneController {
    public GridPane pane;

    private SimpleStringProperty observable;
    private String msg;
    private int row = 0;
    private int column = 0;

    public void initData(SimpleStringProperty observable){
        this.observable = observable;
        msg = observable == null ? "" : observable.getValue();
    }

    public void initialize(){
        setEmoji();
    }


    public void setEmoji() {
        Collection<Emoji> emojis = EmojiManager.getAll();
        Emoji[] emojiArray = emojis.toArray(new Emoji[0]);
        System.out.println(emojiArray.length);

        for (Emoji emoji : emojiArray) {
            if(emoji.getUnicode().isEmpty()) return;
            Label emojiLabel = new Label(emoji.getUnicode());
            emojiLabel.setStyle("-fx-font-size: " + 28 + "px;");
            emojiLabel.setFont(Font.font("Noto Color Emoji"));
            pane.add(emojiLabel, column, row);
            column++;
            if (column == 15) {
                column = 0;
                row++;
            }
            emojiLabel.setOnMouseClicked(e -> {
                msg += emojiLabel.getText();
                observable.setValue(msg);

                pane.getScene().getWindow().hide();
            });
        }

    }
}
