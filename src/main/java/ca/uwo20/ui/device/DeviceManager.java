package ca.uwo20.ui.device;

import ca.uwo20.ui.Main;
import ca.uwo20.ui.util.Eagle;
import ca.uwo20.ui.util.Scheduler;
import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import lombok.Getter;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Author: toropov
 * Date: 3/19/2019
 */
@Getter
public class DeviceManager {
    public static final String OTHER_DEVICES_NAME = "Other devices";
    public static final NumberFormat FORMAT = new DecimalFormat("###0.00");
    public static final NumberFormat FORMAT_NO_DECIMAL = new DecimalFormat("000");
    private static final Random r = new Random();
    private static final int PIE_SIZE = 5;
    private static DeviceManager i;
    private final LinkedHashSet<Device> devices = new LinkedHashSet<>();
    private final ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
    private final PieChart.Data otherDevicesData = new PieChart.Data(OTHER_DEVICES_NAME, 0);
    private final Set<Runnable> currentUsageUpdateCallbacks = new HashSet<>();
    private final List<String> colors = new ArrayList<>();
    private final Map<PieChart.Data, String> pieColorMap = new HashMap<>();
    private boolean pieColorSwitch;
    private Set<Device> receptacles = ConcurrentHashMap.newKeySet();

    private DeviceManager() {
        colors.add("#5c6bc0");
        colors.add("#ec407a");
        colors.add("#26a69a");
        colors.add("#ffa726");
        colors.add("#d4e157");
        colors.add("#ef5350");
        colors.add("#42a5f5");
        colors.add("#7e57c2");
        colors.add("#d4e157");
        colors.add("#26c6da");

        pullDevices();
        Scheduler.runRepeated(task -> updateCurrentUsage(), 5, TimeUnit.SECONDS);
    }

    public static DeviceManager getI() {
        if (i == null) {
            i = new DeviceManager();
        }
        return i;
    }

    public ObservableList<PieChart.Data> updatePieDataList() {
        List<PieChart.Data> list = devices.stream()
                .sorted(Comparator.comparingDouble(Device::getCurrentUsage).reversed())
                .map(Device::getData)
                .collect(Collectors.toList());

        List<PieChart.Data> toRemove = new ArrayList<>(pieData);
        int listSize = list.size();
        for (int i = 0; i < PIE_SIZE && i < listSize; i++) {
            PieChart.Data data = list.remove(0);
            if (!pieData.contains(data)) {
                pieData.add(data);
            }
            toRemove.remove(data);
        }

        if (!list.isEmpty()) {
            otherDevicesData.setPieValue(list.stream().mapToDouble(PieChart.Data::getPieValue).sum());

            if (!pieData.contains(otherDevicesData)) {
                pieData.add(otherDevicesData);
            }
            toRemove.remove(otherDevicesData);
        }
        pieData.removeAll(toRemove);

        Scheduler.runOnMainThread(() -> {
            if (toRemove.size() == 1) {
                pieColorSwitch = !pieColorSwitch;
            }

            pieColorMap.clear();
            for (int i = 0; i < pieData.size() && i < colors.size(); i++) {
                PieChart.Data data = pieData.get(i);
                if (!data.equals(otherDevicesData)) {
                    int c = pieColorSwitch && i == pieData.size() - 1 && i < colors.size() ? i + 1 : i;
                    data.getNode().setStyle("-fx-pie-color: " + colors.get(c) + "; -fx-border-color: #F5F5F5");
                    pieColorMap.put(data, colors.get(c));
                }
            }

            otherDevicesData.getNode().setStyle("-fx-pie-color: #9e9e9e; -fx-border-color: #F5F5F5");
        });

        return pieData;
    }

    private void updateCurrentUsage() {
        for (Device device : receptacles) {
            double currentUsage = Eagle.getCurrentUsage(device.getReceptacle()) * 1000;
            Scheduler.runOnMainThread(() -> device.setCurrentUsage(currentUsage));
        }

        Scheduler.runOnMainThread(() -> {
            for (Device device : devices) {
                if (!device.isControllable()) {
                    if (device.isToggle()) {
                        device.setCurrentUsage(Math.min(device.getUsageMax(), Math.max(device.getUsageMin(), device.getCurrentUsage() + (r.nextInt(3) - 1) * 5)));
                    } else {
                        device.setCurrentUsage(0);
                    }
                }
            }
            updatePieDataList();

            Scheduler.run(() -> Scheduler.runOnMainThread(() -> currentUsageUpdateCallbacks.forEach(Runnable::run)), 50, TimeUnit.MILLISECONDS);
        });
    }

    private void pullDevices() {
        Set<Device> removedDevices = new HashSet<>(devices);
        try {
            JsonObject json = JsonParser.object().from(Main.class.getResourceAsStream("/offline/devices/devices.json"));
            JsonArray array = json.getArray("devices");
            for (int i = 0; i < array.size(); i++) {
                JsonObject object = array.getObject(i);
                MaterialDesignIcon icon;
                try {
                    icon = MaterialDesignIcon.valueOf(object.getString("icon"));
                } catch (IllegalArgumentException | NullPointerException e) {
                    icon = MaterialDesignIcon.POWER_SOCKET;
                }

                Device device = new Device(object.getString("name"), icon, object.getDouble("wh"), object.getDouble("whMax"), object.getDouble("whMin"));
                device.setControllable(object.getBoolean("toggleable", false));
                device.setToggle(!object.getBoolean("turnedOff", false));

                if (object.containsKey("receptacle")) {
                    device.setReceptacle(Eagle.Receptacle.valueOf(object.getString("receptacle")));
                    receptacles.add(device);
                }

                devices.add(device);
                removedDevices.remove(device);
            }
            devices.removeAll(removedDevices);

            //Query the initial receptacles state
            Scheduler.run(() -> receptacles.forEach(device -> device.setToggle(Eagle.getState(device.getReceptacle()))));
            //Yes... we need to do it twice to make sure eagle refreshes its cache...
            Scheduler.run(() -> receptacles.forEach(device -> device.setToggle(Eagle.getState(device.getReceptacle()))), 5, TimeUnit.SECONDS);
        } catch (JsonParserException e) {
            e.printStackTrace();
        }
    }

    public double getCurrentTotal() {
        return devices.stream().mapToDouble(Device::getCurrentUsage).sum();
    }
}
