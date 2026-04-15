package com.kidsbank.storage;

import com.kidsbank.model.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Handles all JSON file read/write operations for persistence.
 * All data is stored in plain JSON files in the /data directory.
 * Group SE-24 - Virtual Bank for Kids
 */
public class JsonStorage {

    private static final String DATA_DIR = "data";

    static {
        // Ensure data directory exists on startup
        new File(DATA_DIR).mkdirs();
    }

    private static String filePath(String name) {
        return DATA_DIR + File.separator + name + ".json";
    }

    // ======================== READ / WRITE HELPERS ========================

    private static JSONArray readArray(String filename) {
        try {
            String path = filePath(filename);
            if (!new File(path).exists()) return new JSONArray();
            String content = new String(Files.readAllBytes(Paths.get(path)));
            return new JSONArray(content.trim().isEmpty() ? "[]" : content);
        } catch (Exception e) {
            System.err.println("Error reading " + filename + ": " + e.getMessage());
            return new JSONArray();
        }
    }

    private static void writeArray(String filename, JSONArray array) {
        try {
            Files.write(Paths.get(filePath(filename)),
                    array.toString(2).getBytes());
        } catch (Exception e) {
            System.err.println("Error writing " + filename + ": " + e.getMessage());
        }
    }

    // ======================== USER ========================

    public static void saveUser(User user) {
        JSONArray arr = readArray("users");
        // Remove existing entry with same ID
        JSONArray updated = new JSONArray();
        for (int i = 0; i < arr.length(); i++) {
            if (!arr.getJSONObject(i).getString("userId").equals(user.getUserId()))
                updated.put(arr.getJSONObject(i));
        }
        JSONObject obj = new JSONObject();
        obj.put("userId", user.getUserId());
        obj.put("name", user.getName());
        obj.put("age", user.getAge());
        obj.put("pinHash", user.getPinHash());
        obj.put("role", user.getRole());
        obj.put("loginFailures", user.getLoginFailures());
        obj.put("locked", user.isLocked());
        updated.put(obj);
        writeArray("users", updated);
    }

    public static List<User> loadAllUsers() {
        List<User> users = new ArrayList<>();
        JSONArray arr = readArray("users");
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            users.add(new User(
                o.getString("userId"), o.getString("name"),
                o.getInt("age"), o.getString("pinHash"),
                o.getString("role"), o.getInt("loginFailures"),
                o.getBoolean("locked")
            ));
        }
        return users;
    }

    public static Optional<User> findUserByName(String name, String role) {
        return loadAllUsers().stream()
            .filter(u -> u.getName().equalsIgnoreCase(name) && u.getRole().equalsIgnoreCase(role))
            .findFirst();
    }

    public static Optional<User> findUserById(String userId) {
        return loadAllUsers().stream()
            .filter(u -> u.getUserId().equals(userId))
            .findFirst();
    }

    // ======================== ACCOUNT ========================

    public static void saveAccount(Account account) {
        JSONArray arr = readArray("accounts");
        JSONArray updated = new JSONArray();
        for (int i = 0; i < arr.length(); i++) {
            if (!arr.getJSONObject(i).getString("accountId").equals(account.getAccountId()))
                updated.put(arr.getJSONObject(i));
        }
        JSONObject obj = new JSONObject();
        obj.put("accountId", account.getAccountId());
        obj.put("userId", account.getUserId());
        obj.put("accountType", account.getAccountType().name());
        obj.put("balance", account.getBalance());
        obj.put("createdDate", account.getCreatedDate().toString());
        updated.put(obj);
        writeArray("accounts", updated);
    }

    public static List<Account> loadAllAccounts() {
        List<Account> accounts = new ArrayList<>();
        JSONArray arr = readArray("accounts");
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            accounts.add(new Account(
                o.getString("accountId"), o.getString("userId"),
                AccountType.valueOf(o.getString("accountType")),
                o.getDouble("balance"),
                LocalDate.parse(o.getString("createdDate"))
            ));
        }
        return accounts;
    }

    public static List<Account> findAccountsByUserId(String userId) {
        List<Account> result = new ArrayList<>();
        for (Account a : loadAllAccounts())
            if (a.getUserId().equals(userId)) result.add(a);
        return result;
    }

    public static Optional<Account> findAccountById(String accountId) {
        return loadAllAccounts().stream()
            .filter(a -> a.getAccountId().equals(accountId))
            .findFirst();
    }

    // ======================== TRANSACTION ========================

    public static void saveTransaction(Transaction t) {
        JSONArray arr = readArray("transactions");
        JSONObject obj = new JSONObject();
        obj.put("transactionId", t.getTransactionId());
        obj.put("accountId", t.getAccountId());
        obj.put("type", t.getType().name());
        obj.put("amount", t.getAmount());
        obj.put("balanceAfter", t.getBalanceAfter());
        obj.put("description", t.getDescription());
        obj.put("dateTime", t.getDateTime().toString());
        arr.put(obj);
        writeArray("transactions", arr);
    }

    public static List<Transaction> findTransactionsByAccountId(String accountId) {
        List<Transaction> result = new ArrayList<>();
        JSONArray arr = readArray("transactions");
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            if (o.getString("accountId").equals(accountId)) {
                result.add(new Transaction(
                    o.getString("transactionId"), o.getString("accountId"),
                    TransactionType.valueOf(o.getString("type")),
                    o.getDouble("amount"), o.getDouble("balanceAfter"),
                    o.getString("description"),
                    LocalDateTime.parse(o.getString("dateTime"))
                ));
            }
        }
        // Sort newest first
        result.sort((a, b) -> b.getDateTime().compareTo(a.getDateTime()));
        return result;
    }

    // ======================== TASK ========================

    public static void saveTask(Task task) {
        JSONArray arr = readArray("tasks");
        JSONArray updated = new JSONArray();
        for (int i = 0; i < arr.length(); i++) {
            if (!arr.getJSONObject(i).getString("taskId").equals(task.getTaskId()))
                updated.put(arr.getJSONObject(i));
        }
        JSONObject obj = new JSONObject();
        obj.put("taskId", task.getTaskId());
        obj.put("parentUserId", task.getParentUserId());
        obj.put("childUserId", task.getChildUserId());
        obj.put("title", task.getTitle());
        obj.put("description", task.getDescription());
        obj.put("rewardAmount", task.getRewardAmount());
        obj.put("dueDate", task.getDueDate() != null ? task.getDueDate().toString() : "");
        obj.put("status", task.getStatus().name());
        updated.put(obj);
        writeArray("tasks", updated);
    }

    public static List<Task> loadAllTasks() {
        List<Task> tasks = new ArrayList<>();
        JSONArray arr = readArray("tasks");
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            String dueDateStr = o.getString("dueDate");
            LocalDate dueDate = dueDateStr.isEmpty() ? null : LocalDate.parse(dueDateStr);
            tasks.add(new Task(
                o.getString("taskId"), o.getString("parentUserId"),
                o.getString("childUserId"), o.getString("title"),
                o.getString("description"), o.getDouble("rewardAmount"),
                dueDate, TaskStatus.valueOf(o.getString("status"))
            ));
        }
        return tasks;
    }

    public static List<Task> findTasksByChildId(String childId) {
        List<Task> result = new ArrayList<>();
        for (Task t : loadAllTasks())
            if (t.getChildUserId().equals(childId)) result.add(t);
        return result;
    }

    public static List<Task> findTasksByParentId(String parentId) {
        List<Task> result = new ArrayList<>();
        for (Task t : loadAllTasks())
            if (t.getParentUserId().equals(parentId)) result.add(t);
        return result;
    }

    // ======================== SAVINGS GOAL ========================

    public static void saveSavingsGoal(SavingsGoal goal) {
        JSONArray arr = readArray("goals");
        JSONArray updated = new JSONArray();
        for (int i = 0; i < arr.length(); i++) {
            if (!arr.getJSONObject(i).getString("goalId").equals(goal.getGoalId()))
                updated.put(arr.getJSONObject(i));
        }
        JSONObject obj = new JSONObject();
        obj.put("goalId", goal.getGoalId());
        obj.put("accountId", goal.getAccountId());
        obj.put("goalName", goal.getGoalName());
        obj.put("targetAmount", goal.getTargetAmount());
        obj.put("savedAmount", goal.getSavedAmount());
        obj.put("targetDate", goal.getTargetDate() != null ? goal.getTargetDate().toString() : "");
        obj.put("completed", goal.isCompleted());
        updated.put(obj);
        writeArray("goals", updated);
    }

    public static List<SavingsGoal> findAllGoals() {
        List<SavingsGoal> result = new ArrayList<>();
        JSONArray arr = readArray("goals");
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            String ds = o.getString("targetDate");
            result.add(new SavingsGoal(
                o.getString("goalId"), o.getString("accountId"),
                o.getString("goalName"), o.getDouble("targetAmount"),
                o.getDouble("savedAmount"),
                ds.isEmpty() ? null : LocalDate.parse(ds),
                o.getBoolean("completed")
            ));
        }
        return result;
    }

    public static List<SavingsGoal> findGoalsByAccountId(String accountId) {
        List<SavingsGoal> result = new ArrayList<>();
        JSONArray arr = readArray("goals");
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            if (o.getString("accountId").equals(accountId)) {
                String ds = o.getString("targetDate");
                result.add(new SavingsGoal(
                    o.getString("goalId"), o.getString("accountId"),
                    o.getString("goalName"), o.getDouble("targetAmount"),
                    o.getDouble("savedAmount"),
                    ds.isEmpty() ? null : LocalDate.parse(ds),
                    o.getBoolean("completed")
                ));
            }
        }
        return result;
    }
}
