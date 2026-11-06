package personalfinancetracker;
import java.io.Serializable;

public class Category implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private final String name;
    
    public Category(String name){
        if (name == null || name.isBlank()){ 
            throw new IllegalArgumentException("Category name required.");
        }
        
        String cleanedName = name.trim();
        for (char character : cleanedName.toCharArray()) {
            if (Character.isDigit(character)) {
                throw new IllegalArgumentException("Category names can't contain digits.");
            }
        }
        this.name = cleanedName;
    }
    
    public String getName() { 
      return name; 
    }
    @Override public String toString() {
        return name;
    
}
}
