package ca.uwo20.ui.page;

import javafx.scene.Node;

/**
 * Author: toropov
 * Date: 3/4/2019
 */
public interface Page {
    void onSelect();

    Node getNode();
}
