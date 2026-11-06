package personalfinancetracker;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class TransactionManager {
    private static final Logger LOGGER = Logger.getLogger(TransactionManager.class.getName());
    private final List<Transaction> transactions = new ArrayList<>(); //list that holds  incomes and expenses
    private final List<Transaction> savedTransactions = new ArrayList<>(); //saved entries kept separate until Load is clicked
    private final List<Category> categories = new ArrayList<>(); // list that holds categories of expenses
    private Budget budget; //user budget
    private final FileHandler fileHandler = new FileHandler("transactions.txt"); //file that saves and loads transactions
    private final FileHandler budgetFileHandler = new FileHandler("budget.txt");
    private boolean loadedFromFile;
    private boolean unsavedChanges;
    private boolean transactionLoadFailed;
    private String startupWarning;

    public TransactionManager() {
        try {
            savedTransactions.addAll(fileHandler.load());
        } catch (IllegalStateException e) {
            transactionLoadFailed = true;
            startupWarning = "Saved transactions could not be read. The app started with an empty list, and saving is disabled until the transaction file can be loaded.";
            LOGGER.warning("Could not load saved transactions during startup: " + e.getMessage());
        }

        try {
            budget = budgetFileHandler.loadBudget();
        } catch (IllegalStateException e) {
            startupWarning = (startupWarning == null ? "" : startupWarning + "\n")
                    + "The saved budget could not be read. You can enter a new budget.";
            LOGGER.warning("Could not load the saved budget during startup: " + e.getMessage());
        }
          updateBudget(); 
    }

    public String getStartupWarning() {
        return startupWarning;
    }

    public boolean hasUnsavedChanges() {
        return unsavedChanges;
    }

    public void addTransaction(Transaction t) {
        if (t == null)
        throw new IllegalArgumentException("Please enter a transaction first."); 
        transactions.add(t); 
        unsavedChanges = true;
        LOGGER.info("Added a " + t.getClass().getSimpleName() + " transaction");
        updateBudget(); 
    }//END
    
    public boolean deleteTransaction(Transaction t) {
        boolean removed = transactions.remove(t);
        if (removed) {
            unsavedChanges = true;
            LOGGER.info("Deleted a " + t.getClass().getSimpleName() + " transaction");
        } else {
            LOGGER.warning("Could not delete transaction because it was not found");
        }
        updateBudget();
        return removed;
    }//END
    
    public List<Transaction> search(String keyword) {
    String searchKey = (keyword == null )? "" : keyword.toLowerCase();
    List<Transaction> results = new ArrayList<>();

    for (Transaction transaction : transactions) {
        String transactionKey = transaction.toString().toLowerCase();

        if (transactionKey.contains(searchKey)) {
            results.add(transaction);
        }
    }

    return results;
    }//END

    public List<Transaction> searchSortedByAmount(String keyword) {
        List<Transaction> results = search(keyword);
        for (int i = 1; i < results.size(); i++) {
            Transaction current = results.get(i);
            int j = i - 1;

            while (j >= 0 && results.get(j).getAmount() < current.getAmount()) {
                results.set(j + 1, results.get(j));
                j--;
            }

            results.set(j + 1, current);
        }
        return results;
    }

    public double getTotalSpending() {
        return totalSpendingFrom(0);
    }//END

    private double totalSpendingFrom(int index) {
        if (index >= transactions.size()) {
            return 0;
        }

        Transaction transaction = transactions.get(index);
        double currentExpense = transaction.isExpense() ? transaction.getAmount() : 0;
        return currentExpense + totalSpendingFrom(index + 1);
    }

    public void addCategory(Category c) {
    if (c == null) {
        return;
    }

    for (Category x : categories) {
        if (x.getName().equalsIgnoreCase(c.getName())) {
            return;
        }
    }

    categories.add(c);
}//END
   
    public List<Category> getCategories() {
    return List.copyOf(categories); 
    }//END
    
    public List<Transaction> getTransactions() {
    return List.copyOf(transactions);
    }//END
    
    public void setBudget(Budget b) {
    budget = b;
    unsavedChanges = true;
    updateBudget(); 
    if (budget != null && !loadedFromFile) {
        budgetFileHandler.saveBudget(budget);
    }
    }//END
    
    public Budget getBudget() {
    return budget;
    }//END
    public void save() {
        if (transactionLoadFailed) {
            throw new IllegalStateException("Transactions were not saved because the existing transaction file could not be read. Fix or restore that file, then restart and load it before saving.");
        }
        if (loadedFromFile) {
            fileHandler.save(transactions);
        } else {
            List<Transaction> combined = new ArrayList<>(savedTransactions);
            combined.addAll(transactions);
            fileHandler.save(combined);
        }
        unsavedChanges = false;
    }//END
    
    public void load() {
        // Load both files successfully before replacing the transactions currently shown.
        List<Transaction> loaded = fileHandler.load();
        Budget loadedBudget = budgetFileHandler.loadBudget();
        transactions.clear();
        transactions.addAll(loaded);
        savedTransactions.clear();
        savedTransactions.addAll(loaded);
        if (loadedBudget != null) {
            budget = loadedBudget;
        }
        loadedFromFile = true;
        transactionLoadFailed = false;
        unsavedChanges = false;
        updateBudget();
    }//END
    
    private void updateBudget(){
    if (budget != null) {
        budget.setSpending(getTotalSpending());
        if (loadedFromFile) {
            budgetFileHandler.saveBudget(budget);
        }
    }
    }//END
    
}
