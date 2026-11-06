package personalfinancetracker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

public class MenuGUI {
    
    private static final Logger LOGGER = Logger.getLogger(MenuGUI.class.getName());
    private final TransactionManager manager;
    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel summaryLabel;
    private JTextField searchField;
    private boolean expenseSortEnabled;
    private JButton expenseSortButton;

    public MenuGUI(TransactionManager manager) {
        this.manager = manager;
    }

    public void showMenu() {
        frame = new JFrame("Personal Finance Tracker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1000, 550));
        frame.setLocationByPlatform(true);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        root.setBackground(new Color(246, 248, 251));
        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createTransactionTable(), BorderLayout.CENTER);
        root.add(createControls(), BorderLayout.SOUTH);
        frame.setContentPane(root);
        refreshTransactions();
        frame.setVisible(true);
        if (manager.getStartupWarning() != null) {
            JOptionPane.showMessageDialog(frame, manager.getStartupWarning(),
                    "Saved data warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel title = new JLabel("Personal Finance Tracker");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        summaryLabel = new JLabel(" ", SwingConstants.RIGHT);
        summaryLabel.setFont(summaryLabel.getFont().deriveFont(Font.PLAIN, 14f));
        panel.add(title, BorderLayout.WEST);
        panel.add(summaryLabel, BorderLayout.EAST);
        return panel;
    }

    private JScrollPane createTransactionTable() {
        String[] columns = {"Date", "Type", "Amount (R)", "Details", "Transaction"};
       
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        
        table.removeColumn(table.getColumnModel().getColumn(4));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(26);
        table.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Transactions"));
        return scrollPane;
    }

    private JPanel createControls() {
        
        JPanel outer = new JPanel(new BorderLayout(8, 8));
        outer.setOpaque(false);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        addButton(actions, "Add income", e -> addIncome());
        addButton(actions, "Add expense", e -> addExpense());
        addButton(actions, "Delete selected", e -> deleteSelected());
        expenseSortButton = new JButton("Sort all by amount (high to low)");
        expenseSortButton.addActionListener(e -> {
            expenseSortEnabled = !expenseSortEnabled;
            expenseSortButton.setText(expenseSortEnabled ? "Show original order" : "Sort all by amount (high to low)");
            refreshTransactions(searchField.getText());
        });
        actions.add(expenseSortButton);
        addButton(actions, "Budget", e -> setBudget());
        addButton(actions, "Spending chart", e -> viewChart());
        addButton(actions, "Save", e -> runAction(() -> manager.save(), "Transactions saved."));
        addButton(actions, "Load", e -> loadData());
        outer.add(actions, BorderLayout.NORTH);

        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        search.setOpaque(false);
        searchField = new JTextField(24);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> refreshTransactions(searchField.getText()));
        JButton clearButton = new JButton("Show all");
        clearButton.addActionListener(e -> { searchField.setText(""); refreshTransactions(); });
        search.add(new JLabel("Find:"));
        search.add(searchField);
        search.add(searchButton);
        search.add(clearButton);
        outer.add(search, BorderLayout.SOUTH);
        return outer;
    }

    private void addButton(JPanel panel, String label, java.awt.event.ActionListener action) {
        JButton button = new JButton(label);
        button.addActionListener(action);
        panel.add(button);
    }

    private void showOriginalTransactionOrder() {
        expenseSortEnabled = false;
        expenseSortButton.setText("Sort all by amount (high to low)");
    }

    private void addIncome() {
        JTextField date = new JTextField(LocalDate.now().toString());
        JTextField amount = new JTextField();
        JTextField source = new JTextField();
        JPanel form = formPanel(new String[]{"Date (YYYY-MM-DD)", "Amount (R)", "Source"}, new JTextField[]{date, amount, source});
        if (JOptionPane.showConfirmDialog(frame, form, "Add income", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
            return;
        try {
            manager.addTransaction(new Income(date.getText().trim(), parseAmount(amount.getText()), source.getText().trim()));
            showOriginalTransactionOrder();
            refreshTransactions();
        } catch (RuntimeException ex) {
            showError(ex); 
        }
    }

    private void addExpense() {
        JTextField date = new JTextField(LocalDate.now().toString());
        JTextField amount = new JTextField();
        JTextField categoryName = new JTextField();
        JPanel form = formPanel(new String[]{"Date (YYYY-MM-DD)", "Amount (R)", "Category"}, new JTextField[]{date, amount, categoryName});
        if (JOptionPane.showConfirmDialog(frame, form, "Add expense", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
           return;
        try {
            String name = categoryName.getText().trim();
            
            Category category = null;

            for (Category existing : manager.getCategories()) {
                if (existing.getName().equalsIgnoreCase(name)) {
                    category = existing;
                    break;
                }
                  }

         if (category == null) {
           category = new Category(name);
           manager.addCategory(category);
}
            manager.addTransaction(new Expense(date.getText().trim(), parseAmount(amount.getText()), category));
            showOriginalTransactionOrder();
            refreshTransactions();
        } catch (RuntimeException ex) { showError(ex); }
    }

    private JPanel formPanel(String[] labels, JTextField[] fields) {
        JPanel panel = new JPanel(new GridLayout(labels.length, 2, 8, 8));
        for (int i = 0; i < labels.length; i++) {
            panel.add(new JLabel(labels[i] + ":"));
            panel.add(fields[i]);
        }
        return panel;
    }

    private double parseAmount(String value) {
        for (char character : value.trim().toCharArray()) {
        if (!Character.isDigit(character) && character != '.') {
        throw new IllegalArgumentException("Enter an amount using numbers, for example 200 or 200.50.");
    }
    }
        double amount;
        try {
            amount = Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Enter an amount using numbers, for example 200 or 200.50.");
        }
        if (!Double.isFinite(amount) || amount < 0) {
            throw new IllegalArgumentException("Amount must be a non-negative number, for example 200 or 200.50.");
        }
        return amount;
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { 
           JOptionPane.showMessageDialog(frame, "Select a transaction first."); 
           return; }
        int modelRow = table.convertRowIndexToModel(row);
        
        Transaction selected = (Transaction) tableModel.getValueAt(modelRow, 4);
        manager.deleteTransaction(selected);
        refreshTransactions();
    }

    private void setBudget() {
        String value = JOptionPane.showInputDialog(frame, "Enter your spending budget limit (R):",
                manager.getBudget() == null ? "" : Double.toString(manager.getBudget().getLimit()));
        if (value == null) return;
        try {
            manager.setBudget(new Budget(parseAmount(value)));
            refreshTransactions();
        } catch (RuntimeException ex) { showError(ex); }
    }

    public void viewChart() {
        JDialog dialog = new JDialog(frame, "Spending by category", true);
        dialog.setContentPane(new SpendingChartPanel(manager));
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private void loadData() {
        if (manager.hasUnsavedChanges()) {
            int answer = JOptionPane.showConfirmDialog(frame,
                    "You have unsaved changes. Loading will replace the transactions shown. Continue?",
                    "Unsaved changes", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (answer != JOptionPane.YES_OPTION) {
                return;
            }
        }
        runAction(() -> manager.load(), "Transactions loaded.");
    }

    private void refreshTransactions() {
     refreshTransactions("");
    }

    private void refreshTransactions(String keyword) {
        if (tableModel == null)
            return;
        tableModel.setRowCount(0);
        java.util.List<Transaction> visibleTransactions = expenseSortEnabled
                ? manager.searchSortedByAmount(keyword) : manager.search(keyword);
        for (Transaction transaction : visibleTransactions) {
            String type =transaction instanceof Expense ? "Expense" : "Income";
            String details = transaction instanceof Expense
                    ? ((Expense) transaction).getCategory().getName()
                    : ((Income) transaction).getSource();
            tableModel.addRow(new Object[]{transaction.getDate(), type,
                    String.format("R%.2f", transaction.getAmount()), details, transaction});
        }
        if (keyword != null && !keyword.isBlank() && visibleTransactions.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No transactions found.");
        }
        if (table.getColumnCount() > 4) 
            table.removeColumn(table.getColumnModel().getColumn(4));
        updateSummary();
    }

    private void updateSummary() {
        double income = 0;
        for (Transaction transaction : manager.getTransactions()) {
            if (!transaction.isExpense()) {
                income += transaction.getAmount();
            }
        }
        double expenses = manager.getTotalSpending();
        StringBuilder text = new StringBuilder(String.format(
                "Income: R%.2f    Expenses: R%.2f    Balance: R%.2f",
                income, expenses, income - expenses));

        if (manager.getBudget() != null) {
            text.append("    ").append(manager.getBudget());
        }

        summaryLabel.setText(text.toString());
    }

    private void runAction(Runnable action, String successMessage) {
        try {
         action.run();
         refreshTransactions();
         JOptionPane.showMessageDialog(frame, successMessage); 
        }
        catch (RuntimeException ex) {
            showError(ex);
        }
    }

    private void showError(RuntimeException ex) {
        LOGGER.warning("A user action failed: " + ex.getMessage());
        JOptionPane.showMessageDialog(frame, ex.getMessage(), "Please check this message", JOptionPane.ERROR_MESSAGE);
    }

    private static class SpendingChartPanel extends JPanel {

    private final Map<String, Double> spending= new LinkedHashMap<>();
    private final double total;

    SpendingChartPanel(TransactionManager manager){
        
        setBackground(Color.WHITE);

        for (Transaction transaction : manager.getTransactions()) {
            if(transaction instanceof Expense) {
                Expense expense = (Expense) transaction;
                String name = expense.getCategory().getName();
                double oldAmount = spending.getOrDefault(name, 0.0);
                spending.put(name, oldAmount + expense.getAmount());
            }
        }
        total= manager.getTotalSpending();
        setPreferredSize(new Dimension(700, Math.max(360, 100 + 26 * spending.size())));
    }

    private Color colorFor(int index) {
        return Color.getHSBColor(index / (float) Math.max(1, spending.size()), 0.65f, 0.9f);
    }

    private String fitText(String text, FontMetrics metrics, int availableWidth) {
        if (metrics.stringWidth(text) <= availableWidth) {
            return text;
        }
        while (text.length() > 1 && metrics.stringWidth(text + "...") > availableWidth) {
            text = text.substring(0, text.length() - 1);
        }
        return text + "...";
    }

        @Override protected void paintComponent(Graphics graphics) {
            
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(Color.BLACK);
        g.setFont(getFont().deriveFont(Font.BOLD, 16f));
        g.drawString("Total spending: R" + String.format("%.2f", total), 20, 30);

        if (spending.isEmpty() ||total <= 0) {
            g.drawString("Add expenses to see the chart.", 20, 60);
            return;
        }

        int size =250; 
        int x =30;
        int y =60;

        
        double startAngle =90;
        int colorIndex =0;
        for (Map.Entry<String, Double> entry : spending.entrySet()) {
            double angle =-360.0 * entry.getValue() / total;
            g.setColor(colorFor(colorIndex));
            g.fill(new Arc2D.Double(x, y, size, size, startAngle, angle, Arc2D.PIE));
            startAngle = startAngle + angle;
            colorIndex++;
        }

        int legendX = x + size + 30;
        int legendY = 80;
        g.setFont(getFont().deriveFont(13f));
        FontMetrics metrics = g.getFontMetrics();
        colorIndex = 0;
        
        for (Map.Entry<String, Double> entry : spending.entrySet()) {
            g.setColor(colorFor(colorIndex));
            g.fillRect(legendX, legendY - 12, 14, 14);
            g.setColor(Color.BLACK);
            double percent = 100.0 * entry.getValue() / total;
            String amountAndPercent = ": R" + String.format("%.2f", entry.getValue())
                    + " (" + String.format("%.1f", percent) + "%)";
            int availableWidth = getWidth() - (legendX + 22) - metrics.stringWidth(amountAndPercent) - 12;
            g.drawString(fitText(entry.getKey(), metrics, availableWidth) + amountAndPercent,
                    legendX + 22, legendY);
            legendY = legendY + 26;
            colorIndex++;
        }
    }
}
}
