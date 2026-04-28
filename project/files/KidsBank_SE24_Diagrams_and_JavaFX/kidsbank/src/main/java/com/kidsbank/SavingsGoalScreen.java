package com.kidsbank;

import com.kidsbank.model.SavingsGoal;
import com.kidsbank.service.AccountService;
import com.kidsbank.service.SavingsGoalService;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

import java.time.LocalDate;
import java.util.List;

/**
 * STAGE 5.5: UI — savings goals screen.
 * Shows progress bars, allows adding money to goals.
 * Group SE-24 - Virtual Bank for Kids
 */
public class SavingsGoalScreen {

    private final SavingsGoalService goalService;
    private final AccountService accountService;
    private final String accountId;
    private BorderPane root;

    public SavingsGoalScreen(SavingsGoalService goalService, AccountService accountService, String accountId) {
        this.goalService    = goalService;
        this.accountService = accountService;
        this.accountId      = accountId;
        buildUI();
    }

    private void buildUI() {
        root = new BorderPane();
        root.setStyle("-fx-background-color:#EBF3FB;");

        // Header
        HBox header = new HBox();
        header.setPadding(new Insets(14, 20, 14, 20));
        header.setStyle("-fx-background-color:#1F4E79;");
        header.setAlignment(Pos.CENTER_LEFT);
        Label hLbl = new Label("🎯  Savings Goals");
        hLbl.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        hLbl.setTextFill(Color.WHITE);
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button backBtn = new Button("← Back");
        backBtn.setStyle("-fx-background-color:#2E75B6; -fx-text-fill:white; "
                + "-fx-font-weight:bold; -fx-background-radius:5;");
        backBtn.setOnAction(e -> MainApp.showChildDashboard());
        header.getChildren().addAll(hLbl, sp, backBtn);

        VBox content = new VBox(14);
        content.setPadding(new Insets(20));

        // Add goal button
        Button addBtn = MainApp.makeSuccessBtn("+ Add New Goal");
        addBtn.setOnAction(e -> showAddGoalDialog());
        content.getChildren().add(addBtn);

        // Active goals
        List<SavingsGoal> active = goalService.getActiveGoals(accountId);
        if (!active.isEmpty()) {
            content.getChildren().add(sectionLbl("🎯 Active Goals"));
            for (SavingsGoal g : active) content.getChildren().add(goalCard(g, false));
        }

        // Completed goals
        List<SavingsGoal> completed = goalService.getCompletedGoals(accountId);
        if (!completed.isEmpty()) {
            content.getChildren().add(sectionLbl("🏆 Completed Goals"));
            for (SavingsGoal g : completed) content.getChildren().add(goalCard(g, true));
        }

        if (active.isEmpty() && completed.isEmpty()) {
            Label empty = new Label("No savings goals yet! Tap '+ Add New Goal' to get started.");
            empty.setFont(Font.font("Arial", FontPosture.ITALIC, 13));
            empty.setTextFill(Color.GRAY);
            content.getChildren().add(empty);
        }

        // Total saved
        double totalSaved = active.stream().mapToDouble(SavingsGoal::getSavedAmount).sum()
                + completed.stream().mapToDouble(SavingsGoal::getSavedAmount).sum();
        Label totalLbl = new Label("💰 Total saved across all goals: $" + String.format("%.2f", totalSaved));
        totalLbl.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        totalLbl.setTextFill(Color.web("#1E8449"));
        totalLbl.setPadding(new Insets(8, 14, 8, 14));
        totalLbl.setStyle("-fx-background-color:white; -fx-background-radius:8;");
        content.getChildren().add(totalLbl);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:transparent; -fx-background:transparent;");

        root.setTop(header);
        root.setCenter(scroll);
    }

    private VBox goalCard(SavingsGoal goal, boolean completed) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(14, 18, 14, 18));
        String bg = completed ? "#EAFAF1" : "white";
        card.setStyle("-fx-background-color:" + bg + "; -fx-background-radius:10; "
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),8,0,0,1);");

        // Title row
        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label nameLbl = new Label((completed ? "✅ " : "🎯 ") + goal.getGoalName());
        nameLbl.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        nameLbl.setTextFill(completed ? Color.web("#1E8449") : Color.web("#1F4E79"));
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label amtLbl = new Label("$" + String.format("%.2f", goal.getSavedAmount())
                + " / $" + String.format("%.2f", goal.getTargetAmount()));
        amtLbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        amtLbl.setTextFill(Color.web("#1E8449"));
        titleRow.getChildren().addAll(nameLbl, sp, amtLbl);

        // Progress bar
        ProgressBar pb = new ProgressBar(goal.getProgressPercent());
        pb.setPrefWidth(Double.MAX_VALUE);
        pb.setStyle("-fx-accent:" + (completed ? "#1E8449" : "#2E75B6") + "; -fx-pref-height:14px;");

        Label pctLbl = new Label(String.format("%.0f%% complete", goal.getProgressPercent() * 100));
        pctLbl.setFont(Font.font("Arial", 11));
        pctLbl.setTextFill(Color.GRAY);

        card.getChildren().addAll(titleRow, pb, pctLbl);

        if (goal.getTargetDate() != null) {
            Label dateLbl = new Label("Target date: " + goal.getTargetDate());
            dateLbl.setFont(Font.font("Arial", FontPosture.ITALIC, 10));
            dateLbl.setTextFill(Color.GRAY);
            card.getChildren().add(dateLbl);
        }

        if (!completed) {
            Button addMoneyBtn = new Button("💰 Add Money");
            addMoneyBtn.setStyle("-fx-background-color:#1F4E79; -fx-text-fill:white; "
                    + "-fx-font-weight:bold; -fx-background-radius:6;");
            addMoneyBtn.setOnAction(e -> showAddMoneyDialog(goal));
            card.getChildren().add(addMoneyBtn);
        }

        return card;
    }

    private void showAddGoalDialog() {
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle("New Savings Goal");
        d.setHeaderText("What are you saving for?");

        TextField nameF   = new TextField(); nameF.setPromptText("e.g. New Bike");
        TextField targetF = new TextField(); targetF.setPromptText("Target amount (e.g. 50.00)");
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Target date (optional)");
        TextField initF = new TextField("0.00");
        initF.setPromptText("Initial deposit from balance (optional)");

        double currentBalance = accountService.getAccountById(accountId).getBalance();
        Label balLbl = new Label("Your current balance: $" + String.format("%.2f", currentBalance));
        balLbl.setFont(Font.font("Arial", 11)); balLbl.setTextFill(Color.GRAY);

        VBox content = new VBox(10,
            new Label("Goal Name:"), nameF,
            new Label("Target Amount ($):"), targetF,
            new Label("Target Date (optional):"), datePicker,
            balLbl,
            new Label("Initial Deposit ($, optional):"), initF
        );
        content.setPadding(new Insets(10));
        d.getDialogPane().setContent(content);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        d.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) return;
            try {
                double target = Double.parseDouble(targetF.getText().trim());
                LocalDate date = datePicker.getValue();
                SavingsGoal goal = goalService.createGoal(accountId, nameF.getText().trim(), target, date);

                double initDeposit = Double.parseDouble(initF.getText().trim());
                if (initDeposit > 0) {
                    goalService.addToGoal(goal.getGoalId(), initDeposit, accountId);
                }
                MainApp.showInfo("Goal Created!", "🎯 Goal '" + goal.getGoalName() + "' created!");
                MainApp.showSavingsGoals(accountId);
            } catch (Exception ex) {
                MainApp.showError(ex.getMessage());
            }
        });
    }

    private void showAddMoneyDialog(SavingsGoal goal) {
        double balance = accountService.getAccountById(accountId).getBalance();
        double remaining = goal.getTargetAmount() - goal.getSavedAmount();

        TextInputDialog td = new TextInputDialog();
        td.setTitle("Add to Goal");
        td.setHeaderText("Add money to: " + goal.getGoalName());
        td.setContentText(String.format(
                "Balance: $%.2f  |  Still need: $%.2f\nAmount to transfer:", balance, remaining));

        td.showAndWait().ifPresent(val -> {
            try {
                double amount = Double.parseDouble(val.trim());
                SavingsGoal updated = goalService.addToGoal(goal.getGoalId(), amount, accountId);
                if (updated.isCompleted()) {
                    MainApp.showInfo("🎉 Goal Reached!",
                            "Congratulations! You saved up for: " + goal.getGoalName() + "!");
                } else {
                    MainApp.showInfo("Saved!",
                            "$" + String.format("%.2f", amount) + " added to your goal!");
                }
                MainApp.showSavingsGoals(accountId);
            } catch (Exception ex) {
                MainApp.showError(ex.getMessage());
            }
        });
    }

    private Label sectionLbl(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lbl.setTextFill(Color.web("#1F4E79"));
        lbl.setPadding(new Insets(6, 0, 2, 0));
        return lbl;
    }

    public BorderPane getRoot() { return root; }
}
