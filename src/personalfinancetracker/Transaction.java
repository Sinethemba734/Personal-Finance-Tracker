package personalfinancetracker;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String date;
    private final double amount;

    public Transaction(String date, double amount) {
        if (date == null || date.isBlank())
         throw new IllegalArgumentException("Enter a transaction date in YYYY-MM-DD format.");

        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new IllegalArgumentException("Use YYYY-MM-DD for the date, for example 2026-10-31.");
        }

        try {
            LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("That date does not exist. Enter a real date in YYYY-MM-DD format.", e);
        }
        
        if (!Double.isFinite(amount) || amount <= 0) 
        throw new IllegalArgumentException("Amount must be a valid,greater than zero and non-negative number.");
        this.date = date;
        this.amount = amount;
    }

    public String getDate() {
    return date; 
    }
    
    public double getAmount() {
    return amount; 
    }
    public boolean isExpense() {
     return false; 
    
    }
    @Override public String toString() { 
     return date + " | " + getClass().getSimpleName() + " | " + amount; 
    }
}
