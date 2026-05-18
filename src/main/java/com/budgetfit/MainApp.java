package com.budgetfit;

import com.budgetfit.database.DatabaseHelper;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize the Database
        DatabaseHelper.initializeDatabase();

        // Load the Login Scene
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(loader.load(), 500, 500);

        primaryStage.setTitle("BudgetFit");
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(750);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
