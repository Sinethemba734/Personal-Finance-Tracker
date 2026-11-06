package personalfinancetracker;

public class Income extends Transaction {
    
    private final String source;
    public Income(String date, double amount, String source) {
        super(date, amount);
        
        if (source == null || source.isBlank()){
            throw new IllegalArgumentException("Income source is required.");
        }
        this.source = source;
    }
    
    public String getSource() {
      return source;
    }
    @Override public String toString() {
       return super.toString() + " | source: " + source;
    }
}
