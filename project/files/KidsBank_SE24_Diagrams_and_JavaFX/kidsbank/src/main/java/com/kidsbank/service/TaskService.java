package com.kidsbank.service;

import com.kidsbank.model.*;
import com.kidsbank.storage.JsonStorage;

import java.time.LocalDate;
import java.util.List;

/**
 * STAGE 3.3: Service — task creation, completion, reward payout.
 * Coordinates Task status changes and AccountService deposits.
 * Group SE-24 - Virtual Bank for Kids
 */
public class TaskService {

    private final AccountService accountService;

    public TaskService(AccountService accountService) {
        this.accountService = accountService;
    }

    /** Creates a new task. */
    public Task createTask(String parentId, String childId, String title,
                           String description, double reward, LocalDate dueDate) {
        if (title == null || title.trim().isEmpty())
            throw new IllegalArgumentException("Task title cannot be empty.");
        if (reward <= 0)
            throw new IllegalArgumentException("Reward must be greater than zero.");
        Task task = new Task(parentId, childId, title.trim(), description, reward, dueDate);
        JsonStorage.saveTask(task);
        return task;
    }

    /** Returns all tasks for a given child. */
    public List<Task> getTasksForChild(String childId) {
        return JsonStorage.findTasksByChildId(childId);
    }

    /** Returns all tasks created by a given parent. */
    public List<Task> getTasksForParent(String parentId) {
        return JsonStorage.findTasksByParentId(parentId);
    }

    /** Child marks a task as completed (awaiting parent approval). */
    public void markComplete(String taskId) {
        Task task = findTaskById(taskId);
        if (task.getStatus() != TaskStatus.PENDING)
            throw new IllegalStateException("Only pending tasks can be marked complete.");
        task.setStatus(TaskStatus.COMPLETED);
        JsonStorage.saveTask(task);
    }

    /**
     * Parent approves a completed task — reward is deposited to the child's account.
     * @param accountId the child's account to receive the reward
     */
    public void approveTask(String taskId, String accountId) {
        Task task = findTaskById(taskId);
        if (task.getStatus() != TaskStatus.COMPLETED)
            throw new IllegalStateException("Only completed tasks can be approved.");
        task.setStatus(TaskStatus.APPROVED);
        JsonStorage.saveTask(task);
        accountService.creditTaskReward(accountId, task.getRewardAmount(), task.getTitle());
    }

    /** Parent rejects a completed task — no reward paid. */
    public void rejectTask(String taskId) {
        Task task = findTaskById(taskId);
        if (task.getStatus() != TaskStatus.COMPLETED)
            throw new IllegalStateException("Only completed tasks can be rejected.");
        task.setStatus(TaskStatus.REJECTED);
        JsonStorage.saveTask(task);
    }

    private Task findTaskById(String taskId) {
        return JsonStorage.loadAllTasks().stream()
            .filter(t -> t.getTaskId().equals(taskId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
    }
}
