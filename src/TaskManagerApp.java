import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.sql.*;

public class TaskManagerApp {
    private static final String DB_URL = "jdbc:sqlite:tasks.db";
    private static DefaultTableModel model;
    private static JTable table;
    private static TableRowSorter<DefaultTableModel> sorter;
    private static JLabel statsLabel;
    private static JProgressBar progressBar; 

    
    private static final Color DARK_BG = new Color(33, 37, 41);
    private static final Color DARKER_BG = new Color(21, 25, 28);
    private static final Color ACCENT_BLUE = new Color(13, 110, 253);
    private static final Color SUCCESS_GREEN = new Color(46, 204, 113);
    private static final Color WARNING_YELLOW = new Color(241, 196, 15);
    private static final Color DANGER_RED = new Color(220, 53, 69);
    private static final Color TEXT_WHITE = new Color(248, 249, 250);

    public static void main(String[] args) {
        setupDatabase();
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Task Master Elite v3.0");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 700);
        frame.getContentPane().setBackground(DARK_BG);
        frame.setLayout(new BorderLayout(10, 10));

       
        JPanel topPanel = new JPanel(new BorderLayout(20, 0));
        topPanel.setBackground(DARKER_BG);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JLabel logo = new JLabel("⚡ TASK MASTER ELITE");
        logo.setFont(new Font("SansSerif", Font.BOLD, 22));
        logo.setForeground(TEXT_WHITE);
        
        JTextField searchField = new JTextField(15);
        searchField.setBackground(new Color(45, 50, 55));
        searchField.setForeground(TEXT_WHITE);
        searchField.setCaretColor(TEXT_WHITE);
        searchField.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(ACCENT_BLUE), "Live Filter", 0, 0, null, TEXT_WHITE));

        topPanel.add(logo, BorderLayout.WEST);
        topPanel.add(searchField, BorderLayout.EAST);
        frame.add(topPanel, BorderLayout.NORTH);

       
        String[] columns = {"ID", "Task Description", "Status"};
        model = new DefaultTableModel(columns, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        table.setRowHeight(45);
        table.setBackground(DARK_BG);
        table.setForeground(TEXT_WHITE);
        table.setGridColor(new Color(60, 64, 67));
        table.setSelectionBackground(new Color(52, 73, 94));
        table.setFont(new Font("SansSerif", Font.PLAIN, 15));
        
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                setHorizontalAlignment(JLabel.CENTER);
                if ("Completed".equals(v)) comp.setForeground(SUCCESS_GREEN);
                else comp.setForeground(WARNING_YELLOW);
                return comp;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(DARK_BG);
        scrollPane.setBorder(BorderFactory.createLineBorder(DARKER_BG, 15));
        frame.add(scrollPane, BorderLayout.CENTER);

        
        JPanel bottomContainer = new JPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.Y_AXIS));
        bottomContainer.setBackground(DARKER_BG);

       
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setBackground(DARKER_BG);
        progressPanel.setBorder(BorderFactory.createEmptyBorder(10, 50, 5, 50));
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setBackground(DARK_BG);
        progressBar.setForeground(ACCENT_BLUE);
        progressBar.setFont(new Font("SansSerif", Font.BOLD, 12));
        progressPanel.add(progressBar, BorderLayout.CENTER);

      
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        inputPanel.setBackground(DARKER_BG);

        JTextField taskInput = new JTextField(20);
        taskInput.setFont(new Font("SansSerif", Font.PLAIN, 16));
        
        JButton addBtn = createBtn("Add", ACCENT_BLUE);
        JButton toggleBtn = createBtn("Done", SUCCESS_GREEN);
        JButton delBtn = createBtn("Delete", DANGER_RED);
        JButton clearBtn = createBtn("Clear All", Color.GRAY);

        inputPanel.add(taskInput);
        inputPanel.add(addBtn);
        inputPanel.add(toggleBtn);
        inputPanel.add(delBtn);
        inputPanel.add(clearBtn);

       
        statsLabel = new JLabel("Status: Loading...");
        statsLabel.setForeground(Color.LIGHT_GRAY);
        statsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        bottomContainer.add(progressPanel);
        bottomContainer.add(inputPanel);
        bottomContainer.add(statsLabel);
        frame.add(bottomContainer, BorderLayout.SOUTH);

       
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
            private void filter() {
                String text = searchField.getText();
                sorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + text));
            }
        });

        addBtn.addActionListener(e -> {
            String val = taskInput.getText().trim();
            if(!val.isEmpty()) { executeSQL("INSERT INTO tasks (title) VALUES ('" + val + "')"); taskInput.setText(""); refreshTable(); }
        });

        toggleBtn.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow != -1) {
                int modelRow = table.convertRowIndexToModel(viewRow);
                int id = (int) model.getValueAt(modelRow, 0);
                String next = model.getValueAt(modelRow, 2).equals("Pending") ? "Completed" : "Pending";
                executeSQL("UPDATE tasks SET status = '" + next + "' WHERE id = " + id);
                refreshTable();
            }
        });

        delBtn.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow != -1) { 
                int modelRow = table.convertRowIndexToModel(viewRow);
                executeSQL("DELETE FROM tasks WHERE id = " + model.getValueAt(modelRow, 0)); 
                refreshTable(); 
            }
        });

        clearBtn.addActionListener(e -> {
            if(JOptionPane.showConfirmDialog(frame, "Delete everything?") == 0) { executeSQL("DELETE FROM tasks"); refreshTable(); }
        });

        refreshTable();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static JButton createBtn(String txt, Color c) {
        JButton b = new JButton(txt);
        b.setBackground(c);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static void setupDatabase() {
        try (Connection c = DriverManager.getConnection(DB_URL)) {
            c.createStatement().execute("CREATE TABLE IF NOT EXISTS tasks (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, status TEXT DEFAULT 'Pending')");
        } catch (Exception e) {}
    }

    private static void executeSQL(String sql) {
        try (Connection c = DriverManager.getConnection(DB_URL)) { c.createStatement().execute(sql); } catch (Exception e) {}
    }

    private static void refreshTable() {
        model.setRowCount(0);
        int pending = 0;
        int completed = 0;
        try (Connection c = DriverManager.getConnection(DB_URL);
             ResultSet rs = c.createStatement().executeQuery("SELECT * FROM tasks")) {
            while (rs.next()) {
                String status = rs.getString("status");
                if (status.equals("Pending")) pending++; else completed++;
                model.addRow(new Object[]{rs.getInt("id"), rs.getString("title"), status});
            }
        } catch (Exception e) {}
        
        
        int total = pending + completed;
        int percent = (total == 0) ? 0 : (int) ((double) completed / total * 100);
        
        progressBar.setValue(percent);
        progressBar.setString(percent + "% Completed");
        
        
        if (percent == 100) progressBar.setForeground(SUCCESS_GREEN);
        else if (percent > 50) progressBar.setForeground(ACCENT_BLUE);
        else progressBar.setForeground(WARNING_YELLOW);

        statsLabel.setText("📊  " + pending + " Pending  |  " + completed + " Finished  |  Total: " + total);
    }
}