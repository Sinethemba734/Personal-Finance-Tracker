package personalfinancetracker;
import java.io.Serializable;

public class Budget implements Serializable {
    private static final long serialVersionUID = 1L;
    private final double limit;
    private double spending;
    
    public Budget(double limit) {
        if (!Double.isFinite(limit) || limit < 0){
    throw new IllegalArgumentException("Enter a valid non-negative budget limit that is also greater than zero, such as 1000 or 1000.50.");}
       this.limit = limit;
    }
    public double getLimit() {
    return limit;
    }
    
    public void setSpending(double spending) {
    this.spending = spending; 
    }
    
    public boolean isExceeded() {
    return spending > limit;
    }
  @Override public String toString() { 
    return "Budget: " + spending + " / " + limit + (isExceeded() ? " (exceeded)" : "");
    
    }
}
