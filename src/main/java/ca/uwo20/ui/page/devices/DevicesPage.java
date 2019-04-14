package ca.uwo20.ui.page.devices;

import ca.uwo20.ui.page.Page;
import ca.uwo20.ui.util.CardStyle;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;

/**
 * Author: toropov
 * Date: 3/19/2019
 */
public class DevicesPage extends GridPane implements Page {
    private static DevicesPage i;

    private DevicesPage() {
        setHgap(15);
        setVgap(15);
        setPadding(new Insets(5, 15, 10, 15));
        getColumnConstraints().add(new ColumnConstraints(505));
        getColumnConstraints().add(new ColumnConstraints(200));
        getRowConstraints().add(new RowConstraints(440));

        DevicesListNode devicesListNode = new DevicesListNode(505, 440);
        add(CardStyle.apply(new Pane(devicesListNode)), 0, 0);
        add(CardStyle.apply(new PieChartNode(200, 440)), 1, 0);
    }

    public static DevicesPage getI() {
        if (i == null) {
            i = new DevicesPage();
        }
        return i;
    }

    @Override
    public void onSelect() {
    }

    @Override
    public Node getNode() {
        return this;
    }
}
