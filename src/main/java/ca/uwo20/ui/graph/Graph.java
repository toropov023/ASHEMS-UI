package ca.uwo20.ui.graph;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import lombok.Setter;

import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Author: toropov
 * Date: 3/2/2019
 */
public class Graph extends AnchorPane {
    private final static Color CURVE_COLOR = Color.web("#2196F3");
    private final static Color CURVE_LINE_COLOR = Color.DIMGRAY;
    private final static int BOTTOM_PADDING = 25;
    private final static int TOP_PADDING = 40;
    private final static int LEFT_PADDING = 15;
    private final static int RIGHT_PADDING = 15;
    private final List<GraphPoint> dataPoints = new ArrayList<>();
    private final int barHeight;
    private final int pointWidth;
    private final int pointPadding;
    private final int width;
    @Setter
    private int dateLabelFont = 14;
    @Setter
    private AxisType axisType = AxisType.DATE;
    @Setter
    private int skipEvery = 1;
    @Setter
    private boolean showSuggestedLine;
    @Setter
    private Color underColor = Color.SKYBLUE;
    @Setter
    private Color criticalColor = Color.SKYBLUE;
    @Setter
    private Color overColor = Color.SKYBLUE;
    @Setter
    private double suggestedDataCriticalPercent = 1.1;

    private Timeline currentBarsAnimation;
    private List<Node> graphChildren = new ArrayList<>();

    public Graph(String title, int width, int height, int maxPoints) {
        this.width = width;
        setMinHeight(height);
        setMaxHeight(height);
        setMinWidth(width);
        setMaxWidth(width);
        barHeight = height - TOP_PADDING - BOTTOM_PADDING;

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("graphTitle");
        titleLabel.setAlignment(Pos.CENTER);
        AnchorPane.setLeftAnchor(titleLabel, 0.0);
        AnchorPane.setRightAnchor(titleLabel, 0.0);
        getChildren().add(titleLabel);

        int pointEstate = (width - LEFT_PADDING - RIGHT_PADDING) / maxPoints;
        pointWidth = (int) (pointEstate * 0.9D);
        pointPadding = pointEstate - pointWidth;

        Line bottomLine = new Line(LEFT_PADDING, height - BOTTOM_PADDING + 1, width - RIGHT_PADDING, height - BOTTOM_PADDING + 1);
        bottomLine.setFill(Color.LIGHTGRAY);
        Line leftLine = new Line(LEFT_PADDING, TOP_PADDING / 2, LEFT_PADDING, height - BOTTOM_PADDING);
        leftLine.setFill(Color.LIGHTGRAY);
        getChildren().add(bottomLine);
        getChildren().add(leftLine);
    }

    public void add(GraphPoint point) {
        Rectangle r = point.getRectangle();
        r.setWidth(pointWidth);
        r.setX(LEFT_PADDING + pointPadding + dataPoints.size() * (pointWidth + pointPadding));
        r.setY(barHeight + TOP_PADDING);

        Label dateLabel = point.getDateLabel();
        dateLabel.getStyleClass().add("graphDateLabel");
        dateLabel.setFont(Font.font(dateLabelFont));

        dataPoints.add(point);
        addChild(point.getRectangle());
        addChild(point.getDateLabel());
    }

    private void addChild(Node node) {
        graphChildren.add(node);
        getChildren().add(node);
    }

    public void update(boolean fromBottom) {
        getChildren().removeAll(graphChildren);
        graphChildren.clear();

        double highest = dataPoints.stream()
                .mapToDouble(p -> Math.max(p.getSuggestedData(), p.getData()))
                .max().orElse(0);
        dataPoints.forEach(point -> {
            addChild(point.getRectangle());
            addChild(point.getDateLabel());
            if (fromBottom) {
                point.getRectangle().setHeight(0);
                point.getRectangle().setY(barHeight + TOP_PADDING);
            }
        });
        if (currentBarsAnimation != null) {
            currentBarsAnimation.stop();
        }

        Path bestFitPath = new Path();
        KeyValue[] keyValues = new KeyValue[dataPoints.size() * 2];
        double slope = 0;
        double controlLength = pointWidth / 4;
        for (int i = 0; i < dataPoints.size(); i++) {
            GraphPoint point = dataPoints.get(i);

            //Rectangle color
            if (showSuggestedLine) {
                double data = point.getData();
                double suggested = point.getSuggestedData();
                double critical = suggested * suggestedDataCriticalPercent;
                point.getRectangle().setFill(data < suggested ? underColor : (data > critical ? overColor : criticalColor));
            }

            //Setup the labels
            if (i % skipEvery == 0) {
                Label dateLabel = point.getDateLabel();
                switch (axisType) {
                    case DATE:
                        dateLabel.setText(point.getDateTime().getMonth().getDisplayName(TextStyle.SHORT, Locale.CANADA) + " " + point.getDateTime().getDayOfMonth());
                        break;
                    case DAY_OF_WEEK:
                        dateLabel.setText(point.getDateTime().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.CANADA));
                        break;
                    case HOUR: {
                        int hour = point.getDateTime().getHour();
                        dateLabel.setText(((hour + 11) % 12) + 1 + "" + (hour / 12 == 0 ? "am" : "pm"));
                        break;
                    }
                    case HOUR_MINUTE: {
                        String hour = point.getDateTime().getHour() < 10 ? "0" + point.getDateTime().getHour() : point.getDateTime().getHour() + "";
                        String minute = point.getDateTime().getMinute() < 10 ? "0" + point.getDateTime().getMinute() : point.getDateTime().getMinute() + "";
                        dateLabel.setText(hour + ":" + minute);
                    }
                }

                Text testText = new Text(dateLabel.getText());
                testText.setFont(Font.font(dateLabelFont));
                double labelWidth = testText.getBoundsInLocal().getWidth();
                double labelHeight = testText.getBoundsInLocal().getHeight();
                dateLabel.setLayoutX(LEFT_PADDING + (i + 1) * pointPadding + (i + .5) * (pointWidth) - labelWidth / 2);
                dateLabel.setLayoutY(barHeight + TOP_PADDING + BOTTOM_PADDING / 2 - labelHeight / 2);
            }

            //Setup animation
            double targetHeight = point.getData() / highest * (double) barHeight;
            keyValues[i] = new KeyValue(point.getRectangle().heightProperty(), targetHeight, Interpolator.EASE_OUT);
            keyValues[dataPoints.size() * 2 - i - 1] = new KeyValue(point.getRectangle().yProperty(), barHeight + TOP_PADDING - targetHeight, Interpolator.EASE_OUT);

            //Setup suggested line
            if (showSuggestedLine) {
                double y = dataPoints.get(i).getSuggestedData() / highest * (double) barHeight;
                double nextY = i == dataPoints.size() - 1 ? y : dataPoints.get(i + 1).getSuggestedData() / highest * (double) barHeight;
                double nextSlope = i >= dataPoints.size() - 2 ? 0 : (dataPoints.get(i + 2).getSuggestedData() / highest * (double) barHeight - y) / (pointPadding + pointWidth);

                if (i == 0) {
                    MoveTo moveTo = new MoveTo(LEFT_PADDING + pointPadding + pointWidth / 2, TOP_PADDING + barHeight - y);
                    moveTo.setAbsolute(true);
                    bestFitPath.getElements().add(moveTo);
                }

                double x = LEFT_PADDING + (i + 1) * pointPadding + (i + .5) * pointWidth;
                double nextX = i == dataPoints.size() - 1 ? x : x + pointPadding + pointWidth;
                CubicCurveTo cct = new CubicCurveTo(
                        x + controlLength, TOP_PADDING + barHeight - (y + (slope * controlLength)),
                        nextX - controlLength, TOP_PADDING + barHeight - (nextY - (nextSlope * controlLength)),
                        nextX, TOP_PADDING + barHeight - nextY);
                cct.setAbsolute(true);
                bestFitPath.getElements().add(cct);

                Line line = new Line(x - (pointWidth / 2 - 1), TOP_PADDING + barHeight - y, x + (pointWidth / 2 - 1), TOP_PADDING + barHeight - y);
                line.setStrokeWidth(1);
                line.setStrokeType(StrokeType.OUTSIDE);
                line.setStroke(CURVE_LINE_COLOR);
                addChild(line);

                slope = nextSlope;
            }
        }
        currentBarsAnimation = new Timeline(new KeyFrame(Duration.seconds(1), keyValues));
        currentBarsAnimation.play();

        bestFitPath.setStrokeWidth(3);
        Shape bestFit = Shape.subtract(bestFitPath, new Rectangle(0, TOP_PADDING + barHeight, width, 50));
        bestFit.setFill(CURVE_COLOR);
        addChild(bestFit);
    }

    public void clear() {
        if (currentBarsAnimation != null) {
            currentBarsAnimation.stop();
        }
        getChildren().removeAll(graphChildren);
        graphChildren.clear();
        dataPoints.clear();
    }

    public enum AxisType {
        DATE, DAY_OF_WEEK, HOUR, HOUR_MINUTE
    }
}
