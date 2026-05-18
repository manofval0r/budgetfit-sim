package com.budgetfit;

/**
 * Entry point class that does not extend Application.
 * Necessary for correctly bundling JavaFX applications into a shaded/fat JAR.
 */
public class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
