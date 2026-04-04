package com.grademanager.ui;

import com.grademanager.model.Student;
import com.grademanager.service.StudentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class StudentPanel extends JPanel {

    private final StudentService service = new StudentService();

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton addBtn, editBtn, deleteBtn;

    // Column indices
    private static final int COL_ID    = 0;
    private static final int COL_FNAME = 1;
    private static final int COL_LNAME = 2;
    private static final int COL_SID   = 3;
    private static final int COL_EMAIL = 4;
    private static final int COL_MAJOR = 5;

    public StudentPanel() {
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildTable(),     BorderLayout.CENTER);
        add(buildButtonBar(), BorderLayout.SOUTH);
        refreshTable(null);
    }

    // ── Top bar: title + search ────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));

        JLabel title = new JLabel("Student Management");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        panel.add(title, BorderLayout.WEST);

        searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "Search name or ID…");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { onSearch(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { onSearch(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { onSearch(); }
        });

        JPanel searchWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        searchWrap.add(new JLabel("Search: "));
        searchWrap.add(searchField);
        panel.add(searchWrap, BorderLayout.EAST);

        return panel;
    }

    // ── Table ──────────────────────────────────────────────────────────────
    private JScrollPane buildTable() {
        String[] columns = {"DB ID", "First name", "Last name", "Student ID", "Email", "Major"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.getTableHeader().setReorderingAllowed(false);
        table.setAutoCreateRowSorter(true);

        // Hide the raw DB ID column — we use it internally
        table.getColumnModel().getColumn(COL_ID).setMinWidth(0);
        table.getColumnModel().getColumn(COL_ID).setMaxWidth(0);
        table.getColumnModel().getColumn(COL_ID).setWidth(0);

        // Column widths
        table.getColumnModel().getColumn(COL_FNAME).setPreferredWidth(120);
        table.getColumnModel().getColumn(COL_LNAME).setPreferredWidth(120);
        table.getColumnModel().getColumn(COL_SID).setPreferredWidth(100);
        table.getColumnModel().getColumn(COL_EMAIL).setPreferredWidth(180);
        table.getColumnModel().getColumn(COL_MAJOR).setPreferredWidth(140);

        // Enable edit/delete only when a row is selected
        table.getSelectionModel().addListSelectionListener(e -> updateButtonStates());

        // Double-click to edit
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) openEditDialog();
            }
        });

        return new JScrollPane(table);
    }

    // ── Button bar ─────────────────────────────────────────────────────────
    private JPanel buildButtonBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        addBtn    = new JButton("Add student");
        editBtn   = new JButton("Edit");
        deleteBtn = new JButton("Delete");

        editBtn.setEnabled(false);
        deleteBtn.setEnabled(false);

        addBtn.addActionListener(e    -> openAddDialog());
        editBtn.addActionListener(e   -> openEditDialog());
        deleteBtn.addActionListener(e -> deleteSelected());

        panel.add(addBtn);
        panel.add(editBtn);
        panel.add(deleteBtn);
        return panel;
    }

    // ── Data ───────────────────────────────────────────────────────────────
    private void refreshTable(String query) {
        tableModel.setRowCount(0);
        List<Student> students = (query == null || query.isBlank())
                ? service.getAllStudents()
                : service.searchStudents(query);

        for (Student s : students) {
            tableModel.addRow(new Object[]{
                    s.getId(),
                    s.getFirstName(),
                    s.getLastName(),
                    s.getStudentId(),
                    s.getEmail(),
                    s.getMajor()
            });
        }
        updateButtonStates();
    }

    private Student getSelectedStudent() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        int dbId = (int) tableModel.getValueAt(modelRow, COL_ID);
        // Rebuild a Student from the table row (avoids extra DB call)
        return new Student(
                dbId,
                (String) tableModel.getValueAt(modelRow, COL_FNAME),
                (String) tableModel.getValueAt(modelRow, COL_LNAME),
                (String) tableModel.getValueAt(modelRow, COL_SID),
                (String) tableModel.getValueAt(modelRow, COL_EMAIL),
                (String) tableModel.getValueAt(modelRow, COL_MAJOR),
                null
        );
    }

    private void updateButtonStates() {
        boolean selected = table.getSelectedRow() >= 0;
        editBtn.setEnabled(selected);
        deleteBtn.setEnabled(selected);
    }

    private void onSearch() {
        refreshTable(searchField.getText());
    }

    // ── Dialogs ────────────────────────────────────────────────────────────
    private void openAddDialog() {
        StudentFormDialog dialog = new StudentFormDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            String error = service.addStudent(
                    dialog.getFirstName(), dialog.getLastName(),
                    dialog.getStudentId(), dialog.getEmail(), dialog.getMajor());
            if (error != null) {
                JOptionPane.showMessageDialog(this, error, "Validation error",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                refreshTable(searchField.getText());
            }
        }
    }

    private void openEditDialog() {
        Student selected = getSelectedStudent();
        if (selected == null) return;

        StudentFormDialog dialog = new StudentFormDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), selected);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            String error = service.updateStudent(selected,
                    dialog.getFirstName(), dialog.getLastName(),
                    dialog.getStudentId(), dialog.getEmail(), dialog.getMajor());
            if (error != null) {
                JOptionPane.showMessageDialog(this, error, "Validation error",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                refreshTable(searchField.getText());
            }
        }
    }

    private void deleteSelected() {
        Student selected = getSelectedStudent();
        if (selected == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete " + selected.getFullName() + "?\nThis will also remove all their grades.",
                "Confirm delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (!service.deleteStudent(selected.getId())) {
                JOptionPane.showMessageDialog(this, "Could not delete student.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                refreshTable(searchField.getText());
            }
        }
    }
}