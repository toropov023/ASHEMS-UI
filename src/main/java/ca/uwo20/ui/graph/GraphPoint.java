package ca.uwo20.ui.graph;

import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Author: toropov
 * Date: 3/4/2019
 */
@Data
public class GraphPoint {
    private final LocalDateTime dateTime;
    private final Rectangle rectangle = new Rectangle();
    private final Label dateLabel = new Label();
    private double data = 0;
    private double suggestedData = 0;
}
