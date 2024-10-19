package org.vaadin.example;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;

public class EnvUtils {

    private static final Dotenv dotenv;

    static {
        try {
            dotenv = Dotenv.configure().load();
        } catch (DotenvException e) {
            throw new RuntimeException("Failed to load .env file", e);
        }
    }

    public static String get(String variableName) {
        return dotenv.get(variableName);
    }
}
