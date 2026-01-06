package com.simplifyqa.codeeditor.helper;

import java.util.logging.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simplifyqa.pluginbase.plugin.sync.models.FormulaData;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.logging.*;

public class FormulaFetcher {

    private static final String endPoint = "http://localhost:4040/formula/fetch";
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";

    private static final Logger log = Logger.getLogger(FormulaFetcher.class.getName());

    static {
        configureLogger();
    }

    private static void configureLogger() {
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            rootLogger.removeHandler(handler);
        }

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.INFO);
        consoleHandler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                String level = record.getLevel().getLocalizedName();
                String message = record.getMessage().replaceAll("\\r?\\n", " ");
                if (level.equalsIgnoreCase("SEVERE")) {
                    return "[" + ANSI_RED + level + ANSI_RESET + "] " + message + System.lineSeparator();
                }
                return "[" + ANSI_BLUE + level + ANSI_RESET + "] " + message + System.lineSeparator();
            }
        });

        rootLogger.addHandler(consoleHandler);
        rootLogger.setLevel(Level.INFO);
    }

    private static void printGap() {
        System.out.println();
    }

    public static List<FormulaData> fetchFormulas(String projectId) {
        HttpURLConnection connection = null;
        try {
            printGap();
            printGap();
            log.info(ANSI_YELLOW
                    + ":::::::::::::::::::::::::::::::::::SENDING REQUEST TO FETCH FORMULA:::::::::::::::::::::::::::::::::::"
                    + ANSI_RESET);
            String fullUrl = endPoint + "?projectId=" + URLEncoder.encode(projectId, StandardCharsets.UTF_8);
            URL url = new URL(fullUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");  // Changed to JSON
            connection.setDoOutput(false);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    System.out.println(response.toString());
                    ObjectMapper objectMapper = new ObjectMapper();
                    return objectMapper.readValue(response.toString(),
                            new TypeReference<List<FormulaData>>() {
                            });
                }
            } else {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    System.out.println(response.toString());
                }
                log.log(Level.SEVERE, ANSI_RED + "HTTP ERROR: " + responseCode + ANSI_RESET);
                return Collections.emptyList();
            }

        } catch (Exception e) {
            printGap();
            printGap();
            log.log(Level.SEVERE, ANSI_RED + "ERROR: " + ANSI_YELLOW + e.getMessage() + ANSI_RESET);
            throw new RuntimeException(e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
