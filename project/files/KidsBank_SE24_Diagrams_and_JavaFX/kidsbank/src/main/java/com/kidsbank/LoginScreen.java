package com.kidsbank;

import com.kidsbank.model.Account;
import com.kidsbank.model.AccountType;
import com.kidsbank.service.AccountService;
import com.kidsbank.service.AuthService;
import com.kidsbank.service.AuthService.LoginResult;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

import java.util.List;

/**
 * Login / Welcome screen. Users select role and enter PIN.
 * Group SE-24 - Virtual Bank for Kids
 */
public class LoginScreen {

    private final AuthService authService;
    private final AccountService accountService;
    private VBox root;

    // "child" or "parent"
    private String selectedRole = null;
    private int failureCount = 0;

    public LoginScreen(AuthService authService, AccountService accountService) {
        this.authService = authService;
        this.accountService = accountService;
        buildUI();
    }

    private void buildUI() {
        root = new VBox(18);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #EBF3FB;");

        // Banner
        Label title = new Label("🏦 KidsBank");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#1F4E79"));

        Label subtitle = new Label("Virtual Bank for Kids — Group SE-24");
        subtitle.setFont(Font.font("Arial", FontPosture.ITALIC, 13));
        subtitle.setTextFill(Color.web("#2E75B6"));

        Separator sep = new Separator();

        // Role selection
        Label roleLabel = new Label("Who are you?");
        roleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 15));

        HBox roleBox = new HBox(20);
        roleBox.setAlignment(Pos.CENTER);
        Button childBtn  = roleButton("👧 I'm a Child",  "child");
        Button parentBtn = roleButton("👨‍👩 I'm a Parent", "parent");
        roleBox.getChildren().addAll(childBtn, parentBtn);

        // Name field
        Label nameLabel = new Label("Your Name:");
        nameLabel.setFont(Font.font("Arial", 13));
        TextField nameField = new TextField();
        nameField.setPromptText("Enter your name");
        nameField.setMaxWidth(260);
        nameField.setStyle("-fx-font-size:13px;");

        // PIN field
        Label pinLabel = new Label("PIN (4 digits):");
        pinLabel.setFont(Font.font("Arial", 13));
        PasswordField pinField = new PasswordField();
        pinField.setPromptText("● ● ● ●");
        pinField.setMaxWidth(260);
        pinField.setStyle("-fx-font-size:13px;");

        // Error label
        Label errorLabel = new Label("");
        errorLabel.setTextFill(Color.RED);
        errorLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        // Login button
        Button loginBtn = MainApp.makePrimaryBtn("Login →");
        loginBtn.setMinWidth(200);

        Label signInHint = new Label(
                "Sign in: pick Parent or Child, then enter your name and 4-digit PIN.");
        signInHint.setFont(Font.font("Arial", 11));
        signInHint.setTextFill(Color.web("#5D6D7E"));
        signInHint.setWrapText(true);
        signInHint.setMaxWidth(320);
        signInHint.setTextAlignment(TextAlignment.CENTER);

        Label registerHint = new Label("New child? A parent must create your account first.");
        registerHint.setFont(Font.font("Arial", FontPosture.ITALIC, 11));
        registerHint.setTextFill(Color.GRAY);

        Hyperlink registerParentLink = new Hyperlink("New parent? Create your account");
        registerParentLink.setOnAction(e -> showRegisterParentDialog());
        registerParentLink.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        // Role button highlight logic
        childBtn.setOnAction(e -> {
            selectedRole = "child";
            highlightRoleBtn(childBtn, parentBtn);
            errorLabel.setText("");
        });
        parentBtn.setOnAction(e -> {
            selectedRole = "parent";
            highlightRoleBtn(parentBtn, childBtn);
            errorLabel.setText("");
        });

        // Login logic
        loginBtn.setOnAction(e -> {
            errorLabel.setText("");
            String name = nameField.getText().trim();
            String pin  = pinField.getText().trim();

            if (selectedRole == null) { errorLabel.setText("Please select your role."); return; }
            if (name.isEmpty())       { errorLabel.setText("Please enter your name."); return; }
            if (pin.isEmpty())        { errorLabel.setText("Please enter your PIN."); return; }
            if (!pin.matches("\\d{4}")) { errorLabel.setText("PIN must be exactly 4 digits."); pinField.clear(); return; }

            LoginResult result = authService.login(name, pin, selectedRole);
            switch (result) {
                case SUCCESS:
                    failureCount = 0;
                    // Ensure child has at least one account
                    if ("child".equals(selectedRole)) {
                        List<Account> accs = accountService.getAccountsForUser(
                                authService.getCurrentUser().getUserId());
                        if (accs.isEmpty()) {
                            accountService.createAccount(
                                    authService.getCurrentUser().getUserId(), AccountType.CURRENT);
                        }
                        MainApp.showChildDashboard();
                    } else {
                        MainApp.showParentDashboard();
                    }
                    break;
                case WRONG_PIN:
                    failureCount++;
                    int remaining = 3 - failureCount;
                    if (remaining <= 0) {
                        errorLabel.setText("Account locked! Contact a parent to reset.");
                    } else {
                        errorLabel.setText("Incorrect PIN. " + remaining + " attempt(s) remaining.");
                    }
                    pinField.clear();
                    break;
                case ACCOUNT_LOCKED:
                    errorLabel.setText("This account is locked. Ask a parent to reset the PIN.");
                    break;
                case USER_NOT_FOUND:
                    errorLabel.setText("No " + selectedRole + " named '" + name + "' found.");
                    break;
            }
        });

        // Allow Enter key to trigger login
        pinField.setOnAction(e -> loginBtn.fire());

        root.getChildren().addAll(
                title, subtitle, sep,
                signInHint,
                roleLabel, roleBox,
                nameLabel, nameField,
                pinLabel, pinField,
                errorLabel, loginBtn,
                registerHint,
                registerParentLink
        );
    }

    /** First-time parent setup from the welcome screen (no existing login required). */
    private void showRegisterParentDialog() {
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle("Create parent account");
        d.setHeaderText("Choose your display name and a 4-digit PIN. You can sign in with these on the next launch too.");

        TextField nameF = new TextField();
        nameF.setPromptText("Your name");
        PasswordField pinF = new PasswordField();
        pinF.setPromptText("4-digit PIN");
        PasswordField pin2F = new PasswordField();
        pin2F.setPromptText("Confirm PIN");

        VBox content = new VBox(10,
                new Label("Name:"), nameF,
                new Label("PIN:"), pinF,
                new Label("Confirm PIN:"), pin2F);
        content.setPadding(new Insets(10));
        d.getDialogPane().setContent(content);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        d.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) return;
            try {
                String name = nameF.getText().trim();
                String pin = pinF.getText().trim();
                String pin2 = pin2F.getText().trim();
                if (name.isEmpty()) {
                    MainApp.showError("Please enter your name.");
                    return;
                }
                if (!pin.matches("\\d{4}")) {
                    MainApp.showError("PIN must be exactly 4 digits.");
                    return;
                }
                if (!pin.equals(pin2)) {
                    MainApp.showError("PINs do not match.");
                    return;
                }
                authService.registerUser(name, 30, pin, "parent");
                LoginResult lr = authService.login(name, pin, "parent");
                if (lr == LoginResult.SUCCESS) {
                    MainApp.showParentDashboard();
                } else {
                    MainApp.showInfo("Account created",
                            "Your parent account was saved. Please sign in with your name and PIN.");
                }
            } catch (Exception ex) {
                MainApp.showError(ex.getMessage());
            }
        });
    }

    private Button roleButton(String text, String role) {
        Button btn = new Button(text);
        btn.setMinWidth(180);
        btn.setMinHeight(50);
        btn.setStyle("-fx-background-color:#FFFFFF; -fx-border-color:#1F4E79; "
                + "-fx-border-radius:8; -fx-background-radius:8; "
                + "-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#1F4E79;");
        return btn;
    }

    private void highlightRoleBtn(Button selected, Button other) {
        selected.setStyle("-fx-background-color:#1F4E79; -fx-border-color:#1F4E79; "
                + "-fx-border-radius:8; -fx-background-radius:8; "
                + "-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:white;");
        other.setStyle("-fx-background-color:#FFFFFF; -fx-border-color:#1F4E79; "
                + "-fx-border-radius:8; -fx-background-radius:8; "
                + "-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#1F4E79;");
    }

    public VBox getRoot() { return root; }
}
