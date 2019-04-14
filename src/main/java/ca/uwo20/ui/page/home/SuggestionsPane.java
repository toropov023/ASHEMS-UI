package ca.uwo20.ui.page.home;

import ca.uwo20.ui.suggestion.SavingsPeriod;
import ca.uwo20.ui.suggestion.Suggestion;
import com.jfoenix.controls.JFXButton;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Author: toropov
 * Date: 3/12/2019
 */
public class SuggestionsPane extends GridPane {
    private final static Insets PADDING = new Insets(30, 10, 10, 10);
    private final static double DIFFICULTY_COLUMN_WIDTH = 95;
    private final static double SAVINGS_COLUMN_WIDTH = 115;
    private final static double ROW_HEIGHT = 32;
    private final int listSize;
    private SortBy sortBy = SortBy.SAVINGS;
    private boolean sortByInverted = true;
    private List<Suggestion> suggestionList;
    private List<Node> dynamicChildren = new ArrayList<>();

    public SuggestionsPane(double width, double height, int listSize) {
        this.listSize = listSize;
        setMinHeight(height);
        setMaxHeight(height);
        setMinWidth(width);
        setMaxWidth(width);
        setPadding(PADDING);

        //Setup the columns
        double suggestionsWidth = width - SAVINGS_COLUMN_WIDTH - DIFFICULTY_COLUMN_WIDTH - PADDING.getLeft() - PADDING.getRight();
        getColumnConstraints().add(new ColumnConstraints(suggestionsWidth));
        getColumnConstraints().add(new ColumnConstraints(DIFFICULTY_COLUMN_WIDTH));
        getColumnConstraints().add(new ColumnConstraints(SAVINGS_COLUMN_WIDTH));
        for (int i = 0; i <= listSize; i++) {
            getRowConstraints().add(new RowConstraints(ROW_HEIGHT));
        }

        //Lines
        addLine(0, ROW_HEIGHT, width - PADDING.getRight() - PADDING.getLeft(), ROW_HEIGHT, 0, 0, 3, 1);
        addLine(suggestionsWidth, 0, suggestionsWidth, ROW_HEIGHT * (listSize + 1), 0, 0, 1, listSize + 1);
        addLine(DIFFICULTY_COLUMN_WIDTH, 0, DIFFICULTY_COLUMN_WIDTH, ROW_HEIGHT * (listSize + 1), 1, 0, 1, listSize + 1);

        //Setup the top bar buttons
        add(createButton("Suggestion", suggestionsWidth, SortBy.NAME, HPos.LEFT), 0, 0);
        add(createButton("Difficulty", DIFFICULTY_COLUMN_WIDTH, SortBy.DIFFICULTY, HPos.CENTER), 1, 0);
        add(createButton("Year savings", SAVINGS_COLUMN_WIDTH, SortBy.SAVINGS, HPos.CENTER), 2, 0);
    }

    private void addLine(double startX, double startY, double endX, double endY, int columnIndex, int rowIndex, int colspan, int rowspan) {
        Line line = new Line(startX, startY, endX, endY);
        line.setStroke(Color.DARKGRAY);
        add(new Pane(line), columnIndex, rowIndex, colspan, rowspan);
    }

    private JFXButton createButton(String name, double width, SortBy sortBy, HPos hPos) {
        JFXButton button = new JFXButton(name);
        button.getStyleClass().add("suggestionHeadLabel");
        button.setOnMouseClicked(event -> setSortBy(sortBy));
        button.setPrefWidth(width);
        button.setDisableVisualFocus(true);
        button.setRipplerFill(Color.SKYBLUE);
        GridPane.setHalignment(button, hPos);

        return button;
    }

    public void setSortBy(SortBy sortBy) {
        if (this.sortBy.equals(sortBy)) {
            sortByInverted = !sortByInverted;
        } else {
            sortByInverted = false;
        }
        this.sortBy = sortBy;

        update();
    }

    public void setSuggestionList(List<Suggestion> suggestionList) {
        this.suggestionList = suggestionList;
        update();
    }

    private void update() {
        suggestionList.sort(sortByInverted ? sortBy.comparator.reversed() : sortBy.comparator);

        getChildren().removeAll(dynamicChildren);
        dynamicChildren.clear();

        for (int i = 0; i < suggestionList.size() && i < listSize; i++) {
            Suggestion suggestion = suggestionList.get(i);

            Label name = new Label(suggestion.getSuggestion());
            name.getStyleClass().add("suggestionNameLabel");
            addChild(name, 0, i + 1);

            StackPane difficulty = suggestion.getDifficulty().getPane();
            addChild(difficulty, 1, i + 1);

            Label savings = new Label(suggestion.getSavings(SavingsPeriod.YEAR, false));
            GridPane.setHalignment(savings, HPos.CENTER);
            savings.getStyleClass().add("suggestionSavingLabel");
            addChild(savings, 2, i + 1);
        }
    }

    private void addChild(Node node, int columnIndex, int rowIndex) {
        addChild(node, columnIndex, rowIndex, 1, 1);
    }

    private void addChild(Node node, int columnIndex, int rowIndex, int colspan, int rowspan) {
        dynamicChildren.add(node);
        add(node, columnIndex, rowIndex, colspan, rowspan);
    }

    @RequiredArgsConstructor
    private enum SortBy {
        NAME(Comparator.comparing(Suggestion::getSuggestion)),
        DIFFICULTY(Comparator.comparing(Suggestion::getDifficulty)),
        SAVINGS(Comparator.comparingDouble(Suggestion::getHourSavings));

        private final Comparator<Suggestion> comparator;
    }
}
