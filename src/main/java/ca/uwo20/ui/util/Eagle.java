package ca.uwo20.ui.util;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: toropov
 * Date: 3/31/2019
 */
public class Eagle {
    private static final Pattern VARIABLE_QUERY_PATTERN = Pattern.compile("<Value>(.+?)</Value>");

    private static final String BASE_URL = "$eagleUrl";
    private static final String DEVICE_ID = "$";
    private static final String USER = "$eagleUser";
    private static final String PASS = "$eaglePass";

    public static double getCurrentUsage(Receptacle receptacle) {
        String value = queryVariable(receptacle, "zigbee:InstantaneousDemand", false);
        return value != null ? Double.parseDouble(value) : 0;
    }

    public static boolean getState(Receptacle receptacle) {
        String value = queryVariable(receptacle, "zigbee:OnOff", true);
        return value != null && value.equals("On");
    }

    public static void setState(Receptacle receptacle, boolean state) {
        String body = "<Command>" +
                "<Name>device_control</Name>" +
                "<DeviceDetails>" +
                "<HardwareAddress>" + DEVICE_ID + "</HardwareAddress>" +
                "</DeviceDetails>" +
                "<Components>" +
                "<Component>" +
                "<Name>" + receptacle.id + "</Name>" +
                "<Variables>" +
                "<Variable>" +
                "<Name>zigbee:OnOff</Name>" +
                "<Value>" + (state ? "on" : "off") + "</Value>" +
                "</Variable>" +
                "</Variables>" +
                "</Component>" +
                "</Components>" +
                "</Command>";

        post(body);
    }

    private static String queryVariable(Receptacle receptacle, String variable, boolean refresh) {
        String body = "<Command>" +
                "<Name>device_query</Name>" +
                "<DeviceDetails>" +
                "<HardwareAddress>" + DEVICE_ID + "</HardwareAddress>" +
                "</DeviceDetails>" +
                "<Components>" +
                "<Component>" +
                "<Name>" + receptacle.id + "</Name>" +
                "<Variables>" +
                "<Variable>" +
                "<Name>" + variable + "</Name>" +
                (refresh ? "<Refresh>Y</Refresh>" : "") +
                "</Variable>" +
                "</Variables>" +
                "</Component>" +
                "</Components>" +
                "</Command>";

        String post = post(body);
        if (post != null) {
            Matcher matcher = VARIABLE_QUERY_PATTERN.matcher(post);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    private static String post(String body) {
        try {
            URL url = new URL("http://" + BASE_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);

            String userPass = Base64.getEncoder().encodeToString((USER + ":" + PASS).getBytes(StandardCharsets.UTF_8));
            connection.setRequestProperty("Authorization", "Basic " + userPass);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-type", "text/xml");

            PrintWriter printWriter = new PrintWriter(connection.getOutputStream());
            printWriter.write(body);
            printWriter.close();

            String outString = streamToString(connection.getInputStream());
            connection.disconnect();

            return outString;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private static String streamToString(InputStream in) {
        try (Scanner scanner = new Scanner(in, StandardCharsets.UTF_8.name())) {
            return scanner.useDelimiter("\\A").next();
        }
    }

    @RequiredArgsConstructor
    public enum Receptacle {
        TOP("Receptacle 1"),
        BOTTOM("Receptacle 2");

        private final String id;
    }
}
