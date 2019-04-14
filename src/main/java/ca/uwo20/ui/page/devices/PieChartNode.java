package ca.uwo20.ui.page.devices;

import ca.uwo20.ui.device.DeviceManager;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Author: toropov
 * Date: 3/19/2019
 */
public class PieChartNode extends GridPane {

    public PieChartNode(double width, double height) {
        ObservableList<PieChart.Data> data = DeviceManager.getI().updatePieDataList();

        DevicesPieChart chart = new DevicesPieChart(data);
        getRowConstraints().add(new RowConstraints(32));
        getRowConstraints().add(new RowConstraints(width));
        getRowConstraints().add(new RowConstraints(height - width - 32));

        Label title = new Label("Most active devices");
        title.getStyleClass().add("pieChartTitle");
        GridPane.setHalignment(title, HPos.CENTER);
        add(title, 0, 0);
        add(chart, 0, 1);

        //Legend
        GridPane legendPane = new GridPane();
        add(legendPane, 0, 2);
        legendPane.setPadding(new Insets(0, 5, 5, 5));
        double h = (height - width - 32 - 5) / 6;
        legendPane.getColumnConstraints().add(new ColumnConstraints(h));
        legendPane.getColumnConstraints().add(new ColumnConstraints(width - h - 10));

        for (int i = 0; i < 6; i++) {
            legendPane.getRowConstraints().add(new RowConstraints(h));
        }
        legendPane.setGridLinesVisible(true);

        DeviceManager.getI().getCurrentUsageUpdateCallbacks().add(() -> {
            legendPane.getChildren().clear();

            List<PieChart.Data> ordered = data.stream()
                    .sorted(Comparator.comparingDouble(PieChart.Data::getPieValue).reversed())
                    .collect(Collectors.toList());
            ordered.remove(DeviceManager.getI().getOtherDevicesData());
            ordered.add(DeviceManager.getI().getOtherDevicesData());

            for (int i = 0; i < ordered.size(); i++) {
                PieChart.Data d = ordered.get(i);

                Color color = Color.web(DeviceManager.getI().getPieColorMap().getOrDefault(d, "#9e9e9e"));
                Circle circle = new Circle(10, color);
                Circle innerCircle = new Circle(5, Color.WHITESMOKE);
                GridPane.setHalignment(circle, HPos.CENTER);
                GridPane.setValignment(circle, VPos.CENTER);
                GridPane.setHalignment(innerCircle, HPos.CENTER);
                GridPane.setValignment(innerCircle, VPos.CENTER);
                Label label = new Label(d.getName());
                label.getStyleClass().add("pieChartLegendLabel");
                GridPane.setHalignment(label, HPos.LEFT);
                GridPane.setValignment(label, VPos.CENTER);

                legendPane.add(circle, 0, i);
                legendPane.add(innerCircle, 0, i);
                legendPane.add(label, 1, i);
            }
        });
    }
}
