package com.kidsbank.model;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a task created by a parent for a child to complete and earn money.
 * Group SE-24 - Virtual Bank for Kids
 */
public class Task {
    private String taskId;
    private String parentUserId;
    private String childUserId;
    private String title;
    private String description;
    private double rewardAmount;
    private LocalDate dueDate;   // nullable
    private TaskStatus status;

    /** Constructor for creating a new task. */
    public Task(String parentUserId, String childUserId, String title,
                String description, double rewardAmount, LocalDate dueDate) {
        this.taskId = UUID.randomUUID().toString();
        this.parentUserId = parentUserId;
        this.childUserId = childUserId;
        this.title = title;
        this.description = description;
        this.rewardAmount = rewardAmount;
        this.dueDate = dueDate;
        this.status = TaskStatus.PENDING;
    }

    /** Constructor for loading from file. */
    public Task(String taskId, String parentUserId, String childUserId, String title,
                String description, double rewardAmount, LocalDate dueDate, TaskStatus status) {
        this.taskId = taskId;
        this.parentUserId = parentUserId;
        this.childUserId = childUserId;
        this.title = title;
        this.description = description;
        this.rewardAmount = rewardAmount;
        this.dueDate = dueDate;
        this.status = status;
    }

    // Getters & setters
    public String getTaskId() { return taskId; }
    public String getParentUserId() { return parentUserId; }
    public String getChildUserId() { return childUserId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public double getRewardAmount() { return rewardAmount; }
    public LocalDate getDueDate() { return dueDate; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    @Override
    public String toString() {
        return title + " | Reward: $" + String.format("%.2f", rewardAmount) + " | " + status;
    }
}
