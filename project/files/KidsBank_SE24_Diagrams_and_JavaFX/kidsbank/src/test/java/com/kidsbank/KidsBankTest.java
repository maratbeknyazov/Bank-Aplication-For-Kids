package com.kidsbank;

import com.kidsbank.model.*;
import com.kidsbank.service.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.time.LocalDate;

/**
* STAGE 4: Tests — verify Service logic before building UI.
* Ensures deposit/withdraw/task completion works correctly.
* Group SE-24 - Virtual Bank for Kids
*/
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KidsBankTest {

   private static AccountService accountService;
   private static AuthService    authService;
   private static TaskService    taskService;
   private static SavingsGoalService goalService;

   @BeforeAll
   static void setup() {
       // Use a test data directory to avoid polluting real data
       System.setProperty("kidsbank.datadir", "test-data");
       new File("test-data").mkdirs();

       accountService = new AccountService();
       authService    = new AuthService();
       taskService    = new TaskService(accountService);
       goalService    = new SavingsGoalService(accountService);
   }

   @AfterAll
   static void cleanup() {
       // Remove test data files
       File dir = new File("test-data");
       for (File f : dir.listFiles()) f.delete();
       dir.delete();
   }

   // ======================== AUTH TESTS ========================

   @Test @Order(1)
   void testRegisterChild() {
       User child = authService.registerUser("Emma", 10, "1234", "child");
       assertNotNull(child);
       assertEquals("Emma", child.getName());
       assertEquals("child", child.getRole());
   }

   @Test @Order(2)
   void testRegisterParent() {
       User parent = authService.registerUser("Alice", 35, "5678", "parent");
       assertNotNull(parent);
       assertEquals("parent", parent.getRole());
   }

   @Test @Order(3)
   void testLoginSuccess() {
       AuthService.LoginResult result = authService.login("Emma", "1234", "child");
       assertEquals(AuthService.LoginResult.SUCCESS, result);
       assertNotNull(authService.getCurrentUser());
       assertEquals("Emma", authService.getCurrentUser().getName());
   }

   @Test @Order(4)
   void testLoginWrongPin() {
       AuthService.LoginResult result = authService.login("Alice", "0000", "parent");
       assertEquals(AuthService.LoginResult.WRONG_PIN, result);
   }

   @Test @Order(5)
   void testLoginUserNotFound() {
       AuthService.LoginResult result = authService.login("Ghost", "9999", "child");
       assertEquals(AuthService.LoginResult.USER_NOT_FOUND, result);
   }

   @Test @Order(6)
   void testRegisterDuplicateThrows() {
       assertThrows(IllegalArgumentException.class,
           () -> authService.registerUser("Emma", 10, "1234", "child"));
   }

   @Test @Order(7)
   void testInvalidPinFormatThrows() {
       assertThrows(IllegalArgumentException.class,
           () -> authService.registerUser("Bob", 8, "12", "child")); // too short
   }

   // ======================== ACCOUNT TESTS ========================

   @Test @Order(8)
   void testCreateAccount() {
       authService.login("Emma", "1234", "child");
       String userId = authService.getCurrentUser().getUserId();
       Account acc = accountService.createAccount(userId, AccountType.CURRENT);
       assertNotNull(acc);
       assertEquals(0.0, acc.getBalance());
       assertEquals(AccountType.CURRENT, acc.getAccountType());
   }

   @Test @Order(9)
   void testDeposit() {
       authService.login("Emma", "1234", "child");
       String userId = authService.getCurrentUser().getUserId();
       Account acc = accountService.getAccountsForUser(userId).get(0);
       Transaction tx = accountService.deposit(acc.getAccountId(), 50.0, "Test deposit");
       assertEquals(50.0, tx.getAmount());
       assertEquals(50.0, accountService.getAccountById(acc.getAccountId()).getBalance());
   }

   @Test @Order(10)
   void testWithdraw() {
       authService.login("Emma", "1234", "child");
       String userId = authService.getCurrentUser().getUserId();
       Account acc = accountService.getAccountsForUser(userId).get(0);
       accountService.deposit(acc.getAccountId(), 100.0, "Setup");
       Transaction tx = accountService.withdraw(acc.getAccountId(), 30.0);
       assertEquals(30.0, tx.getAmount());
       // Balance should be 100 + 50 (from prev test) - 30 = 120
       assertTrue(accountService.getAccountById(acc.getAccountId()).getBalance() >= 70.0);
   }

   @Test @Order(11)
   void testWithdrawInsufficientFunds() {
       authService.login("Emma", "1234", "child");
       String userId = authService.getCurrentUser().getUserId();
       Account acc = accountService.getAccountsForUser(userId).get(0);
       assertThrows(IllegalArgumentException.class,
           () -> accountService.withdraw(acc.getAccountId(), 999999.0));
   }

   @Test @Order(12)
   void testDepositNegativeThrows() {
       authService.login("Emma", "1234", "child");
       String userId = authService.getCurrentUser().getUserId();
       Account acc = accountService.getAccountsForUser(userId).get(0);
       assertThrows(IllegalArgumentException.class,
           () -> accountService.deposit(acc.getAccountId(), -10.0, "bad"));
   }

   @Test @Order(13)
   void testTransactionHistoryNotEmpty() {
       authService.login("Emma", "1234", "child");
       String userId = authService.getCurrentUser().getUserId();
       Account acc = accountService.getAccountsForUser(userId).get(0);
       assertFalse(accountService.getTransactions(acc.getAccountId()).isEmpty());
   }

   // ======================== TASK TESTS ========================

   @Test @Order(14)
   void testCreateTask() {
       authService.login("Alice", "5678", "parent");
       String parentId = authService.getCurrentUser().getUserId();
       authService.login("Emma", "1234", "child");
       String childId = authService.getCurrentUser().getUserId();

       Task task = taskService.createTask(parentId, childId,
               "Wash dishes", "Clean all dishes", 5.0, LocalDate.now().plusDays(1));
       assertNotNull(task);
       assertEquals(TaskStatus.PENDING, task.getStatus());
       assertEquals(5.0, task.getRewardAmount());
   }

   @Test @Order(15)
   void testMarkTaskComplete() {
       authService.login("Emma", "1234", "child");
       String childId = authService.getCurrentUser().getUserId();
       Task task = taskService.getTasksForChild(childId).stream()
               .filter(t -> t.getStatus() == TaskStatus.PENDING).findFirst().orElseThrow();
       taskService.markComplete(task.getTaskId());
       Task updated = taskService.getTasksForChild(childId).stream()
               .filter(t -> t.getTaskId().equals(task.getTaskId())).findFirst().orElseThrow();
       assertEquals(TaskStatus.COMPLETED, updated.getStatus());
   }

   @Test @Order(16)
   void testApproveTask() {
       authService.login("Emma", "1234", "child");
       String childId = authService.getCurrentUser().getUserId();
       Account acc = accountService.getAccountsForUser(childId).get(0);
       double balanceBefore = acc.getBalance();

       Task task = taskService.getTasksForChild(childId).stream()
               .filter(t -> t.getStatus() == TaskStatus.COMPLETED).findFirst().orElseThrow();
       taskService.approveTask(task.getTaskId(), acc.getAccountId());

       double balanceAfter = accountService.getAccountById(acc.getAccountId()).getBalance();
       assertEquals(balanceBefore + task.getRewardAmount(), balanceAfter, 0.001);
   }

   @Test @Order(17)
   void testCreateTaskEmptyTitleThrows() {
       assertThrows(IllegalArgumentException.class,
           () -> taskService.createTask("pid", "cid", "", "desc", 5.0, null));
   }

   // ======================== SAVINGS GOAL TESTS ========================

   @Test @Order(18)
   void testCreateSavingsGoal() {
       authService.login("Emma", "1234", "child");
       Account acc = accountService.getAccountsForUser(
               authService.getCurrentUser().getUserId()).get(0);
       SavingsGoal goal = goalService.createGoal(acc.getAccountId(), "New Bike", 100.0, null);
       assertNotNull(goal);
       assertEquals("New Bike", goal.getGoalName());
       assertEquals(0.0, goal.getSavedAmount());
       assertEquals(0.0, goal.getProgressPercent());
   }

   @Test @Order(19)
   void testAddToGoal() {
       authService.login("Emma", "1234", "child");
       Account acc = accountService.getAccountsForUser(
               authService.getCurrentUser().getUserId()).get(0);
       SavingsGoal goal = goalService.getActiveGoals(acc.getAccountId()).get(0);

       goalService.addToGoal(goal.getGoalId(), 20.0, acc.getAccountId());
       SavingsGoal updated = goalService.findGoalById(goal.getGoalId());
       assertEquals(20.0, updated.getSavedAmount(), 0.001);
       assertFalse(updated.isCompleted());
   }

   @Test @Order(20)
   void testGoalCompletionWhenTargetReached() {
       authService.login("Emma", "1234", "child");
       Account acc = accountService.getAccountsForUser(
               authService.getCurrentUser().getUserId()).get(0);
       // Create a small goal that can be completed easily
       accountService.deposit(acc.getAccountId(), 200.0, "Test setup");
       SavingsGoal goal = goalService.createGoal(acc.getAccountId(), "SmallGoal", 10.0, null);
       goalService.addToGoal(goal.getGoalId(), 10.0, acc.getAccountId());
       SavingsGoal updated = goalService.findGoalById(goal.getGoalId());
       assertTrue(updated.isCompleted());
       assertEquals(1.0, updated.getProgressPercent(), 0.001);
   }

   @Test @Order(21)
   void testGoalInvalidTargetThrows() {
       assertThrows(IllegalArgumentException.class,
           () -> goalService.createGoal("accId", "BadGoal", -50.0, null));
   }
}
