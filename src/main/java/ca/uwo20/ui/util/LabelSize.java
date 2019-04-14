package ca.uwo20.ui.util;

import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Pair;

/**
 * Author: toropov
 * Date: 3/20/2019
 */
public class LabelSize {
    /**
     * Compute what the size of a label with the provided text and font size would be.
     *
     * @param string   The text that a label would have
     * @param fontSize The size of that label
     * @return Width and height
     */
    public static Pair<Double, Double> get(String string, int fontSize) {
        Text testText = new Text(string);
        testText.setFont(Font.font(fontSize));
        testText.applyCss();
        return new Pair<>(testText.getBoundsInLocal().getWidth(), testText.getBoundsInLocal().getHeight());
    }
}
