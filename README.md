# Personal Finance Tracker

A Java desktop application for recording income and expenses, setting a spending budget and saving your data to files. It can be used through a **graphical interface (Swing)** or a **console menu (CLI)**, and it is protected by a start-up password.

## Features

- Password prompt at start-up
- Choice of **GUI** or **console** interface, with the same features in both
- Record **income** (date, amount, source)
- Record **expenses** (date, amount, category). Categories are created automatically the first time you use them, and names are matched case-insensitively.
- **Search** transactions by keyword (matches the date, type, amount, source or category)
- **Delete** transactions
- **Sort** transactions by amount, highest first
- Set a **spending budget** and see whether it has been exceeded. The budget is saved automatically.
- Show total income, expenses and balance (GUI) or total expenses (CLI)
- **Spending chart** by category: a pie chart in the GUI and a text bar chart in the CLI
- **Save** and **load** transactions to and from a file, with a warning before unsaved changes are replaced
- Clear messages if saved data can't be read, and protection against overwriting a file that could not be loaded
- Activity and errors are written to the console with `java.util.logging`

## Requirements

- Java Development Kit (JDK) **23 or newer**
- A desktop environment that can show windows. The password dialog uses Swing even if you choose the console menu, so the program will not work on a headless server.
- Any IDE (e.g. NetBeans) or the command line

## Project structure

All classes are in the package `personalfinancetracker`.

| File | Purpose |
|---|---|
| `Main.java` | Entry point. Asks for the password, lets you choose GUI or CLI, creates the manager and starts the chosen menu. |
| `MenuGUI.java` | Swing interface: transaction table, buttons, search box, summary line and the spending chart. |
| `MenuCLI.java` | Console menu and text input. |
| `TransactionManager.java` | Core logic: stores transactions and categories, search, sorting, total spending, budget updates, unsaved-change tracking, save and load. |
| `Transaction.java` | Base class for all transactions (date and amount, with validation). |
| `Income.java` | A transaction with a source. |
| `Expense.java` | A transaction with a category; counts towards spending. |
| `Category.java` | Name of an expense category. |
| `Budget.java` | Spending limit, current spending and an "exceeded" check. |
| `FileHandler.java` | Saves and loads a list of transactions or a budget using Java serialization, with logging and specific error messages. |

## How to run

### In NetBeans

1. Open the project and make sure it uses the same JDK for compiling and running.
2. Choose **Clean and Build**, then **Run**. `Main` is the main class.

### From the command line

From the folder that contains the `.java` files:

```bash
cd C:\Users\YourName\Projects\PersonalFinanceTracker\src
javac -d out *.java
java -cp out personalfinancetracker.Main
```

## Starting the program

1. A dialog asks for the password. The default password is **`1234`** and it is written directly in `Main.java`, so it only stops casual use. Wrong passwords show an error and ask again, and cancelling closes the program.
2. The console then asks you to choose an interface: type **1** for the GUI or **2** for the CLI and press Enter. Any other input shows "Please type 1 or 2." and asks again.
3. Saved data is read. If the saved transactions or the saved budget cannot be read, a warning is shown (a dialog in the GUI, text in the CLI) and the program continues. See [Saving and loading](#saving-and-loading).

## Using the GUI

The window shows a table of transactions (Date, Type, Amount (R), Details), a summary line at the top right (Income, Expenses, Balance and the budget status) and a row of buttons. Amounts are shown in rand (R).

| Control | What it does |
|---|---|
| **Add income** | Opens a form for date, amount (R) and source. The date starts as today's date. |
| **Add expense** | Opens a form for date, amount (R) and category. |
| **Delete selected** | Removes the selected row. Select a row first. |
| **Sort all by amount (high to low)** | Sorts the visible transactions by amount. The button changes to "Show original order" to switch back. Adding a transaction returns the list to the original order. |
| **Budget** | Asks for a spending limit (R). The summary line then shows `Budget: spending / limit`, with "(exceeded)" if you are over. |
| **Spending chart** | Shows a pie chart with the amount and percentage for each category. The colours are generated for however many categories you have, long names are shortened with "...", and the window grows with the number of categories. |
| **Save** | Writes the transactions to a file. A message confirms success. |
| **Load** | Reads the saved transactions and budget. If you have unsaved changes, it asks you to confirm first. |
| **Find / Search / Show all** | Filters the table by keyword, or clears the filter. A "No transactions found." message appears if nothing matches. |

## Using the CLI

```
Personal Finance Tracker
1. Add income
2. Add expense
3. List transactions
4. Search
5. Set budget
6. Show spending
7. Save
8. Load
9. Delete a transaction
10. Sort by amount (high to low)
11. Spending chart
0. Exit
```

Type the number of an option and press Enter.

| Option | What it does |
|---|---|
| 1. Add income | Asks for date, amount and source. |
| 2. Add expense | Asks for category, date and amount. |
| 3. List transactions | Prints every transaction, or explains that the list is empty (and suggests Load). |
| 4. Search | Prints transactions that contain the keyword, or "No transactions found." |
| 5. Set budget | Asks for a limit and shows `Budget: spending / limit`. |
| 6. Show spending | Prints total expenses and the budget status, if a budget is set. |
| 7. Save | Writes the transactions to a file. |
| 8. Load | Reads the saved transactions and budget. If you have unsaved changes, it asks `y/n` first. |
| 9. Delete a transaction | Lists the transactions with numbers and deletes the one you choose. |
| 10. Sort by amount | Prints all transactions from the highest amount to the lowest. |
| 11. Spending chart | Prints a bar chart of spending per category using `#` characters and amounts in rand (R). |
| 0. Exit | Quits the program. |

### Example session

```
Choose: 2
Category: Groceries
Date (YYYY-MM-DD): 2026-10-12
Amount: 250.50
Expense added.

Choose: 5
Budget limit: 1000
Budget: 250.5 / 1000.0

Choose: 6
Total expenses: 250.50
Budget: 250.5 / 1000.0
```

## Input rules

- **Date:** required, in the format `YYYY-MM-DD`, and it must be a real calendar date (for example `2026-02-30` is rejected).
- **Amount:** a number using digits and an optional decimal point only, such as `200` or `200.50`. Minus signs, commas, spaces and letters are rejected. A transaction amount must be **greater than zero**.
- **Income source:** required.
- **Category name:** required, and it **cannot contain digits**.
- **Budget limit:** the same number format as an amount, and it must be zero or more.
- Invalid input shows an error message (a dialog in the GUI, or text such as `Could not add expense: ...` in the CLI) and nothing is added.

## Saving and loading

- Two files are written in the folder the program is run from: `transactions.txt` for transactions and `budget.txt` for the budget. They are binary Java serialization files, so don't edit them by hand.
- **Transactions at start-up:** any existing `transactions.txt` is read into memory but **not shown**. The list stays empty until you press **Load** (GUI) or choose **8** (CLI).
- **Budget at start-up:** a saved budget **is** loaded and shown straight away.
- **Save before Load:** the new transactions you entered are added to the previously saved ones, so earlier data is kept.
- **Save after Load:** the file is replaced with exactly what is on screen, so deleted transactions are removed from the file.
- **Load** reads both files first and only then replaces the list, so a failed load leaves your current data unchanged. A saved budget replaces the current one.
- **The budget is saved automatically** as soon as you set it. You don't need to press Save for it.
- **Unsaved changes:** adding or deleting a transaction, or setting a budget, counts as an unsaved change. Load asks for confirmation while there are unsaved changes, and Save clears them.
- **If the saved transactions can't be read at start-up:** a warning explains that the program started with an empty list. **Saving is disabled** so the existing file is not overwritten, and it is enabled again after a successful Load.
- The category list is not saved. It is rebuilt as you add expenses.
- Anything not saved is lost when you exit.

## Known limitations

- The amounts use `double`, which can carry small rounding errors.
- Totals, balance and budget status are calculated from the transactions currently in the list, so saved transactions only count after you Load them.
