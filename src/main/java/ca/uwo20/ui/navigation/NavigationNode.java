package ca.uwo20.ui.navigation;

import ca.uwo20.ui.MainController;
import ca.uwo20.ui.page.Page;
import com.jfoenix.controls.JFXButton;
import de.jensd.fx.glyphs.GlyphIcons;
import de.jensd.fx.glyphs.GlyphsDude;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.OverrunStyle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

/**
 * Author: toropov
 * Date: 1/16/2019
 */
@Getter
public class NavigationNode {
    private static Timeline closeTimer = new Timeline();
    private final JFXButton button;
    private final Consumer<NavigationNode> onClick;
    @Setter
    private NavigationPage page;
    private String name;

    public NavigationNode(String name, GlyphIcons icon, Page page) {
        this(name, icon, navigationNode -> {
            navigationNode.select();
            page.onSelect();
            MainController.getI().container.getChildren().clear();
            MainController.getI().container.getChildren().add(page.getNode());
        });
    }

    public NavigationNode(String name, GlyphIcons icon, Consumer<NavigationNode> onClick) {
        this.onClick = onClick;
        this.name = name;

        button = new JFXButton();
        if (icon != null) {
            Text text = GlyphsDude.createIcon(icon);
            text.setWrappingWidth(20);
            text.setTextAlignment(TextAlignment.CENTER);
            button.setGraphic(text);
            button.setAlignment(Pos.CENTER);
            button.setTextOverrun(OverrunStyle.CLIP);
        } else {
            button.setAlignment(Pos.BASELINE_LEFT);
            button.setText(name);
        }
        button.setPrefWidth(200);
        button.setDisableVisualFocus(true);
        button.getStyleClass().add("navigationNodeButton");
        button.setButtonType(JFXButton.ButtonType.FLAT);
        button.setOnMouseClicked(event -> {
            if (MainController.getNavigation().getDrawer().isClosed()) {
                MainController.getNavigation().open();
            } else {
                onClick.accept(this);

                //Auto close after 5 seconds
                closeTimer.stop();
                closeTimer = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
                    MainController.getNavigation().close();
                }));
                closeTimer.play();
            }
        });
    }

    public void onDrawerEvent(boolean closed) {
        if (button.getGraphic() != null) {
            if (closed) {
                button.setText(null);
                button.setAlignment(Pos.CENTER);
            } else {
                button.setAlignment(Pos.BASELINE_LEFT);
                button.setText(name);
            }
        }

        closeTimer.stop();
    }

    public void select() {
        if (page != null) {
            page.clearSelection();
        }
        button.setDisable(true);
    }

    public void unselect() {
        button.setDisable(false);
    }
}
