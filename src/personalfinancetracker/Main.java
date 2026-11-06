package personalfinancetracker;
import java.util.Scanner;
import java.util.Arrays;
import javax.swing.JPasswordField;
import javax.swing.JOptionPane;

public class Main {
    private static final String APP_PASSWORD = "1234";

    public static void main(String[] args) {
        if (!authenticate()) 
            return;
        
        Scanner input=new Scanner(System.in);
        System.out.println("Pls chosse whether program makes use of GUI interface or console based interface CLI");
        int chooser = 0;
        while (chooser != 1 && chooser != 2) {
            System.out.print("Enter (1)- GUI ------ (2)-CLI: ");
            String answer = input.nextLine().trim();
            if (answer.equals("1") || answer.equals("2")) {
                chooser = Integer.parseInt(answer);
            } else {
                System.out.println("Please type 1 or 2.");
            }
        }
        
        TransactionManager manager;
        try {
            manager = new TransactionManager();
        } catch (IllegalStateException e) {
            String message = e.getMessage();
            if (chooser == 1) {
                JOptionPane.showMessageDialog(null, message, "Saved data problem", JOptionPane.ERROR_MESSAGE);
            } else {
                System.out.println(message);
            }
            return;
        }
        if(chooser==1){
        MenuGUI menu = new MenuGUI(manager);
        menu.showMenu();
        }
        else{
            MenuCLI menu=new MenuCLI(manager);
            menu.showMenu();
        }
    }

    private static boolean authenticate() {
        while (true) {
            JPasswordField passwordField = new JPasswordField();
            int result = JOptionPane.showConfirmDialog(null, passwordField,"Enter password to open Personal Finance Tracker",JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result != JOptionPane.OK_OPTION)
                return false;

            char[] enteredPassword = passwordField.getPassword();
            boolean correct = APP_PASSWORD.equals(new String(enteredPassword));
            Arrays.fill(enteredPassword, '\0');

            if (correct)
                return true;
            JOptionPane.showMessageDialog(null, "Incorrect password. Please try again.","Login failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
