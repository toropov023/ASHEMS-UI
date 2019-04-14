package ca.uwo20.ui.weather;

import de.jensd.fx.glyphs.weathericons.WeatherIcon;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Author: toropov
 * Date: 2/24/2019
 */
@Data
public class WeatherDay {
    private final LocalDateTime date;
    private String condition = "Loading...";
    private WeatherIcon icon = WeatherIcon.NA;
    private int dayTemperature = -999;
    private int nightTemperature = 999;
    private int currentTemperature;

    public void setConditionAndIcon(String condition, String iconName) {
        this.condition = condition;

        int id = Integer.parseInt(iconName.replace("n", "").replace("d", ""));
        switch (id) {
            case 1:
                icon = WeatherIcon.DAY_SUNNY;
                break;
            case 2:
                icon = WeatherIcon.DAY_SUNNY_OVERCAST;
                break;
            case 3:
                icon = WeatherIcon.CLOUD;
                break;
            case 4:
                icon = WeatherIcon.CLOUDY;
                break;
            case 9:
                icon = WeatherIcon.SHOWERS;
                break;
            case 10:
                icon = WeatherIcon.RAIN;
                break;
            case 11:
                icon = WeatherIcon.THUNDERSTORM;
                break;
            case 13:
                icon = WeatherIcon.SNOW;
                break;
            case 50:
                icon = WeatherIcon.FOG;
                break;
            default:
                icon = WeatherIcon.NA;
        }
    }
}
