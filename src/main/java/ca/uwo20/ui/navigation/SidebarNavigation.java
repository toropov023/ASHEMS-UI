package ca.uwo20.ui.navigation;

import com.jfoenix.controls.JFXDrawer;
import com.jfoenix.controls.JFXHamburger;
import com.jfoenix.transitions.hamburger.HamburgerBackArrowBasicTransition;
import javafx.animation.Transition;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import lombok.Getter;

/**
 * Author: toropov
 * Date: 1/16/2019
 */
public class SidebarNavigation {
    private static final String DEFAULT_TITLE = "POWER HOUR";
    private static final String TITLE_PADDING = "  ";
    private final Transition burger;
    @Getter
    private final JFXDrawer drawer;
    private final Label title;
    private final VBox buttonsPane;
    private NavigationPage currentPage;

    public SidebarNavigation(JFXDrawer drawer) {
        this.drawer = drawer;
        drawer.setOnDrawerClosed(event -> {
            if (currentPage != null) {
                currentPage.onDrawerEvent(true);
            }
        });

        JFXHamburger hamburger = new JFXHamburger();
        burger = new HamburgerBackArrowBasicTransition(hamburger);
        burger.setRate(-1);
        hamburger.setMinWidth(30);
        hamburger.setOnMouseClicked(event -> {
            if (drawer.isClosed()) {
                open();
            } else {
                if (currentPage != null && currentPage.getParent() != null) {
                    NavigationPage parent = currentPage.getParent();
                    parent.clearSelection();
                    navigate(parent);
                } else {
                    drawer.toggle();
                }
            }
        });

        title = new Label(DEFAULT_TITLE);
        title.getStyleClass().add("sidebarTitle");

        GridPane header = new GridPane();
        header.add(hamburger, 0, 0);
        header.add(title, 1, 0);

        GridPane.setHgrow(title, Priority.ALWAYS);
        GridPane.setHalignment(title, HPos.CENTER);
        header.setPadding(new Insets(5, 0, 0, 10));

        buttonsPane = new VBox();
        StackPane drawerPane = new StackPane(new VBox(header, buttonsPane));
        drawerPane.getStyleClass().add("drawer");
        drawer.setSidePane(drawerPane);

        BackgroundPosition bgPosition = new BackgroundPosition(Side.LEFT, 0, false, Side.BOTTOM, 0, false);
        BackgroundImage bgImage = new BackgroundImage(new Image("/image/logo.png"),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                bgPosition, BackgroundSize.DEFAULT);
        BackgroundFill bgFill = new BackgroundFill(Color.web("#f5f5f6"), null, null);
        Background background = new Background(new BackgroundFill[]{bgFill}, new BackgroundImage[]{bgImage});
        drawerPane.setBackground(background);
    }

    public void navigate(NavigationPage page) {
        if (currentPage != null && page.getParent() == null && currentPage.getParent() != page) {
            page.setParent(currentPage);
        }
        currentPage = page;
        page.init();

        if (page.getTitle() != null) {
            title.setText(TITLE_PADDING + page.getTitle().toUpperCase() + TITLE_PADDING);
        } else {
            title.setText(TITLE_PADDING + DEFAULT_TITLE + TITLE_PADDING);
        }
        if (page.getParent() == null) {
            burger.setRate(-1);
            burger.play();
        } else {
            burger.setRate(1);
            burger.play();
        }

        buttonsPane.getChildren().clear();
        for (NavigationNode node : page.getNodes()) {
            buttonsPane.getChildren().add(node.getButton());
        }
    }

    public void open() {
        drawer.open();
        if (currentPage != null) {
            currentPage.onDrawerEvent(false);
        }
    }

    public void close() {
        drawer.close();
    }
}
