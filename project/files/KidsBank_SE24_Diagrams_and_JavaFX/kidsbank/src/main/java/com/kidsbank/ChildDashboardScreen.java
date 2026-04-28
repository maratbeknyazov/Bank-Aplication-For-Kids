package com.kidsbank;

import com.kidsbank.model.*;
import com.kidsbank.service.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

import java.util.List;

/**
 * STAGE 5.3: UI — Child dashboard (build after login).
 * Shows balance, tasks, goals, transaction history.
 * Group SE-24 - Virtual Bank for Kids
 */
public class ChildDashboardScreen {

    private final AuthService authService;
    private final AccountService accountService;
    private final TaskService taskService;
    private final SavingsGoalService goalService;
    private BorderPane root;

    public ChildDashboardScreen(AuthService authService, AccountService accountService,
                                 TaskService taskService, SavingsGoalService goalService) {
        this.authService    = authService;
        this.accountService = accountService;
        this.taskService    = taskService;
        this.goalService    = goalService;
        buildUI();
    }

    private void buildUI() {
        root = new BorderPane();
        root.setStyle("-fx-background-color:#EBF3FB;");

        User user = authService.getCurrentUser();
        List<Account> accounts = accountService.getAccountsForUser(user.getUserId());
        Account account = accounts.isEmpty() ? null : accounts.get(0);

        // ---- TOP HEADER ----
        HBox header = new HBox();
        header.setPadding(new Insets(14, 20, 14, 20));
        header.setStyle("-fx-background-color:#1F4E79;");
        header.setAlignment(Pos.CENTER_LEFT);
        Label headerLbl = new Label("🏦  Hi " + user.getName() + "! Welcome to KidsBank");
        headerLbl.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        headerLbl.setTextFill(Color.WHITE);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color:#C0392B; -fx-text-fill:white; "
                + "-fx-font-weight:bold; -fx-background-radius:5;");
        logoutBtn.setOnAction(e -> { authService.logout(); MainApp.showLoginScreen(); });
        header.getChildren().addAll(headerLbl, spacer, logoutBtn);
        root.setTop(header);

        // ---- CENTER CONTENT ----
        VBox center = new VBox(16);
        center.setPadding(new Insets(24));
        center.setAlignment(Pos.TOP_CENTER);

        // Balance card
        VBox balanceCard = new VBox(6);
        balanceCard.setAlignment(Pos.CENTER);
        balanceCard.setPadding(new Insets(18));
        balanceCard.setStyle("-fx-background-color:white; -fx-background-radius:12; "
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.1),10,0,0,2);");
        balanceCard.setMaxWidth(400);

        Label balanceTitleLbl = new Label("Current Balance");
        balanceTitleLbl.setFont(Font.font("Arial", 13));
        balanceTitleLbl.setTextFill(Color.GRAY);

        double balance = account != null ? account.getBalance() : 0.0;
        Label balanceLbl = new Label("$" + String.format("%.2f", balance));
        balanceLbl.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        balanceLbl.setTextFill(Color.web("#1E8449"));

        String accType = account != null ? account.getAccountType().getDisplayName() : "No Account";
        Label accTypeLbl = new Label(accType);
        accTypeLbl.setFont(Font.font("Arial", FontPosture.ITALIC, 11));
        accTypeLbl.setTextFill(Color.GRAY);

        balanceCard.getChildren().addAll(balanceTitleLbl, balanceLbl, accTypeLbl);

        // Pending tasks alert
        long pendingTasks = account == null ? 0 :
            taskService.getTasksForChild(user.getUserId()).stream()
                .filter(t -> t.getStatus() == TaskStatus.PENDING).count();
        if (pendingTasks > 0) {
            Label taskAlert = new Label("📋  You have " + pendingTasks + " task(s) waiting for you!");
            taskAlert.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            taskAlert.setTextFill(Color.web("#D35400"));
            taskAlert.setPadding(new Insets(8, 16, 8, 16));
            taskAlert.setStyle("-fx-background-color:#FEF9E7; -fx-background-radius:8; "
                    + "-fx-border-color:#F39C12; -fx-border-radius:8;");
            center.getChildren().add(taskAlert);
        }

        // Savings goal mini-preview
        if (account != null) {
            List<SavingsGoal> activeGoals = goalService.getActiveGoals(account.getAccountId());
            if (!activeGoals.isEmpty()) {
                SavingsGoal topGoal = activeGoals.get(0);
                VBox goalPreview = buildGoalPreview(topGoal);
                center.getChildren().add(goalPreview);
            }
        }

        // Action buttons grid
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        grid.setAlignment(Pos.CENTER);

        final String accId = account != null ? account.getAccountId() : null;

        Button withdrawBtn = actionBtn("💸 Withdraw", "#C0392B");
        Button tasksBtn    = actionBtn("📋 My Tasks",  "#1A5276");
        Button goalsBtn    = actionBtn("🎯 Savings Goals", "#1E8449");
        Button historyBtn  = actionBtn("📜 History",   "#6C3483");

        withdrawBtn.setOnAction(e -> {
            if (accId == null) { MainApp.showError("No account found."); return; }
            showWithdrawDialog(accId);
        });
        tasksBtn.setOnAction(e -> MainApp.showTaskScreen(false));
        goalsBtn.setOnAction(e -> {
            if (accId == null) { MainApp.showError("No account found."); return; }
            MainApp.showSavingsGoals(accId);
        });
        historyBtn.setOnAction(e -> {
            if (accId == null) { MainApp.showError("No account found."); return; }
            MainApp.showTransactionHistory(accId, accType);
        });

        grid.add(withdrawBtn, 0, 0);
        grid.add(tasksBtn,    1, 0);
        grid.add(goalsBtn,    0, 1);
        grid.add(historyBtn,  1, 1);

        center.getChildren().addAll(balanceCard, grid);
        root.setCenter(center);
    }

    private VBox buildGoalPreview(SavingsGoal goal) {
        VBox box = new VBox(6);
        box.setPadding(new Insets(12, 18, 12, 18));
        box.setMaxWidth(400);
        box.setStyle("-fx-background-color:white; -fx-background-radius:10; "
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),8,0,0,1);");

        Label title = new Label("🎯 Savings Goal: " + goal.getGoalName());
        title.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        title.setTextFill(Color.web("#1F4E79"));

        ProgressBar pb = new ProgressBar(goal.getProgressPercent());
        pb.setPrefWidth(350);
        pb.setStyle("-fx-accent:#1E8449;");

        Label pctLbl = new Label(String.format("$%.2f / $%.2f (%.0f%%)",
                goal.getSavedAmount(), goal.getTargetAmount(), goal.getProgressPercent() * 100));
        pctLbl.setFont(Font.font("Arial", 11));
        pctLbl.setTextFill(Color.GRAY);

        box.getChildren().addAll(title, pb, pctLbl);
        return box;
    }

    private Button actionBtn(String text, String color) {
        Button btn = new Button(text);
        btn.setMinWidth(200);
        btn.setMinHeight(60);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        btn.setStyle("-fx-background-color:" + color + "; -fx-text-fill:white; "
                + "-fx-background-radius:10; -fx-cursor:hand;");
        return btn;
    }

    private void showWithdrawDialog(String accountId) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Withdraw Money");
        dialog.setHeaderText("How much would you like to withdraw?");

        Account account = accountService.getAccountById(accountId);
        Label balanceLbl = new Label("Current balance: $" + String.format("%.2f", account.getBalance()));
        balanceLbl.setFont(Font.font("Arial", 12));

        TextField amountField = new TextField();
        amountField.setPromptText("Enter amount (e.g. 5.00)");

        Label previewLbl = new Label("");
        previewLbl.setFont(Font.font("Arial", FontPosture.ITALIC, 11));

        Label errorLbl = new Label("");
        errorLbl.setTextFill(Color.RED);

        amountField.textProperty().addListener((obs, old, val) -> {
            try {
                double amt = Double.parseDouble(val);
                double remaining = account.getBalance() - amt;
                if (remaining < 0) {
                    previewLbl.setTextFill(Color.RED);
                    previewLbl.setText("⚠ Insufficient funds!");
                } else {
                    previewLbl.setTextFill(Color.web("#1E8449"));
                    previewLbl.setText("Remaining balance: $" + String.format("%.2f", remaining));
                }
            } catch (NumberFormatException e) {
                previewLbl.setText("");
            }
        });

        VBox content = new VBox(10, balanceLbl, amountField, previewLbl, errorLbl);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    double amount = Double.parseDouble(amountField.getText().trim());
                    accountService.withdraw(accountId, amount);
                    MainApp.showInfo("Success", "Withdrawn $" + String.format("%.2f", amount) + " successfully!");
                    MainApp.showChildDashboard(); // refresh
                } catch (Exception ex) {
                    MainApp.showError(ex.getMessage());
                }
            }
        });
    }

    public BorderPane getRoot() { return root; }
}
