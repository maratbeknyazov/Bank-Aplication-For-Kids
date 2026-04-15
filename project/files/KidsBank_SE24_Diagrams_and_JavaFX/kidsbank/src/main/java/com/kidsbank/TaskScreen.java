package com.kidsbank;

import com.kidsbank.model.*;
import com.kidsbank.service.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Task management screen — shows different views for parent vs child.
 * Group SE-24 - Virtual Bank for Kids
 */
public class TaskScreen {

    private final AuthService authService;
    private final TaskService taskService;
    private final AccountService accountService;
    private final boolean isParent;
    private BorderPane root;

    public TaskScreen(AuthService authService, TaskService taskService,
                      AccountService accountService, boolean isParent) {
        this.authService    = authService;
        this.taskService    = taskService;
        this.accountService = accountService;
        this.isParent       = isParent;
        buildUI();
    }

    private void buildUI() {
        root = new BorderPane();
        root.setStyle("-fx-background-color:#EBF3FB;");

        User user = authService.getCurrentUser();

        // Header
        HBox header = new HBox();
        header.setPadding(new Insets(14, 20, 14, 20));
        header.setStyle("-fx-background-color:#1F4E79;");
        header.setAlignment(Pos.CENTER_LEFT);
        Label hLbl = new Label(isParent ? "📋  Task Management" : "📋  My Tasks");
        hLbl.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        hLbl.setTextFill(Color.WHITE);
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button backBtn = new Button("← Back");
        backBtn.setStyle("-fx-background-color:#2E75B6; -fx-text-fill:white; "
                + "-fx-font-weight:bold; -fx-background-radius:5;");
        backBtn.setOnAction(e -> {
            if (isParent) MainApp.showParentDashboard();
            else MainApp.showChildDashboard();
        });
        header.getChildren().addAll(hLbl, sp, backBtn);

        VBox content = new VBox(14);
        content.setPadding(new Insets(20));

        if (isParent) {
            buildParentView(content, user);
        } else {
            buildChildView(content, user);
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:transparent; -fx-background:transparent;");

        root.setTop(header);
        root.setCenter(scroll);
    }

    // ======================== PARENT VIEW ========================
    private void buildParentView(VBox content, User parent) {
        // Add task button
        Button addBtn = MainApp.makePrimaryBtn("+ Add New Task");
        addBtn.setOnAction(e -> showAddTaskDialog(parent));

        content.getChildren().add(addBtn);

        List<Task> tasks = taskService.getTasksForParent(parent.getUserId());

        // Completed tasks needing approval
        List<Task> toApprove = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED).toList();

        if (!toApprove.isEmpty()) {
            content.getChildren().add(sectionLabel("⏳ Awaiting Your Approval (" + toApprove.size() + ")"));
            for (Task t : toApprove) content.getChildren().add(parentTaskCard(t, parent, true));
        }

        // Pending tasks
        List<Task> pending = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.PENDING).toList();
        if (!pending.isEmpty()) {
            content.getChildren().add(sectionLabel("📋 Active Tasks"));
            for (Task t : pending) content.getChildren().add(parentTaskCard(t, parent, false));
        }

        // Approved/rejected history
        List<Task> done = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.APPROVED || t.getStatus() == TaskStatus.REJECTED).toList();
        if (!done.isEmpty()) {
            content.getChildren().add(sectionLabel("✅ Completed History"));
            for (Task t : done) content.getChildren().add(parentTaskCard(t, parent, false));
        }

        if (tasks.isEmpty()) {
            Label empty = new Label("No tasks yet. Add one above!");
            empty.setFont(Font.font("Arial", FontPosture.ITALIC, 13));
            empty.setTextFill(Color.GRAY);
            content.getChildren().add(empty);
        }
    }

    private VBox parentTaskCard(Task task, User parent, boolean showApproveReject) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(12, 16, 12, 16));
        card.setStyle("-fx-background-color:white; -fx-background-radius:8; "
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),6,0,0,1);");

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        Label titleLbl = new Label(task.getTitle());
        titleLbl.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label rewardLbl = new Label("$" + String.format("%.2f", task.getRewardAmount()));
        rewardLbl.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        rewardLbl.setTextFill(Color.web("#1E8449"));
        Label statusLbl = new Label(task.getStatus().name());
        statusLbl.setFont(Font.font("Arial", 11));
        statusLbl.setTextFill(statusColor(task.getStatus()));
        top.getChildren().addAll(titleLbl, sp, rewardLbl, statusLbl);

        card.getChildren().add(top);

        if (!task.getDescription().isEmpty()) {
            Label descLbl = new Label(task.getDescription());
            descLbl.setFont(Font.font("Arial", 11));
            descLbl.setTextFill(Color.GRAY);
            card.getChildren().add(descLbl);
        }

        if (task.getDueDate() != null) {
            Label dueLbl = new Label("Due: " + task.getDueDate());
            dueLbl.setFont(Font.font("Arial", FontPosture.ITALIC, 10));
            dueLbl.setTextFill(Color.GRAY);
            card.getChildren().add(dueLbl);
        }

        if (showApproveReject) {
            HBox btnRow = new HBox(10);
            btnRow.setPadding(new Insets(6, 0, 0, 0));

            Button approveBtn = MainApp.makeSuccessBtn("✅ Approve");
            Button rejectBtn  = MainApp.makeDangerBtn("❌ Reject");

            approveBtn.setOnAction(e -> {
                // Get child's account
                List<Account> accs = accountService.getAccountsForUser(task.getChildUserId());
                if (accs.isEmpty()) { MainApp.showError("Child has no account."); return; }
                taskService.approveTask(task.getTaskId(), accs.get(0).getAccountId());
                MainApp.showInfo("Approved!", "Task approved! $"
                        + String.format("%.2f", task.getRewardAmount()) + " added to child's balance.");
                MainApp.showTaskScreen(true);
            });
            rejectBtn.setOnAction(e -> {
                taskService.rejectTask(task.getTaskId());
                MainApp.showInfo("Rejected", "Task has been rejected.");
                MainApp.showTaskScreen(true);
            });
            btnRow.getChildren().addAll(approveBtn, rejectBtn);
            card.getChildren().add(btnRow);
        }

        return card;
    }

    // ======================== CHILD VIEW ========================
    private void buildChildView(VBox content, User child) {
        List<Task> tasks = taskService.getTasksForChild(child.getUserId());

        // Pending tasks child can mark done
        List<Task> pending = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.PENDING).toList();
        if (!pending.isEmpty()) {
            content.getChildren().add(sectionLabel("📋 Tasks To Complete"));
            for (Task t : pending) content.getChildren().add(childTaskCard(t));
        }

        // Waiting approval
        List<Task> waiting = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED).toList();
        if (!waiting.isEmpty()) {
            content.getChildren().add(sectionLabel("⏳ Waiting for Parent Approval"));
            for (Task t : waiting) content.getChildren().add(childTaskCard(t));
        }

        // Approved
        List<Task> approved = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.APPROVED).toList();
        if (!approved.isEmpty()) {
            content.getChildren().add(sectionLabel("✅ Completed & Approved"));
            for (Task t : approved) content.getChildren().add(childTaskCard(t));
        }

        if (tasks.isEmpty()) {
            Label empty = new Label("No tasks yet! Ask a parent to set you a task.");
            empty.setFont(Font.font("Arial", FontPosture.ITALIC, 13));
            empty.setTextFill(Color.GRAY);
            content.getChildren().add(empty);
        }
    }

    private VBox childTaskCard(Task task) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(12, 16, 12, 16));
        card.setStyle("-fx-background-color:white; -fx-background-radius:8; "
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),6,0,0,1);");

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        Label titleLbl = new Label(task.getTitle());
        titleLbl.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label rewardLbl = new Label("🏆 $" + String.format("%.2f", task.getRewardAmount()));
        rewardLbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        rewardLbl.setTextFill(Color.web("#1E8449"));
        top.getChildren().addAll(titleLbl, sp, rewardLbl);
        card.getChildren().add(top);

        Label statusLbl = new Label("Status: " + task.getStatus().name());
        statusLbl.setFont(Font.font("Arial", 11));
        statusLbl.setTextFill(statusColor(task.getStatus()));
        card.getChildren().add(statusLbl);

        if (task.getDueDate() != null) {
            Label dueLbl = new Label("Due: " + task.getDueDate());
            dueLbl.setFont(Font.font("Arial", FontPosture.ITALIC, 10));
            dueLbl.setTextFill(Color.GRAY);
            card.getChildren().add(dueLbl);
        }

        if (task.getStatus() == TaskStatus.PENDING) {
            Button doneBtn = MainApp.makeSuccessBtn("✅ Mark as Done");
            doneBtn.setOnAction(e -> {
                taskService.markComplete(task.getTaskId());
                MainApp.showInfo("Great job!", "Task marked as complete! Waiting for parent approval.");
                MainApp.showTaskScreen(false);
            });
            card.getChildren().add(doneBtn);
        }

        return card;
    }

    // ======================== ADD TASK DIALOG ========================
    private void showAddTaskDialog(User parent) {
        // Pick child
        List<User> children = com.kidsbank.storage.JsonStorage.loadAllUsers().stream()
                .filter(u -> "child".equals(u.getRole())).toList();
        if (children.isEmpty()) { MainApp.showError("No children registered yet."); return; }

        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle("Add New Task");
        d.setHeaderText("Set a task for your child to earn money");

        ComboBox<String> childCb = new ComboBox<>();
        children.forEach(c -> childCb.getItems().add(c.getName()));
        childCb.getSelectionModel().selectFirst();

        TextField titleF  = new TextField(); titleF.setPromptText("Task title (e.g. Wash dishes)");
        TextField descF   = new TextField(); descF.setPromptText("Description (optional)");
        TextField rewardF = new TextField(); rewardF.setPromptText("Reward amount (e.g. 5.00)");
        DatePicker duePicker = new DatePicker();
        duePicker.setPromptText("Due date (optional)");

        VBox content = new VBox(10,
            new Label("Assign to:"), childCb,
            new Label("Task Title:"), titleF,
            new Label("Description:"), descF,
            new Label("Reward ($):"), rewardF,
            new Label("Due Date (optional):"), duePicker
        );
        content.setPadding(new Insets(10));
        d.getDialogPane().setContent(content);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        d.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) return;
            try {
                int idx = childCb.getSelectionModel().getSelectedIndex();
                User child = children.get(idx);
                double reward = Double.parseDouble(rewardF.getText().trim());
                LocalDate due = duePicker.getValue();
                taskService.createTask(parent.getUserId(), child.getUserId(),
                        titleF.getText().trim(), descF.getText().trim(), reward, due);
                MainApp.showInfo("Task Created", "Task assigned to " + child.getName() + "!");
                MainApp.showTaskScreen(true);
            } catch (Exception ex) {
                MainApp.showError(ex.getMessage());
            }
        });
    }

    // ======================== HELPERS ========================
    private Label sectionLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lbl.setTextFill(Color.web("#1F4E79"));
        lbl.setPadding(new Insets(8, 0, 2, 0));
        return lbl;
    }

    private Color statusColor(TaskStatus status) {
        return switch (status) {
            case PENDING   -> Color.web("#D35400");
            case COMPLETED -> Color.web("#2471A3");
            case APPROVED  -> Color.web("#1E8449");
            case REJECTED  -> Color.web("#C0392B");
        };
    }

    public BorderPane getRoot() { return root; }
}
