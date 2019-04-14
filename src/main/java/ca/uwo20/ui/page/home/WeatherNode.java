package ca.uwo20.ui.page.home;

import ca.uwo20.ui.util.CardStyle;
import ca.uwo20.ui.weather.WeatherAPI;
import ca.uwo20.ui.weather.WeatherDay;
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.weathericons.WeatherIcon;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

/**
 * Author: toropov
 * Date: 3/4/2019
 */
public class WeatherNode extends GridPane {
    public WeatherNode() {
        CardStyle.apply(this);
        WeatherAPI.getI().addUpdateConsumer(this::update);
    }

    private void update(List<WeatherDay> weatherDays) {
        if (weatherDays.isEmpty())
            return;
        getChildren().clear();

        WeatherDay today = weatherDays.get(0);

        VBox iconPane = new VBox(createIcon(today.getIcon(), Color.WHITESMOKE, 40));
        iconPane.setAlignment(Pos.CENTER);
        iconPane.setBackground(new Background(new BackgroundFill(Color.SKYBLUE, new CornerRadii(4, 0, 0, 0, false), null)));
        iconPane.setPrefWidth(80);
        iconPane.setPrefHeight(80);
        add(iconPane, 0, 0);

        Label degrees = new Label(today.getCurrentTemperature() + "°");
        degrees.setStyle("-fx-font-size: " + 30);
        degrees.setTextFill(Color.GRAY);
        Label condition = new Label(today.getCondition().toUpperCase());
        condition.setStyle("-fx-font-size: " + 20);
        condition.setTextFill(Color.GRAY);
        VBox conditionBox = new VBox(degrees, condition);
        conditionBox.setAlignment(Pos.CENTER);
        GridPane.setHgrow(conditionBox, Priority.ALWAYS);
        add(conditionBox, 1, 0);

        Line line = new Line(1, 80, 199, 80);
        line.setStroke(Color.SKYBLUE);
        add(new Pane(line), 0, 0, 2, 1);

        //The forecast
        for (int i = 1; i < Math.min(weatherDays.size(), 6); i++) {
            add(createForecastDay(weatherDays.get(i)), 0, i, 2, 1);
        }
    }

    private HBox createForecastDay(WeatherDay day) {
        Label dayOfWeek = new Label(day.getDate().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.CANADA).toUpperCase());
        dayOfWeek.setStyle("-fx-font-size: " + 18);
        dayOfWeek.setTextFill(Color.GRAY);
        dayOfWeek.setMinWidth(60);
        HBox.setHgrow(dayOfWeek, Priority.ALWAYS);

        Label icon = createIcon(day.getIcon(), Color.GRAY, 18);
        icon.setMinWidth(30);

        Label dayTemp = new Label(day.getDayTemperature() + "°");
        dayTemp.setStyle("-fx-font-size: " + 18);
        dayTemp.setTextFill(Color.GRAY);
        dayTemp.setMinWidth(30);

        Label nightTemp = new Label(day.getNightTemperature() + "°");
        nightTemp.setStyle("-fx-font-size: " + 18);
        nightTemp.setTextFill(Color.DIMGRAY);
        nightTemp.setMinWidth(30);

        Label spacer = new Label("/");
        spacer.setStyle("-fx-font-size: " + 18);
        spacer.setTextFill(Color.GRAY);
        spacer.setMinWidth(5);

        HBox hBox = new HBox(dayOfWeek, icon, dayTemp, spacer, nightTemp);
        hBox.setMinHeight(38);
        hBox.setAlignment(Pos.CENTER);
        return hBox;
    }

    private Label createIcon(WeatherIcon weatherIcon, Color fillColor, int size) {
        Text graphic = GlyphsDude.createIcon(weatherIcon);
        graphic.setFill(fillColor);
        Label icon = new Label();
        icon.setGraphic(graphic);
        icon.setStyle("-fx-font-size: " + size + "; -fx-text-fill: #fff");
        return icon;
    }
}
