package com.grademanager.ui;

import com.grademanager.model.Grade;
import com.grademanager.model.Student;
import com.grademanager.service.GPACalculator;
import com.grademanager.service.GradeService;
import com.grademanager.service.StudentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class GradePanel extends JPanel {

    private final GradeService   gradeService   = new GradeService();
    private final StudentService studentService = new StudentService();

    // Top controls
    private JComboBox<Student> studentCombo;
    private JLabel gpaLabel, standingLabel;

    // Table
    private JTable table;
    private DefaultTableModel tableModel;

    // Buttons
    private JButton addBtn, editBtn, deleteBtn;

    // Column indices
    private static final int COL_ID      = 0;
    private static final int COL_COURSE  = 1;
    private static final int COL_CODE    = 2;
    private static final int COL_GRADE   = 3;
    private static final int COL_LETTER  = 4;
    private static final int COL_CREDITS = 5;
    private static final int COL_SEM     = 6;

    public GradePanel() {
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildTable(),     BorderLayout.CENTER);
        add(buildButtonBar(), BorderLayout.SOUTH);
        populateStudentCombo();
    }

    // ── Top bar: student picker + GPA display ──────────────────────────────
    private JPanel buildTopBar() {
        JPanel panel = new JPanel(new BorderLayout(12, 0));

        // Left: label + combo
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JLabel title = new JLabel("Grade Management");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        left.add(title);
        left.add(new JLabel("Student:"));

        studentCombo = new JComboBox<>();
        studentCombo.setPreferredSize(new Dimension(220, 26));
        studentCombo.addActionListener(e -> onStudentSelected());
        left.add(studentCombo);
        panel.add(left, BorderLayout.WEST);

        // Right: GPA badge
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        gpaLabel     = new JLabel("GPA: —");
        standingLabel = new JLabel("");
        gpaLabel.setFont(gpaLabel.getFont().deriveFont(Font.BOLD, 14f));
        standingLabel.setFont(standingLabel.getFont().deriveFont(Font.PLAIN, 12f));
        standingLabel.setForeground(Color.GRAY);
        right.add(gpaLabel);
        right.add(standingLabel);
        panel.add(right, BorderLayout.EAST);

        return panel;
    }

    // ── Table ──────────────────────────────────────────────────────────────
    private JScrollPane buildTable() {
        String[] cols = {"ID", "Course name", "Code", "Grade (%)", "Letter", "Credits", "Semester"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.getTableHeader().setReorderingAllowed(false);
        table.setAutoCreateRowSorter(true);

        // Hide raw DB ID column
        table.getColumnModel().getColumn(COL_ID).setMinWidth(0);
        table.getColumnModel().getColumn(COL_ID).setMaxWidth(0);
        table.getColumnModel().getColumn(COL_ID).setWidth(0);

        // Column widths
        table.getColumnModel().getColumn(COL_COURSE).setPreferredWidth(180);
        table.getColumnModel().getColumn(COL_CODE).setPreferredWidth(80);
        table.getColumnModel().getColumn(COL_GRADE).setPreferredWidth(80);
        table.getColumnModel().getColumn(COL_LETTER).setPreferredWidth(60);
        table.getColumnModel().getColumn(COL_CREDITS).setPreferredWidth(60);
        table.getColumnModel().getColumn(COL_SEM).setPreferredWidth(100);

        table.getSelectionModel().addListSelectionListener(e -> updateButtonStates());
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

        addBtn    = new JButton("Add grade");
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

    // ── Population helpers ─────────────────────────────────────────────────
    public void populateStudentCombo() {
        Student prev = (Student) studentCombo.getSelectedItem();
        studentCombo.removeAllItems();
        studentService.getAllStudents().forEach(studentCombo::addItem);

        // Restore previous selection if still present
        if (prev != null) {
            for (int i = 0; i < studentCombo.getItemCount(); i++) {
                if (studentCombo.getItemAt(i).getId() == prev.getId()) {
                    studentCombo.setSelectedIndex(i);
                    return;
                }
            }
        }
        // Otherwise trigger refresh for whoever is selected
        if (studentCombo.getItemCount() > 0) onStudentSelected();
    }

    private void onStudentSelected() {
        Student s = (Student) studentCombo.getSelectedItem();
        if (s == null) {
            tableModel.setRowCount(0);
            gpaLabel.setText("GPA: —");
            standingLabel.setText("");
            return;
        }
        refreshTable(s);
    }

    private void refreshTable(Student s) {
        tableModel.setRowCount(0);
        List<Grade> grades = gradeService.getGradesForStudent(s.getId());

        for (Grade g : grades) {
            tableModel.addRow(new Object[]{
                    g.getId(),
                    g.getCourseName(),
                    g.getCourseCode(),
                    String.format("%.1f", g.getGrade()),
                    GPACalculator.toLetter(g.getGrade()),
                    g.getCreditHours(),
                    g.getSemester()
            });
        }

        // Update GPA display
        double gpa = GPACalculator.calculateGPA(grades);
        gpaLabel.setText(String.format("GPA: %.2f / 4.00", gpa));
        standingLabel.setText("· " + GPACalculator.standing(gpa));
        updateButtonStates();
    }

    private void updateButtonStates() {
        Student s       = (Student) studentCombo.getSelectedItem();
        boolean hasStudent = s != null;
        boolean hasRow     = table.getSelectedRow() >= 0;
        addBtn.setEnabled(hasStudent);
        editBtn.setEnabled(hasRow);
        deleteBtn.setEnabled(hasRow);
    }

    // ── Selected grade helper ──────────────────────────────────────────────
    private Grade getSelectedGrade() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return null;
        int modelRow = table.convertRowIndexToModel(viewRow);

        Student s = (Student) studentCombo.getSelectedItem();
        int gradeId = (int) tableModel.getValueAt(modelRow, COL_ID);

        // Fetch fresh from service so we have all fields
        return gradeService.getGradesForStudent(s.getId())
                .stream()
                .filter(g -> g.getId() == gradeId)
                .findFirst()
                .orElse(null);
    }

    // ── Dialogs ────────────────────────────────────────────────────────────
    private void openAddDialog() {
        Student s = (Student) studentCombo.getSelectedItem();
        if (s == null) return;

        GradeFormDialog dialog = new GradeFormDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            String error = gradeService.addGrade(
                    s.getId(),
                    dialog.getCourseName(),
                    dialog.getCourseCode(),
                    dialog.getGradeValue(),
                    dialog.getCreditHours(),
                    dialog.getSemester());
            if (error != null) {
                JOptionPane.showMessageDialog(this, error,
                        "Validation error", JOptionPane.WARNING_MESSAGE);
            } else {
                refreshTable(s);
            }
        }
    }

    private void openEditDialog() {
        Grade selected = getSelectedGrade();
        if (selected == null) return;

        GradeFormDialog dialog = new GradeFormDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), selected);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            String error = gradeService.updateGrade(selected,
                    dialog.getCourseName(),
                    dialog.getCourseCode(),
                    dialog.getGradeValue(),
                    dialog.getCreditHours(),
                    dialog.getSemester());
            if (error != null) {
                JOptionPane.showMessageDialog(this, error,
                        "Validation error", JOptionPane.WARNING_MESSAGE);
            } else {
                refreshTable((Student) studentCombo.getSelectedItem());
            }
        }
    }

    private void deleteSelected() {
        Grade selected = getSelectedGrade();
        if (selected == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete grade for \"" + selected.getCourseName() + "\"?",
                "Confirm delete", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (!gradeService.deleteGrade(selected.getId())) {
                JOptionPane.showMessageDialog(this, "Could not delete grade.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                refreshTable((Student) studentCombo.getSelectedItem());
            }
        }
    }
}