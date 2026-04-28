package com.kidsbank.service;

import com.kidsbank.model.*;
import com.kidsbank.storage.JsonStorage;

import java.util.List;

/**
 * STAGE 3.2: Service — deposit/withdraw/transfer logic.
 * Modifies Account balance, saves via Storage, creates Transactions.
 * Group SE-24 - Virtual Bank for Kids
 */
public class AccountService {

    /**
     * Creates a new account for a user.
     */
    public Account createAccount(String userId, AccountType type) {
        Account account = new Account(userId, type);
        JsonStorage.saveAccount(account);
        return account;
    }

    /**
     * Returns all accounts for a given user.
     */
    public List<Account> getAccountsForUser(String userId) {
        return JsonStorage.findAccountsByUserId(userId);
    }

    /**
     * Returns account by ID, or throws if not found.
     */
    public Account getAccountById(String accountId) {
        return JsonStorage.findAccountById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));
    }

    /**
     * Deposits money into an account and logs the transaction.
     * @throws IllegalArgumentException if amount <= 0
     */
    public Transaction deposit(String accountId, double amount, String description) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit amount must be greater than zero.");
        Account account = getAccountById(accountId);
        account.deposit(amount);
        JsonStorage.saveAccount(account);

        Transaction tx = new Transaction(accountId, TransactionType.DEPOSIT,
                amount, account.getBalance(), description);
        JsonStorage.saveTransaction(tx);
        return tx;
    }

    /**
     * Withdraws money from an account and logs the transaction.
     * @throws IllegalArgumentException if amount <= 0 or insufficient funds
     */
    public Transaction withdraw(String accountId, double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal amount must be greater than zero.");
        Account account = getAccountById(accountId);
        boolean success = account.withdraw(amount);
        if (!success) throw new IllegalArgumentException("Insufficient funds. Balance: $"
                + String.format("%.2f", account.getBalance()));
        JsonStorage.saveAccount(account);

        Transaction tx = new Transaction(accountId, TransactionType.WITHDRAWAL,
                amount, account.getBalance(), "Withdrawal");
        JsonStorage.saveTransaction(tx);
        return tx;
    }

    /**
     * Credits a task reward to an account (internal — called by TaskService).
     */
    public Transaction creditTaskReward(String accountId, double amount, String taskTitle) {
        Account account = getAccountById(accountId);
        account.deposit(amount);
        JsonStorage.saveAccount(account);

        Transaction tx = new Transaction(accountId, TransactionType.TASK_REWARD,
                amount, account.getBalance(), "Task Reward: " + taskTitle);
        JsonStorage.saveTransaction(tx);
        return tx;
    }

    /**
     * Records a savings transfer transaction (internal — called by SavingsGoalService).
     */
    public Transaction savingsTransfer(String accountId, double amount, String goalName) {
        Account account = getAccountById(accountId);
        boolean success = account.withdraw(amount);
        if (!success) throw new IllegalArgumentException("Insufficient funds for savings transfer.");
        JsonStorage.saveAccount(account);

        Transaction tx = new Transaction(accountId, TransactionType.SAVINGS_TRANSFER,
                amount, account.getBalance(), "Savings: " + goalName);
        JsonStorage.saveTransaction(tx);
        return tx;
    }

    /**
     * Returns transaction history for an account, sorted newest-first.
     */
    public List<Transaction> getTransactions(String accountId) {
        return JsonStorage.findTransactionsByAccountId(accountId);
    }
}
