package personalfinancetracker;
public class Expense extends Transaction {
    private static final long serialVersionUID = 1L;
    private final Category category;
    
    public Expense(String date, double amount, Category category) {
        super(date, amount);
        if (category == null){
            throw new IllegalArgumentException("Expense category required.");
        }
        this.category = category;
    }
    public Category getCategory() {
      return category;
    }
    
  @Override public boolean isExpense() {
        return true;
    }
    
    @Override public String toString() {
        
        return super.toString() + " | category: " + category.getName();
    }
}
