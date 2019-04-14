package ca.uwo20.ui.page.home;

import ca.uwo20.ui.Main;
import ca.uwo20.ui.MainController;
import ca.uwo20.ui.graph.Graph;
import ca.uwo20.ui.graph.GraphPoint;
import ca.uwo20.ui.page.Page;
import ca.uwo20.ui.suggestion.Difficulty;
import ca.uwo20.ui.suggestion.Suggestion;
import ca.uwo20.ui.util.CardStyle;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: toropov
 * Date: 2/24/2019
 */
public class HomePage extends GridPane implements Page {
    private static HomePage i;
    private final Graph graph;

    private HomePage() {
        setHgap(15);
        setVgap(15);
        setPadding(new Insets(5, 15, 10, 15));
        getColumnConstraints().add(new ColumnConstraints(505));
        getColumnConstraints().add(new ColumnConstraints(200));
        getRowConstraints().add(new RowConstraints(280));
        getRowConstraints().add(new RowConstraints(145));

        add(new WeatherNode(), 1, 0);

        //Graph
        graph = new Graph("Hourly Consumption", 505, 200, 24);
        graph.setAxisType(Graph.AxisType.HOUR);
        graph.setSkipEvery(2);
        graph.setShowSuggestedLine(true);
        generateGraphData(graph);
        MainController.getI().addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode().equals(KeyCode.F5)) {
                generateGraphData(graph);
            }
        });

        //Suggestions
        SuggestionsPane suggestions = new SuggestionsPane(505, 225, 5);
        List<Suggestion> suggestionList = new ArrayList<>();
        suggestionList.add(new Suggestion("Cook dinner 1 hour early", 0.00417, Difficulty.HARD));
        suggestionList.add(new Suggestion("Start the dishwasher 2 hours late", 0.0035, Difficulty.VERY_EASY));
        suggestionList.add(new Suggestion("Vacuum only on the weekends", 0.0021, Difficulty.EASY));
        suggestionList.add(new Suggestion("Take the shower 1 hour early", 0.0068, Difficulty.HARD));
        suggestionList.add(new Suggestion("Start the dryer 4 hours late", 0.00905, Difficulty.MODERATE));
        suggestions.setSuggestionList(suggestionList);

        add(CardStyle.apply(new VBox(graph, suggestions)), 0, 0, 1, 2);
        add(CardStyle.apply(new Pane(new RateMeter(200, 145))), 1, 1);
    }

    public static HomePage getI() {
        if (i == null) {
            i = new HomePage();
        }
        return i;
    }

    //TODO for test purposes
    private void generateGraphData(Graph graph) {
        String[] data;
        try {
            InputStream stream = Main.class.getResourceAsStream("/offline/graphData");

            StringBuilder textBuilder = new StringBuilder();
            try (Reader reader = new BufferedReader(new InputStreamReader(stream, Charset.forName(StandardCharsets.UTF_8.name())))) {
                int c;
                while ((c = reader.read()) != -1) {
                    textBuilder.append((char) c);
                }
            }

            data = textBuilder.toString().split("\n");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        graph.clear();

        LocalDateTime time = LocalDateTime.now().minus(18, ChronoUnit.HOURS);
        for (int i = 0; i < 24; i++) {
            LocalDateTime plus = time.plus(i, ChronoUnit.HOURS);
            String[] split = data[plus.getHour()].replace("\r", "").split(":");
            GraphPoint point = new GraphPoint(plus);
            if (i <= 18) {
                point.setData(Integer.parseInt(split[1]));
            }
//            else if (i == 12) {
//                point.setData(Math.max(10, r.nextDouble() * 20));
//            }
            point.setSuggestedData(Integer.parseInt(split[2]));
            graph.add(point);
        }

        graph.update(true);
    }

    @Override
    public void onSelect() {
        graph.update(true);
    }

    @Override
    public Node getNode() {
        return this;
    }
}
