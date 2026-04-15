package com.kidsbank;

import com.kidsbank.model.Transaction;
import com.kidsbank.model.TransactionType;
import com.kidsbank.service.AccountService;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Screen displaying transaction history with filter options.
 * Group SE-24 - Virtual Bank for Kids
 */
public class TransactionScreen {

    private final AccountService accountService;
    private final String accountId;
    private final String accountName;
    private BorderPane root;
    private VBox listBox;
    private List<Transaction> allTransactions;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy  HH:mm");

    public TransactionScreen(AccountService accountService, String accountId, String accountName) {
        this.accountService = accountService;
        this.accountId      = accountId;
        this.accountName    = accountName;
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
        Label hLbl = new Label("📜  Transaction History — " + accountName);
        hLbl.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        hLbl.setTextFill(Color.WHITE);
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button backBtn = new Button("← Back");
        backBtn.setStyle("-fx-background-color:#2E75B6; -fx-text-fill:white; "
                + "-fx-font-weight:bold; -fx-background-radius:5;");
        backBtn.setOnAction(e -> MainApp.showLoginScreen()); // simplified back
        header.getChildren().addAll(hLbl, sp, backBtn);

        // Filter bar
        HBox filterBar = new HBox(10);
        filterBar.setPadding(new Insets(12, 20, 8, 20));
        filterBar.setAlignment(Pos.CENTER_LEFT);
        Label filterLbl = new Label("Filter:");
        filterLbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        ToggleGroup tg = new ToggleGroup();
        ToggleButton allBtn      = filterToggle("All",          tg);
        ToggleButton depositBtn  = filterToggle("Deposits",     tg);
        ToggleButton withdrawBtn = filterToggle("Withdrawals",  tg);
        ToggleButton rewardBtn   = filterToggle("Task Rewards", tg);
        allBtn.setSelected(true);

        allBtn.setOnAction(e      -> renderList(null));
        depositBtn.setOnAction(e  -> renderList(TransactionType.DEPOSIT));
        withdrawBtn.setOnAction(e -> renderList(TransactionType.WITHDRAWAL));
        rewardBtn.setOnAction(e   -> renderList(TransactionType.TASK_REWARD));

        filterBar.getChildren().addAll(filterLbl, allBtn, depositBtn, withdrawBtn, rewardBtn);

        // Transaction list
        listBox = new VBox(0);
        ScrollPane scroll = new ScrollPane(listBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:transparent; -fx-background:transparent;");

        // Balance footer
        allTransactions = accountService.getTransactions(accountId);
        double balance = accountService.getAccountById(accountId).getBalance();
        Label footerLbl = new Label("Running Balance: $" + String.format("%.2f", balance)
                + "   |   Total Transactions: " + allTransactions.size());
        footerLbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        footerLbl.setTextFill(Color.web("#1E8449"));
        footerLbl.setPadding(new Insets(10, 20, 10, 20));
        footerLbl.setStyle("-fx-background-color:white; -fx-border-color:#DDDDDD; -fx-border-width:1 0 0 0;");

        root.setTop(new VBox(header, filterBar));
        root.setCenter(scroll);
        root.setBottom(footerLbl);

        renderList(null);
    }

    private void renderList(TransactionType filter) {
        listBox.getChildren().clear();

        List<Transaction> list = filter == null ? allTransactions :
                allTransactions.stream()
                        .filter(t -> t.getType() == filter)
                        .collect(Collectors.toList());

        if (list.isEmpty()) {
            Label empty = new Label("No transactions found.");
            empty.setFont(Font.font("Arial", FontPosture.ITALIC, 13));
            empty.setTextFill(Color.GRAY);
            empty.setPadding(new Insets(30));
            listBox.getChildren().add(empty);
            return;
        }

        for (int i = 0; i < list.size(); i++) {
            Transaction tx = list.get(i);
            HBox row = buildRow(tx, i % 2 == 0);
            listBox.getChildren().add(row);
        }
    }

    private HBox buildRow(Transaction tx, boolean even) {
        HBox row = new HBox(12);
        row.setPadding(new Insets(10, 18, 10, 18));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color:" + (even ? "#FFFFFF" : "#F5F9FF") + ";");

        // Icon
        String icon = switch (tx.getType()) {
            case DEPOSIT         -> "💰";
            case WITHDRAWAL      -> "💸";
            case TASK_REWARD     -> "🏆";
            case SAVINGS_TRANSFER-> "🎯";
        };
        Label iconLbl = new Label(icon);
        iconLbl.setFont(Font.font(18));
        iconLbl.setMinWidth(28);

        // Description + date
        VBox descBox = new VBox(2);
        Label descLbl = new Label(tx.getDescription().isEmpty()
                ? tx.getType().getDisplayName() : tx.getDescription());
        descLbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        Label dateLbl = new Label(tx.getDateTime().format(FMT));
        dateLbl.setFont(Font.font("Arial", 10));
        dateLbl.setTextFill(Color.GRAY);
        descBox.getChildren().addAll(descLbl, dateLbl);

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        // Amount
        boolean isCredit = tx.getType() == TransactionType.DEPOSIT
                        || tx.getType() == TransactionType.TASK_REWARD;
        Label amtLbl = new Label(tx.getSignedAmount());
        amtLbl.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        amtLbl.setTextFill(isCredit ? Color.web("#1E8449") : Color.web("#C0392B"));

        // Balance after
        Label balLbl = new Label("bal: $" + String.format("%.2f", tx.getBalanceAfter()));
        balLbl.setFont(Font.font("Arial", 10));
        balLbl.setTextFill(Color.GRAY);

        VBox rightBox = new VBox(2);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        rightBox.getChildren().addAll(amtLbl, balLbl);

        row.getChildren().addAll(iconLbl, descBox, sp, rightBox);

        // Hover effect
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color:#D6EAF8;"));
        row.setOnMouseExited(e  -> row.setStyle("-fx-background-color:" + (even ? "#FFFFFF" : "#F5F9FF") + ";"));

        return row;
    }

    private ToggleButton filterToggle(String text, ToggleGroup tg) {
        ToggleButton tb = new ToggleButton(text);
        tb.setToggleGroup(tg);
        tb.setFont(Font.font("Arial", 11));
        tb.setStyle("-fx-background-radius:5;");
        return tb;
    }

    public BorderPane getRoot() { return root; }
}
