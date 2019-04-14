package ca.uwo20.ui.weather;

import ca.uwo20.ui.Main;
import ca.uwo20.ui.util.Scheduler;
import com.grack.nanojson.*;
import lombok.Getter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Author: toropov
 * Date: 2/20/2019
 */
public class WeatherAPI {
    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String API_KEY = "$weatherApiKey";
    private static final String URL = "http://api.openweathermap.org/data/2.5/";
    private static WeatherAPI i;
    @Getter
    private final List<WeatherDay> weatherDays = new ArrayList<>();
    private List<Consumer<List<WeatherDay>>> updateConsumers = new CopyOnWriteArrayList<>();

    private WeatherAPI() {
        Scheduler.runRepeated(task -> update(), 10, TimeUnit.MINUTES);
    }

    public static WeatherAPI getI() {
        if (i == null) {
            i = new WeatherAPI();
        }

        return i;
    }

    public void addUpdateConsumer(Consumer<List<WeatherDay>> updateConsumer) {
        updateConsumers.add(updateConsumer);
    }

    private void update() {
        weatherDays.clear();

        //Get current
        JsonObject currentJson = callApi("weather");
        if (currentJson == null) {
            return;
        }

        WeatherDay currentDay = new WeatherDay(LocalDateTime.now());
        currentDay.setCurrentTemperature((int) currentJson.getObject("main").getDouble("temp"));
        currentDay.setConditionAndIcon(currentJson.getArray("weather").getObject(0).getString("main"), currentJson.getArray("weather").getObject(0).getString("icon"));
        weatherDays.add(currentDay);

        //Get forecast
        JsonObject forecastJson = callApi("forecast");
        if (forecastJson == null) {
            return;
        }
        JsonArray array = forecastJson.getArray("list");

        WeatherDay day = null;
        for (int i = 0; i < array.size(); i++) {
            JsonObject data = array.getObject(i);
            LocalDateTime date = LocalDateTime.from(dateFormat.parse(data.getString("dt_txt")));

            if (day == null || day.getDate().getDayOfWeek() != date.getDayOfWeek()) {
                if (day != null) {
                    weatherDays.add(day);
                }
                day = new WeatherDay(date);
            }

            int temp = (int) data.getObject("main").getDouble("temp");
            if (temp < day.getNightTemperature()) {
                day.setNightTemperature(temp);
            }
            if (temp > day.getDayTemperature()) {
                day.setDayTemperature(temp);
                day.setConditionAndIcon(data.getArray("weather").getObject(0).getString("main"), data.getArray("weather").getObject(0).getString("icon"));
            }
        }
        weatherDays.add(day);

        Scheduler.runOnMainThread(() -> updateConsumers.forEach(c -> c.accept(weatherDays)));
    }


    private JsonObject callApi(String type) {
        JsonObject jsonObject = null;
        boolean loadedFromFile = false;

        File file = new File(System.getProperty("user.home") + "/powerHour/weather/" + type + ".json");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            URL url = new URL(URL + type + "?q=London,ca&units=metric&APPID=" + API_KEY);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(2000);
            connection.setRequestMethod("GET");
            jsonObject = JsonParser.object().from(connection.getInputStream());
            connection.disconnect();
        } catch (IOException | JsonParserException e) {
            try {
                InputStream stream = new FileInputStream(file);
                StringBuilder builder = new StringBuilder();

                try (Reader reader = new BufferedReader(new InputStreamReader(stream, Charset.forName(StandardCharsets.UTF_8.name())))) {
                    int c;
                    while ((c = reader.read()) != -1) {
                        builder.append((char) c);
                    }

                    if (!builder.toString().isEmpty()) {
                        jsonObject = JsonParser.object().from(builder.toString());
                        loadedFromFile = true;
                    }
                } catch (IOException | JsonParserException e1) {
                    e1.printStackTrace();
                }
            } catch (FileNotFoundException e2) {
                e2.printStackTrace();
            }

            if (jsonObject == null) {
                try {
                    jsonObject = JsonParser.object().from(Main.class.getResourceAsStream("/offline/weather/" + type + ".json"));
                } catch (JsonParserException e3) {
                    e.printStackTrace();
                }
            }
        }

        if (!loadedFromFile && jsonObject != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(JsonWriter.string(jsonObject));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return jsonObject;
    }
}
