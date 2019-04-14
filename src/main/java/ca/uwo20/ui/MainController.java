package ca.uwo20.ui;

import ca.uwo20.ui.navigation.NavigationNode;
import ca.uwo20.ui.navigation.NavigationPage;
import ca.uwo20.ui.navigation.SidebarNavigation;
import ca.uwo20.ui.page.devices.DevicesPage;
import ca.uwo20.ui.page.home.HomePage;
import com.jfoenix.controls.JFXDrawer;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;
import lombok.Getter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainController {
    @Getter
    private static SidebarNavigation navigation;
    @Getter
    private static MainController i;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss | E, MMM d");

    public GridPane header;
    public Label dateLabel;
    public AnchorPane container;
    public AnchorPane root;
    @FXML
    private JFXDrawer drawer;

    @FXML
    private void initialize() {
        i = this;
        navigation = new SidebarNavigation(drawer);

        NavigationNode home = new NavigationNode("Home", MaterialIcon.HOME, HomePage.getI());
        NavigationPage navigation = NavigationPage.builder()
                .node(home)
                .node(new NavigationNode("Devices", MaterialIcon.WIDGETS, DevicesPage.getI()))
                .build();
        MainController.navigation.navigate(navigation);
        home.getOnClick().accept(home);

        //Container
        container.setOnMouseClicked(event -> MainController.navigation.close());

        //Setup clock
        Timeline clock = new Timeline(
                new KeyFrame(Duration.ZERO, event -> dateLabel.setText(dateFormat.format(new Date()))),
                new KeyFrame(Duration.seconds(1))
        );
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode().equals(KeyCode.Q)) {
                Platform.exit();
            }
        });
    }

    public final <T extends Event> void addEventHandler(final EventType<T> eventType, final EventHandler<? super T> eventHandler) {
        root.addEventHandler(eventType, eventHandler);
    }
}
