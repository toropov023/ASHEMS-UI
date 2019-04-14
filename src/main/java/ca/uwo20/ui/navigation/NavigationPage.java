package ca.uwo20.ui.navigation;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Singular;

import java.util.List;

/**
 * Author: toropov
 * Date: 1/16/2019
 */
@Builder
@Getter
public class NavigationPage {
    private String title;
    @Setter
    private NavigationPage parent;
    @Singular("node")
    private List<NavigationNode> nodes;

    public void init() {
        nodes.forEach(node -> node.setPage(this));
    }

    public void clearSelection() {
        nodes.forEach(NavigationNode::unselect);
    }

    public void onDrawerEvent(boolean closed) {
        nodes.forEach(node -> node.onDrawerEvent(closed));
    }
}
