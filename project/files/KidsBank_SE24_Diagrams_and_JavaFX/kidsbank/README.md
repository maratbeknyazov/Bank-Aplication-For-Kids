# 🏦 Virtual Bank Application for Kids
**Group SE-24** | Agile Software Development | Politecnico di Torino — Tashkent

**Team:** Kokhkhorov Usmonjon, Muratbayev Albert, Mirzaev Jahongir, Yuldosheva Muslima, Bakhtiyorova Visola

---

## 📋 Project Overview
A standalone JavaFX desktop application that teaches children about money management
through virtual banking. Parents manage accounts and tasks; children earn, save, and spend.

---

## 🛠 Prerequisites
| Tool | Version |
|------|---------|
| Java JDK | 17 or later |
| Maven | 3.8+ |
| JavaFX | 21 (auto-downloaded via Maven) |

---

## 🚀 How to Run

### Option 1 — Maven (recommended)
```bash
# Clone or unzip the project
cd kidsbank

# Compile and run
mvn javafx:run
```

### Option 2 — Run tests
```bash
mvn test
```

### Option 3 — Build fat JAR
```bash
mvn package
java -jar target/kidsbank-1.0.0-shaded.jar
```

---

## 📁 Project Structure
```
kidsbank/
├── pom.xml                          # Maven build file
├── data/                            # Auto-created: JSON data files
│   ├── users.json
│   ├── accounts.json
│   ├── transactions.json
│   ├── tasks.json
│   └── goals.json
├── diagrams/                        # PlantUML diagrams
│   ├── class_diagram.puml / .png
│   ├── usecase_diagram.puml / .png
│   ├── sequence_diagram.puml / .png
│   └── activity_diagram.puml / .png
└── src/main/java/com/kidsbank/
    ├── MainApp.java                 # JavaFX entry point
    ├── LoginScreen.java
    ├── ChildDashboardScreen.java
    ├── ParentDashboardScreen.java
    ├── TransactionScreen.java
    ├── TaskScreen.java
    ├── SavingsGoalScreen.java
    ├── KidsBankTest.java            # JUnit 5 tests
    ├── model/                       # Data model classes
    │   ├── User.java
    │   ├── Account.java
    │   ├── Transaction.java
    │   ├── Task.java
    │   ├── SavingsGoal.java
    │   ├── AccountType.java
    │   ├── TransactionType.java
    │   └── TaskStatus.java
    ├── service/                     # Business logic
    │   ├── AuthService.java
    │   ├── AccountService.java
    │   ├── TaskService.java
    │   └── SavingsGoalService.java
    └── storage/                     # JSON file persistence
        └── JsonStorage.java
```

---

## 🎮 How to Use the App

### First-Time Setup
1. Run the app → Click **"I'm a Parent"** → Register yourself via the Parent Dashboard
2. From Parent Dashboard → **"Create Child Account"** → fill in child's name, age, account type, PIN
3. Deposit some money so the child has a starting balance

### As a Child
1. Select **"I'm a Child"** → enter name + PIN
2. View balance, withdraw money, complete tasks, set savings goals

### As a Parent
1. Select **"I'm a Parent"** → enter name + PIN
2. Deposit money, set tasks with rewards, approve/reject completed tasks

---

## 📊 Data Storage
- All data is stored **locally as JSON files** in the `/data` folder
- No internet connection or database required
- Files are human-readable and can be inspected with any text editor

---

## 🏗 Architecture
The project follows a **3-layer architecture**:
- **Model** — Plain Java objects (User, Account, Transaction, Task, SavingsGoal)
- **Service** — Business logic (AuthService, AccountService, TaskService, SavingsGoalService)
- **Storage** — JSON file persistence (JsonStorage)
- **UI** — JavaFX screens (no FXML — programmatic UI)

---

## ✅ Implemented Features (Iteration 1)
- [x] User registration and PIN-based login (child + parent roles)
- [x] Account creation (Current / Savings)
- [x] Balance tracking
- [x] Deposit and Withdrawal with validation
- [x] Transaction history with filtering
- [x] Task creation, completion, approval/rejection
- [x] Savings goals with progress tracking
- [x] Error handling and input validation throughout
- [x] JSON file persistence (no database)

---

## 🔐 Security
- PINs are stored as **SHA-256 hashes** — never in plain text
- Account lockout after **3 failed login attempts**
- Parent can reset child PIN

---

*Group SE-24 — Agile Software Development — Politecnico di Torino Tashkent Campus*
