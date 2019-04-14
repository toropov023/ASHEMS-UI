package ca.uwo20.ui.util;

import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

/**
 * Author: toropov
 * Date: 2/24/2019
 */
public class CardStyle {
    public static Pane apply(Pane pane) {
        Background background = new Background(new BackgroundFill(Color.WHITESMOKE, new CornerRadii(4), null));
        pane.setBackground(background);

        DropShadow dropShadow = new DropShadow();
        dropShadow.setOffsetX(2);
        dropShadow.setOffsetY(2);
        pane.setEffect(dropShadow);

        return pane;
    }
}
