package ca.uwo20.ui.page.devices;

import ca.uwo20.ui.device.DeviceManager;
import ca.uwo20.ui.util.Scheduler;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import lombok.Getter;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: toropov
 * Date: 3/19/2019
 */
public class DevicesPieChart extends javafx.scene.chart.PieChart {
    private static final int LABEL_FONT = 20;

    private final Circle circle;
    private final Label label;
    private final Pane labelPane;

    public DevicesPieChart(ObservableList<Data> data) {
        circle = new Circle();
        circle.setFill(Color.WHITESMOKE);

        //Label
        label = new Label();
        labelPane = new Pane(label);
        label.getStyleClass().add("pieChartLabel");
        label.setFont(Font.font(LABEL_FONT));

        setLegendVisible(false);
        setLabelsVisible(false);
        setData(data);
    }

    @Override
    protected void layoutChartChildren(double top, double left, double contentWidth, double contentHeight) {
        super.layoutChartChildren(top, left, contentWidth, contentHeight);

        addFeatures();
        updateLayout();
    }

    private void addFeatures() {
        if (getData().size() > 0) {
            Node pie = getData().get(0).getNode();
            if (pie.getParent() instanceof Pane) {
                Pane parent = (Pane) pie.getParent();

                if (!parent.getChildren().contains(circle)) {
                    parent.getChildren().add(circle);
                } else {
                    circle.toFront();
                }
                if (!parent.getChildren().contains(labelPane)) {
                    parent.getChildren().add(labelPane);
                } else {
                    labelPane.toFront();
                }
            }
        }
    }

    private void updateLayout() {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
        for (DevicesPieChart.Data data : getData()) {
            Node node = data.getNode();

            Bounds bounds = node.getBoundsInParent();
            if (bounds.getMinX() < minX) {
                minX = bounds.getMinX();
            }
            if (bounds.getMinY() < minY) {
                minY = bounds.getMinY();
            }
            if (bounds.getMaxX() > maxX) {
                maxX = bounds.getMaxX();
            }
            if (bounds.getMaxY() > maxY) {
                maxY = bounds.getMaxY();
            }
        }

        double centerX = minX + (maxX - minX) / 2;
        double centerY = minY + (maxY - minY) / 2;
        circle.setCenterX(centerX);
        circle.setCenterY(centerY);
        circle.setRadius((maxX - minX) / Math.PI);

        //Label
        String labelString = DeviceManager.FORMAT.format(DeviceManager.getI().getCurrentTotal() / 1000) + " kWh";
        label.setText(labelString);
        Text testText = new Text(labelString);
        testText.setFont(Font.font(LABEL_FONT));
        double titleWidth = testText.getBoundsInLocal().getWidth();
        double titleHeight = testText.getBoundsInLocal().getHeight();
        labelPane.setLayoutX(centerX - titleWidth / 2);
        labelPane.setLayoutY(centerY - titleHeight / 2);
    }
}
