# Data Directory — STAGE 2: Storage

This folder contains JSON files used by `JsonStorage.java` for persistence.

## File Structure

### users.json
Stores all registered users (children and parents).
- `userId`: unique identifier
- `name`: user's name
- `age`: user's age
- `pinHash`: SHA-256 hash of PIN
- `role`: "child" or "parent"
- `loginFailures`: failed login attempts counter
- `locked`: account lock status

### accounts.json
Stores bank accounts linked to users.
- `accountId`: unique identifier
- `userId`: owner's user ID
- `accountType`: "CURRENT" or "SAVINGS"
- `balance`: current balance
- `createdDate`: account creation date

### tasks.json
Stores tasks created by parents for children.
- `taskId`: unique identifier
- `parentUserId`: who created the task
- `childUserId`: who should complete it
- `title`: task name
- `description`: task details
- `rewardAmount`: money earned on completion
- `dueDate`: deadline (nullable)
- `status`: "PENDING", "COMPLETED", "APPROVED", "REJECTED"

### transactions.json
Records all financial operations.
- `transactionId`: unique identifier
- `accountId`: affected account
- `type`: "DEPOSIT", "WITHDRAWAL", "TASK_REWARD", "SAVINGS_TRANSFER"
- `amount`: transaction amount
- `balanceAfter`: balance after operation
- `description`: transaction note
- `dateTime`: timestamp

### goals.json
Stores savings goals set by children.
- `goalId`: unique identifier
- `accountId`: linked account
- `goalName`: goal description
- `targetAmount`: target sum
- `savedAmount`: current progress
- `targetDate`: deadline (nullable)
- `completed`: goal completion status

## Usage
These files are read/written by `JsonStorage.java` using the org.json library.
Never edit manually while the application is running.
