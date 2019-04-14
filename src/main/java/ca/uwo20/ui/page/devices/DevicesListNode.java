package ca.uwo20.ui.page.devices;

import ca.uwo20.ui.device.Device;
import ca.uwo20.ui.device.DeviceManager;
import ca.uwo20.ui.util.CardStyle;
import ca.uwo20.ui.util.LabelSize;
import com.jfoenix.controls.JFXToggleButton;
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import javafx.beans.property.StringProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Author: toropov
 * Date: 3/20/2019
 */
public class DevicesListNode extends GridPane {
    private final static Random r = new Random();
    private final static String TITLE = "Connected devices";
    private final static int TITLE_FONT = 20;
    private final double width;

    private VBox scrollBox = new VBox(15);

    public DevicesListNode(double width, double height) {
        this.width = width;
        setMinHeight(height);
        setMaxHeight(height);
        setMinWidth(width);
        setMaxWidth(width);

        Pair<Double, Double> titleSize = LabelSize.get(TITLE, TITLE_FONT);
        getRowConstraints().add(new RowConstraints(titleSize.getValue() + 10));
        getRowConstraints().add(new RowConstraints(height - titleSize.getValue() - 10));
        getColumnConstraints().add(new ColumnConstraints(width));

        Label title = new Label(TITLE);
        title.setFont(Font.font(TITLE_FONT));
        title.getStyleClass().add("devicesListTitle");
        GridPane.setHalignment(title, HPos.CENTER);
        GridPane.setValignment(title, VPos.TOP);

        Pane titleBackground = new Pane();
        titleBackground.getStyleClass().add("devicesListTitlePane");
        DropShadow dropShadow = new DropShadow();
        dropShadow.setOffsetY(2);
        titleBackground.setEffect(dropShadow);
        GridPane.setHgrow(titleBackground, Priority.ALWAYS);
        add(titleBackground, 0, 0);

        add(title, 0, 0);

        ScrollPane scrollPane = new ScrollPane();
        if (!System.getProperty("os.name").equals("Windows 10")) {
            scrollPane.setCursor(Cursor.NONE);
        }
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setBackground(new Background(new BackgroundFill(Color.WHITESMOKE, null, null)));
        scrollPane.setBorder(Border.EMPTY);

        scrollBox.setPadding(new Insets(10));
        scrollPane.setContent(scrollBox);
        update();

        addRow(1, scrollPane);
    }

    private void update() {
        scrollBox.getChildren().clear();
        scrollBox.getChildren().addAll(DeviceManager.getI().getDevices().stream().map(DeviceNode::new).collect(Collectors.toList()));
    }

    private HBox createInfoBox(String title, String value, Consumer<StringProperty> bind) {
        HBox hBox = new HBox(5);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("deviceInfoLabel");
        Label infoLabel = new Label(value);
        infoLabel.getStyleClass().add("deviceInfoLabel");
        if (bind != null) {
            bind.accept(infoLabel.textProperty());
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        hBox.getChildren().addAll(titleLabel, spacer, infoLabel);

        return hBox;
    }

    private class DeviceNode extends HBox {
        public DeviceNode(Device device) {
            super(5);
            setMinWidth(width - 32);

            //Icon and title
            VBox infoBox = new VBox(2);
            infoBox.setMinWidth(250);
            infoBox.setPadding(new Insets(5, 5, 5, 10));

            HBox titleBox = new HBox(5);
            titleBox.setPadding(new Insets(5, 0, 5, 0));
            Text icon = GlyphsDude.createIcon(device.getIcon(), "22px");
            icon.setFill(Color.web("#80DEEA"));
            Label name = new Label(device.getName());
            name.getStyleClass().add("deviceNameLabel");
            name.setAlignment(Pos.CENTER);
            titleBox.getChildren().addAll(icon, name);

            HBox currentUsage = createInfoBox("CURRENT USAGE", "0", device::setListBind);
            double dUsage = device.getCurrentUsage() * (r.nextInt(10) + 2);
            HBox todayUsage = createInfoBox("DAY USAGE", Device.format(dUsage), null);
            double mUsage = dUsage * LocalDateTime.now().getDayOfMonth();
            HBox monthUsage = createInfoBox("MONTH USAGE", Device.format(mUsage), null);
            infoBox.getChildren().addAll(titleBox, currentUsage, todayUsage, monthUsage);

            AnchorPane statusBox = new AnchorPane();
            statusBox.setMinWidth(120);
            Label statusLabel = new Label();
            statusLabel.setAlignment(Pos.CENTER);
            AnchorPane.setRightAnchor(statusLabel, 0D);
            AnchorPane.setLeftAnchor(statusLabel, 0D);
            AnchorPane.setTopAnchor(statusLabel, 40D);
            statusLabel.getStyleClass().add("deviceStatusLabel");
            if (device.isControllable()) {
                JFXToggleButton toggle = new JFXToggleButton();
                AnchorPane.setTopAnchor(toggle, -7D);
                AnchorPane.setRightAnchor(toggle, 0D);
                AnchorPane.setLeftAnchor(toggle, 0D);
                toggle.setAlignment(Pos.TOP_RIGHT);
                toggle.setSelected(device.isToggle());
                toggle.selectedProperty().addListener((observable, oldValue, newValue) -> device.toggle(newValue));
                device.setToggleBind(toggle.selectedProperty());
                toggle.setAlignment(Pos.TOP_RIGHT);
                statusBox.getChildren().add(toggle);

                statusLabel.setText("CONTROLLABLE");
                statusLabel.setTextFill(Color.web("#4db6ac"));
            } else {
                Color statusColor;
                if (device.isToggle()) {
                    statusLabel.setText("TURNED ON");
                    statusLabel.setTextFill(Color.web("#81C784"));
                    statusColor = Color.web("#81C784");
                } else {
                    statusLabel.setText("TURNED OFF");
                    statusLabel.setTextFill(Color.web("#BDBDBD"));
                    statusColor = Color.web("#BDBDBD");
                }

                Text statusIconText = GlyphsDude.createIcon(MaterialDesignIcon.POWER);
                statusIconText.setFill(statusColor);

                Label statusIconLabel = new Label();
                statusIconLabel.setFont(Font.font(30));
                statusIconLabel.setGraphic(statusIconText);
                statusIconLabel.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                statusIconLabel.setAlignment(Pos.CENTER);
                AnchorPane.setRightAnchor(statusIconLabel, 0D);
                AnchorPane.setLeftAnchor(statusIconLabel, 0D);
                AnchorPane.setTopAnchor(statusIconLabel, 5D);
                statusBox.getChildren().add(statusIconLabel);
            }
            statusBox.getChildren().add(statusLabel);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            getChildren().addAll(infoBox, spacer, statusBox);

            CardStyle.apply(this);
        }
    }
}
