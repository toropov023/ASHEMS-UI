package ca.uwo20.ui.page.home;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Author: toropov
 * Date: 3/6/2019
 */
public class RateMeter extends AnchorPane {
    private static final double PEAK_LABEL_FONT = 30;
    private final int width;
    private final int height;
    private final double topHeight;
    private final double rectanglesWidth;
    private final double rectanglesHeight;
    private final double rectanglesPadding;
    private final double rectanglesTop;
    private final Label peakLabel = new Label();
    private final Label priceLabel = new Label();
    private final Label periodLabel = new Label();
    private final List<Node> rectangleParts = new ArrayList<>();

    public RateMeter(int width, int height) {
        setMinWidth(width);
        setMinHeight(height);
        this.width = width;
        this.height = height;
        topHeight = height * 0.3;
        rectanglesWidth = width * 0.9;
        rectanglesHeight = height * 0.08;
        rectanglesPadding = (width - rectanglesWidth) / 2;
        rectanglesTop = height * 0.85;

        //Peak label
        peakLabel.getStyleClass().add("peakRateTitle");
        peakLabel.setAlignment(Pos.CENTER);
        peakLabel.setFont(Font.font(PEAK_LABEL_FONT));

        //Other labels
        priceLabel.getStyleClass().add("ratePriceLabel");
        priceLabel.setAlignment(Pos.CENTER);
        priceLabel.setAlignment(Pos.CENTER);
        AnchorPane.setLeftAnchor(priceLabel, 0D);
        AnchorPane.setRightAnchor(priceLabel, 0D);
        priceLabel.setLayoutY(height * 0.37);

        periodLabel.getStyleClass().add("ratePeriodLabel");
        periodLabel.setAlignment(Pos.CENTER);
        periodLabel.setAlignment(Pos.CENTER);
        AnchorPane.setLeftAnchor(periodLabel, 0D);
        AnchorPane.setRightAnchor(periodLabel, 0D);
        periodLabel.setLayoutY(height * 0.65);
        getChildren().addAll(peakLabel, priceLabel, periodLabel);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, event -> update()),
                new KeyFrame(Duration.seconds(60))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void update() {
        RateType currentRate = getCurrentRate();
        setBackground(new Background(new BackgroundFill(currentRate.color, new CornerRadii(4, 4, 0, 0, false), new Insets(0, 0, height - topHeight, 0))));

        //Update peak label
        peakLabel.setText(currentRate.name);
        Text testText = new Text(peakLabel.getText());
        testText.setFont(Font.font(PEAK_LABEL_FONT));
        AnchorPane.setLeftAnchor(peakLabel, width / 2 - testText.getBoundsInLocal().getWidth() / 2);
        AnchorPane.setTopAnchor(peakLabel, topHeight / 2 - testText.getBoundsInLocal().getHeight() / 2);

        //Other labels
        priceLabel.setText(currentRate.price + " c/kWh");

        //Rectangles
        getChildren().removeAll(rectangleParts);
        rectangleParts.clear();
        List<RateType> dayRates = getDayRates();
        double x = rectanglesPadding;
        int previousHours = 0;
        for (int i = 0; i < dayRates.size(); i++) {
            RateType rateType = dayRates.get(i);
            Region region = new Region();
            CornerRadii radii = null;
            if (i == 0) {
                radii = new CornerRadii(4, 0, 0, 4, false);
            } else if (i == dayRates.size() - 1) {
                radii = new CornerRadii(0, 4, 4, 0, false);
            }
            region.setBackground(new Background(new BackgroundFill(rateType.color, radii, null)));

            double regionWidth = rateType.duration / 24D * rectanglesWidth;
            region.setMinSize(regionWidth, rectanglesHeight);
            region.setLayoutX(x);
            x += regionWidth;
            region.setLayoutY(rectanglesTop);

            rectangleParts.add(region);

            //Period label
            if (rateType.equals(currentRate)) {
                if (rateType.equals(RateType.OFF_PEAK_24)) {
                    periodLabel.setText("weekend");
                } else {
                    String fromHour = previousHours < 10 ? "0" + previousHours : previousHours + "";
                    int to = previousHours + currentRate.duration;
                    if (to == 24) {
                        to = 0;
                    }
                    String toHour = to < 10 ? "0" + to : to + "";
                    periodLabel.setText(fromHour + ":00 - " + toHour + ":00");
                }
            }
            previousHours += rateType.duration;
        }

        //Line
        LocalDateTime now = LocalDateTime.now();
        double lineX = rectanglesPadding + (now.getHour() * 60 + now.getMinute()) / 1440D * rectanglesWidth;
        Line line = new Line(lineX, rectanglesTop - 2, lineX, rectanglesTop + rectanglesHeight + 1);
        line.setStrokeWidth(3);
        line.setStroke(Color.web("#2196F3"));
        rectangleParts.add(line);
        getChildren().addAll(rectangleParts);
    }

    private RateType getCurrentRate() {
        LocalDateTime now = LocalDateTime.now();
        int hour = 0;
        for (RateType rateType : getDayRates()) {
            hour += rateType.duration;
            if (now.getHour() < hour) {
                return rateType;
            }
        }
        return RateType.OFF_PEAK_24;
    }

    private List<RateType> getDayRates() {
        LocalDateTime now = LocalDateTime.now();
        if (now.getDayOfWeek().equals(DayOfWeek.SATURDAY) || now.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            return Collections.singletonList(RateType.OFF_PEAK_24);
        } else {
            return Arrays.asList(Arrays.copyOfRange(RateType.values(), 0, RateType.values().length - 1));
        }
    }

    @RequiredArgsConstructor
    private enum RateType {
        OFF_PEAK_1("OFF PEAK", 6.5, Color.web("#8BC34A"), 7),
        ON_PEAK_1("ON PEAK", 13.2, Color.web("#FF5722"), 4),
        MID_PEAK("MID PEAK", 9.4, Color.web("#FF9800"), 6),
        ON_PEAK_2("ON PEAK", 13.2, Color.web("#FF5722"), 2),
        OFF_PEAK_2("OFF PEAK", 6.5, Color.web("#8BC34A"), 5),
        OFF_PEAK_24("OFF PEAK", 6.5, Color.web("#8BC34A"), 24);

        private final String name;
        private final double price;
        private final Color color;
        private final int duration;
    }
}
