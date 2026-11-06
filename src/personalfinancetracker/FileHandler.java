package personalfinancetracker;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileHandler {
    
    private static final Logger LOGGER = Logger.getLogger(FileHandler.class.getName());
    private final String filePath;
    
    public FileHandler(String filePath) {
        this.filePath = filePath;
    }
    
    public void save(List<?> list) {
           try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath));) {
            
            out.writeObject(new ArrayList<>(list));
            LOGGER.info("Saved " + list.size() + " transactions to " + filePath);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not save transaction data to " + filePath, e);
            throw new IllegalStateException("Could not save your transactions. Check that the project folder is writable, then try again.", e);
        }
    }

    public void saveBudget(Budget budget) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(budget);
            LOGGER.info("Saved budget to " + filePath);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not save budget to " + filePath, e);
            throw new IllegalStateException("Could not save your budget. Check that the project folder is writable, then try again.", e);
        }
    }

    public Budget loadBudget() {
        File file = new File(filePath);
        if (!file.exists()) {
            LOGGER.info("No saved budget found at " + filePath);
            return null;
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            Budget loaded = (Budget) in.readObject();
            LOGGER.info("Loaded budget from " + filePath);
            return loaded;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load budget from " + filePath, e);
            throw new IllegalStateException("Could not read the saved budget file. It may be damaged or unavailable; the file was not changed.", e);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "The saved budget uses an unknown class in " + filePath, e);
            throw new IllegalStateException("This budget file was created by an incompatible version of the program.", e);
        } catch (ClassCastException e) {
            LOGGER.log(Level.SEVERE, "The file does not contain budget data: " + filePath, e);
            throw new IllegalStateException("This file does not contain saved budget data.", e);
        }
    }
    
    @SuppressWarnings("unchecked")
    public <T> List<T> load() {
        File file = new File(filePath);
        if (!file.exists()) {
            LOGGER.info("No saved transaction file found at " + filePath + "; starting with an empty list");
            return new ArrayList<>();
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            List<T> loaded = (List<T>) in.readObject();
            LOGGER.info("Loaded " + loaded.size() + " transactions from " + filePath);
            return loaded;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load transaction data from " + filePath, e);
            throw new IllegalStateException("Could not read the saved transactions file. It may be damaged or unavailable; the file was not changed.", e);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "The saved transactions use an unknown class in " + filePath, e);
            throw new IllegalStateException("This transactions file was created by an incompatible version of the program.", e);
        } catch (ClassCastException e) {
            LOGGER.log(Level.SEVERE, "The file does not contain a transaction list: " + filePath, e);
            throw new IllegalStateException("This file does not contain saved transactions.", e);
        }
    }
}
