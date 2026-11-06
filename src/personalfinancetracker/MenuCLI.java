package personalfinancetracker;

import java.util.Scanner;

public class MenuCLI {
    private final TransactionManager manager;
    private final Scanner input = new Scanner(System.in);

    public MenuCLI(TransactionManager manager) {
        this.manager = manager;
    }

    public void showMenu() {
        if (manager.getStartupWarning() != null) {
            System.out.println("\n" + manager.getStartupWarning());
        }

        boolean running = true;

        while (running) {
            System.out.println(
                    "\nPersonal Finance Tracker"
                    + "\n1. Add income"
                    + "\n2. Add expense"
                    + "\n3. List transactions"
                    + "\n4. Search"
                    + "\n5. Set budget"
                    + "\n6. Show spending"
                    + "\n7. Save"
                    + "\n8. Load"
                    + "\n9. Delete a transaction"
                    + "\n10. Sort by amount (high to low)"
                    + "\n11. Spending chart"
                    + "\n0. Exit"
            );

            switch (read("Choose: ")) {
                case "1":
                    addIncome();
                    break;
                case "2":
                    addExpense();
                    break;
                case "3": {
                    java.util.List<Transaction> transactions = manager.getTransactions();
                    if (transactions.isEmpty()) {
                        System.out.println("The transaction list is empty. If you have saved transactions, choose 8 (Load); otherwise, add a transaction first.");
                    } else {
                        transactions.forEach(System.out::println);
                    }
                    break;
                }
                case "4": {
                    String keyword = read("Keyword: ");
                    java.util.List<Transaction> results = manager.search(keyword);

                    if (results.isEmpty()) {
                        System.out.println("No transactions found.");
                    } else {
                        results.forEach(System.out::println);
                    }
                    break;
                }
                case "5":
                    setBudget();
                    break;
                case "6":
                    showSpending();
                    break;
                case "7":
                    runSafely(manager::save, "Saved.");
                    break;
                case "8":
                    loadData();
                    break;
                case "9":
                    deleteTransaction();
                    break;
                case "10": {
                    java.util.List<Transaction> sorted = manager.searchSortedByAmount("");
                    if (sorted.isEmpty()) {
                        System.out.println("The transaction list is empty. Load saved transactions or add a transaction first.");
                    } else {
                        sorted.forEach(System.out::println);
                    }
                    break;
                }
                case "11":
                    showChart();
                    break;
                case "0":
                    running = false;
                    break;
                default:
                    System.out.println("Unknown option.");
            }
        }
    }

    public String readInput() {
        return read("Input: ");
    }

    private void addIncome() {
        try {
            Income income = new Income(
              read("Date (YYYY-MM-DD): "),
              number("Amount: "),
              read("Source: ")
            );

            manager.addTransaction(income);
            System.out.println("Income added.");

        } catch (RuntimeException e) {
            System.out.println("Could not add income: " + e.getMessage());
        }
    }

    private void addExpense() {
        try {
            String name = read("Category: ");
            Category category = null;

            for (Category existingCategory : manager.getCategories()) {
                if (existingCategory.getName().equalsIgnoreCase(name)) {
                    category = existingCategory;
                    break;
                }
            }

            if (category == null) {
                category = new Category(name);
                manager.addCategory(category);
            }

            Expense expense = new Expense(
                    read("Date (YYYY-MM-DD): "),
                    number("Amount: "),
                    category
            );

            manager.addTransaction(expense);
            System.out.println("Expense added.");

        } catch (RuntimeException e) {
            System.out.println("Could not add expense: " + e.getMessage());
        }
    }

    private void setBudget() {
        try {
            manager.setBudget(new Budget(number("Budget limit: ")));
            System.out.println(manager.getBudget());

        } catch (RuntimeException e) {
            System.out.println("Could not set budget: " + e.getMessage());
        }
    }

    private void showSpending() {
        System.out.printf("Total expenses: %.2f%n", manager.getTotalSpending());

        if (manager.getBudget() != null) {
            System.out.println(manager.getBudget());
        }
    }

    private void deleteTransaction() {
        java.util.List<Transaction> transactions = manager.getTransactions();
        if (transactions.isEmpty()) {
            System.out.println("There are no transactions to delete.");
            return;
        }

        for (int i = 0; i < transactions.size(); i++) {
            System.out.println((i + 1) + ". " + transactions.get(i));
        }

        String answer = read("Enter the number of the transaction to delete: ");
        try {
            int selected = Integer.parseInt(answer);
            if (selected < 1 || selected > transactions.size()) {
                System.out.println("Enter a number from 1 to " + transactions.size() + ".");
                return;
            }
            manager.deleteTransaction(transactions.get(selected - 1));
            System.out.println("Transaction deleted.");
        } catch (NumberFormatException e) {
            System.out.println("Enter the transaction number using digits.");
        }
    }

    private void showChart() {
        java.util.Map<String, Double> totals = new java.util.LinkedHashMap<>();
        for (Transaction transaction : manager.getTransactions()) {
            if (transaction instanceof Expense) {
                String category = ((Expense) transaction).getCategory().getName();
                double total = totals.containsKey(category) ? totals.get(category) : 0.0;
                totals.put(category, total + transaction.getAmount());
            }
        }

        if (totals.isEmpty()) {
            System.out.println("There are no expenses to display in the chart.");
            return;
        }

        double largest = 0;
        for (double total : totals.values()) {
            largest = Math.max(largest, total);
        }

        System.out.println("\nSpending by category:");
        for (java.util.Map.Entry<String, Double> entry : totals.entrySet()) {
            int barLength = Math.max(1, (int) Math.round(30 * entry.getValue() / largest));
            StringBuilder bar = new StringBuilder();
            for (int i = 0; i < barLength; i++) {
                bar.append('#');
            }
            System.out.printf("%-20s %s R%.2f%n", entry.getKey(), bar, entry.getValue());
        }
    }

    private void loadData() {
        if (manager.hasUnsavedChanges()) {
            String answer = read("You have unsaved changes. Loading will replace the transactions shown. Continue? (y/n): ");
            if (!answer.equalsIgnoreCase("y")) {
                System.out.println("Load cancelled.");
                return;
            }
        }
        runSafely(manager::load, "Transactions loaded.");
    }

    private double number(String prompt) {
    String text = read(prompt);
    for (char character : text.toCharArray()) {
        if (!Character.isDigit(character) && character != '.') {
            throw new IllegalArgumentException("Enter a number:");
        }
    }
    try {
        double value = Double.parseDouble(text);
        if (!Double.isFinite(value) || value < 0) {
            throw new IllegalArgumentException("Enter a non-negative number:");
        }
        return value;
    } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Enter a number:");
    }
}

    private String read(String prompt) {
        System.out.print(prompt);
        return input.nextLine().trim();
    }

    private void runSafely(Runnable action, String success) {
        try {
            action.run();
            System.out.println(success);

        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }
}
