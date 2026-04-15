package com.kidsbank;

import com.kidsbank.service.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

/**
 * Main JavaFX Application entry point for Virtual Bank for Kids.
 * Group SE-24 - Agile Software Development
 */
public class MainApp extends Application {

    // Shared services — created once, passed to controllers
    private static AuthService authService;
    private static AccountService accountService;
    private static TaskService taskService;
    private static SavingsGoalService goalService;
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;

        // Initialise services
        authService     = new AuthService();
        accountService  = new AccountService();
        taskService     = new TaskService(accountService);
        goalService     = new SavingsGoalService(accountService);

        stage.setTitle("KidsBank 🏦 - Virtual Bank for Kids");
        stage.setMinWidth(600);
        stage.setMinHeight(500);

        showLoginScreen();
        stage.show();
    }

    // ======================== SCENE NAVIGATION ========================

    public static void showLoginScreen() {
        LoginScreen screen = new LoginScreen(authService, accountService);
        primaryStage.setScene(new Scene(screen.getRoot(), 620, 520));
        primaryStage.setTitle("KidsBank 🏦 - Welcome");
    }

    public static void showChildDashboard() {
        ChildDashboardScreen screen = new ChildDashboardScreen(
                authService, accountService, taskService, goalService);
        primaryStage.setScene(new Scene(screen.getRoot(), 700, 580));
        primaryStage.setTitle("KidsBank 🏦 - " + authService.getCurrentUser().getName() + "'s Dashboard");
    }

    public static void showParentDashboard() {
        ParentDashboardScreen screen = new ParentDashboardScreen(
                authService, accountService, taskService);
        primaryStage.setScene(new Scene(screen.getRoot(), 700, 580));
        primaryStage.setTitle("KidsBank 🏦 - Parent Dashboard");
    }

    public static void showTransactionHistory(String accountId, String accountName) {
        TransactionScreen screen = new TransactionScreen(accountService, accountId, accountName);
        primaryStage.setScene(new Scene(screen.getRoot(), 700, 560));
        primaryStage.setTitle("KidsBank 🏦 - Transaction History");
    }

    public static void showSavingsGoals(String accountId) {
        SavingsGoalScreen screen = new SavingsGoalScreen(goalService, accountService, accountId);
        primaryStage.setScene(new Scene(screen.getRoot(), 700, 560));
        primaryStage.setTitle("KidsBank 🏦 - Savings Goals");
    }

    public static void showTaskScreen(boolean isParent) {
        TaskScreen screen = new TaskScreen(authService, taskService, accountService, isParent);
        primaryStage.setScene(new Scene(screen.getRoot(), 700, 580));
        primaryStage.setTitle("KidsBank 🏦 - " + (isParent ? "Manage Tasks" : "My Tasks"));
    }

    /** Helper: styled header label. */
    public static Label makeHeader(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        lbl.setTextFill(Color.web("#1F4E79"));
        return lbl;
    }

    /** Helper: styled primary button. */
    public static Button makePrimaryBtn(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color:#1F4E79; -fx-text-fill:white; "
                + "-fx-font-size:13px; -fx-font-weight:bold; "
                + "-fx-background-radius:6; -fx-padding:8 18 8 18;");
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color:#2E75B6; -fx-text-fill:white; "
                + "-fx-font-size:13px; -fx-font-weight:bold; "
                + "-fx-background-radius:6; -fx-padding:8 18 8 18;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color:#1F4E79; -fx-text-fill:white; "
                + "-fx-font-size:13px; -fx-font-weight:bold; "
                + "-fx-background-radius:6; -fx-padding:8 18 8 18;"));
        return btn;
    }

    /** Helper: styled danger button. */
    public static Button makeDangerBtn(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color:#C0392B; -fx-text-fill:white; "
                + "-fx-font-size:13px; -fx-font-weight:bold; "
                + "-fx-background-radius:6; -fx-padding:8 18 8 18;");
        return btn;
    }

    /** Helper: styled success button. */
    public static Button makeSuccessBtn(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color:#1E8449; -fx-text-fill:white; "
                + "-fx-font-size:13px; -fx-font-weight:bold; "
                + "-fx-background-radius:6; -fx-padding:8 18 8 18;");
        return btn;
    }

    /** Shows an info alert. */
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /** Shows an error alert. */
    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
