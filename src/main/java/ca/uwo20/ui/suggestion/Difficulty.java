package ca.uwo20.ui.suggestion;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Author: toropov
 * Date: 3/12/2019
 */
@Getter
@RequiredArgsConstructor
public enum Difficulty {
    VERY_EASY("Very easy", Color.web("#8BC34A")),
    EASY("Easy", Color.web("#4CAF50")),
    MODERATE("Moderate", Color.web("#FF9800")),
    HARD("Hard", Color.web("#FF5722"));

    private final String name;
    private final Color color;

    public StackPane getPane() {
        Label label = new Label(name);
        label.setAlignment(Pos.CENTER);
        label.getStyleClass().add("suggestionDifficultyLabel");
        StackPane pane = new StackPane(label);
        pane.setBackground(new Background(new BackgroundFill(color, new CornerRadii(5), new Insets(5))));

        StackPane.setAlignment(label, Pos.CENTER);
        return pane;
    }
}
