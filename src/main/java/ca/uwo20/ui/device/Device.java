package ca.uwo20.ui.device;

import ca.uwo20.ui.util.Eagle;
import ca.uwo20.ui.util.Scheduler;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.chart.PieChart;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Author: toropov
 * Date: 3/19/2019
 */
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Device {
    @EqualsAndHashCode.Include
    private final String name;
    private final PieChart.Data data;
    private final double usageMax;
    private final double usageMin;
    private final MaterialDesignIcon icon;
    private boolean controllable;
    private boolean toggle;
    private double currentUsage;
    private StringProperty listBind;
    private BooleanProperty toggleBind;
    private Eagle.Receptacle receptacle;

    Device(String name, MaterialDesignIcon icon, double currentUsage, double usageMax, double usageMin) {
        this.name = name;
        this.icon = icon;
        this.currentUsage = currentUsage;
        this.usageMax = usageMax;
        this.usageMin = usageMin;

        data = new PieChart.Data(name, 0);
    }

    public static String format(double value) {
        if (value > 1000) {
            return DeviceManager.FORMAT.format(value / 1000) + " kWh";
        } else {
            return DeviceManager.FORMAT_NO_DECIMAL.format(value) + " Wh";
        }
    }

    void setCurrentUsage(double currentUsage) {
        this.currentUsage = currentUsage;

        data.setPieValue(currentUsage);
        updateListBind();
    }

    public void setListBind(StringProperty listBind) {
        this.listBind = listBind;

        updateListBind();
    }

    private void updateListBind() {
        if (listBind != null) {
            listBind.set(format(currentUsage));
        }
    }

    void setToggle(boolean toggle) {
        this.toggle = toggle;

        if (toggleBind != null) {
            toggleBind.set(toggle);
        }
    }

    public void toggle(boolean state) {
        if (receptacle != null && toggle != state) {
            Scheduler.run(() -> Eagle.setState(receptacle, state));
        }

        this.toggle = state;
    }
}
