package com.grademanager.ui;

import com.grademanager.model.Grade;
import com.grademanager.model.Student;
import com.grademanager.service.GPACalculator;
import com.grademanager.service.GradeService;
import com.grademanager.service.StudentService;
import com.grademanager.util.CSVExporter;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportPanel extends JPanel {

    private final StudentService studentService = new StudentService();
    private final GradeService   gradeService   = new GradeService();

    // State
    private List<Student>              allStudents = new ArrayList<>();
    private Map<Integer, List<Grade>>  gradeMap    = new HashMap<>();

    // Filter controls
    private JTextField searchField;
    private JComboBox<String> standingFilter;
    private JComboBox<String> majorFilter;

    // Stats bar
    private JLabel statTotal, statAvgGPA, statHonours;

    // Table
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    // Column indices
    private static final int COL_DBID     = 0;
    private static final int COL_SID      = 1;
    private static final int COL_NAME     = 2;
    private static final int COL_MAJOR    = 3;
    private static final int COL_COURSES  = 4;
    private static final int COL_CREDITS  = 5;
    private static final int COL_GPA      = 6;
    private static final int COL_LETTER   = 7;
    private static final int COL_STANDING = 8;

    public ReportPanel() {
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buildHeader(),    BorderLayout.NORTH);
        add(buildTable(),     BorderLayout.CENTER);
        add(buildFooter(),    BorderLayout.SOUTH);
    }

    // ── Header: title + filters + stats ───────────────────────────────────
    private JPanel buildHeader() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));

        // Row 1 — title + refresh button
        JPanel titleRow = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Reports & Overview");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        titleRow.add(title, BorderLayout.WEST);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadData());
        titleRow.add(refreshBtn, BorderLayout.EAST);
        wrapper.add(titleRow, BorderLayout.NORTH);

        // Row 2 — filters
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        searchField = new JTextField(16);
        searchField.putClientProperty("JTextField.placeholderText", "Search name or ID…");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { applyFilters(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { applyFilters(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
        });

        standingFilter = new JComboBox<>(new String[]{
                "All standings", "Summa Cum Laude", "Magna Cum Laude",
                "Cum Laude", "Good Standing", "Academic Probation", "No grades"
        });
        standingFilter.addActionListener(e -> applyFilters());

        majorFilter = new JComboBox<>();
        majorFilter.addItem("All majors");
        majorFilter.addActionListener(e -> applyFilters());

        filterRow.add(new JLabel("Search:"));
        filterRow.add(searchField);
        filterRow.add(new JLabel("Standing:"));
        filterRow.add(standingFilter);
        filterRow.add(new JLabel("Major:"));
        filterRow.add(majorFilter);
        wrapper.add(filterRow, BorderLayout.CENTER);

        // Row 3 — stats bar
        JPanel statsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 4));
        statsRow.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0,
                UIManager.getColor("Separator.foreground")));

        statTotal   = new JLabel("Students: 0");
        statAvgGPA  = new JLabel("Avg GPA: —");
        statHonours = new JLabel("Honour roll (≥3.5): 0");

        Font statFont = statTotal.getFont().deriveFont(Font.BOLD, 12f);
        statTotal.setFont(statFont);
        statAvgGPA.setFont(statFont);
        statHonours.setFont(statFont);

        statsRow.add(statTotal);
        statsRow.add(statAvgGPA);
        statsRow.add(statHonours);
        wrapper.add(statsRow, BorderLayout.SOUTH);

        return wrapper;
    }

    // ── Table ──────────────────────────────────────────────────────────────
    private JScrollPane buildTable() {
        String[] cols = {"DB ID", "Student ID", "Name", "Major",
                "Courses", "Credits", "GPA", "Top grade", "Standing"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                if (c == COL_GPA || c == COL_COURSES || c == COL_CREDITS)
                    return Double.class;
                return String.class;
            }
        };

        table = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(26);
        table.getTableHeader().setReorderingAllowed(false);

        // Hide DB ID
        table.getColumnModel().getColumn(COL_DBID).setMinWidth(0);
        table.getColumnModel().getColumn(COL_DBID).setMaxWidth(0);
        table.getColumnModel().getColumn(COL_DBID).setWidth(0);

        // Column widths
        table.getColumnModel().getColumn(COL_SID).setPreferredWidth(90);
        table.getColumnModel().getColumn(COL_NAME).setPreferredWidth(160);
        table.getColumnModel().getColumn(COL_MAJOR).setPreferredWidth(140);
        table.getColumnModel().getColumn(COL_COURSES).setPreferredWidth(60);
        table.getColumnModel().getColumn(COL_CREDITS).setPreferredWidth(60);
        table.getColumnModel().getColumn(COL_GPA).setPreferredWidth(70);
        table.getColumnModel().getColumn(COL_LETTER).setPreferredWidth(80);
        table.getColumnModel().getColumn(COL_STANDING).setPreferredWidth(160);

        // Colour-code GPA column
        table.getColumnModel().getColumn(COL_GPA)
                .setCellRenderer(new GPACellRenderer());

        return new JScrollPane(table);
    }

    // ── Footer: export buttons ─────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        JButton exportAll = new JButton("Export all → CSV");
        JButton exportOne = new JButton("Export selected student → CSV");

        exportAll.addActionListener(e -> exportAllToCSV());
        exportOne.addActionListener(e -> exportSelectedToCSV());

        panel.add(exportAll);
        panel.add(exportOne);
        return panel;
    }

    // ── Data loading ───────────────────────────────────────────────────────
    public void loadData() {
        allStudents = studentService.getAllStudents();
        gradeMap.clear();
        for (Student s : allStudents) {
            gradeMap.put(s.getId(), gradeService.getGradesForStudent(s.getId()));
        }
        rebuildMajorFilter();
        populateTable(allStudents);
        updateStats(allStudents);
    }

    private void rebuildMajorFilter() {
        String prev = (String) majorFilter.getSelectedItem();
        majorFilter.removeAllItems();
        majorFilter.addItem("All majors");
        allStudents.stream()
                .map(Student::getMajor)
                .filter(m -> m != null && !m.isBlank())
                .distinct()
                .sorted()
                .forEach(majorFilter::addItem);
        if (prev != null) majorFilter.setSelectedItem(prev);
    }

    private void populateTable(List<Student> students) {
        tableModel.setRowCount(0);
        for (Student s : students) {
            List<Grade> grades = gradeMap.getOrDefault(s.getId(), List.of());
            double gpa = GPACalculator.calculateGPA(grades);
            int totalCredits = grades.stream().mapToInt(Grade::getCreditHours).sum();

            // Best (highest) grade letter across all courses
            String topLetter = grades.stream()
                    .mapToDouble(Grade::getGrade)
                    .max()
                    .stream()
                    .mapToObj(GPACalculator::toLetter)
                    .findFirst()
                    .orElse("—");

            tableModel.addRow(new Object[]{
                    s.getId(),
                    s.getStudentId(),
                    s.getFullName(),
                    s.getMajor(),
                    grades.size(),
                    totalCredits,
                    gpa,
                    topLetter,
                    GPACalculator.standing(gpa)
            });
        }
    }

    private void updateStats(List<Student> students) {
        int count = students.size();
        long honours = students.stream()
                .filter(s -> GPACalculator.calculateGPA(
                        gradeMap.getOrDefault(s.getId(), List.of())) >= 3.5)
                .count();

        double avgGpa = students.stream()
                .mapToDouble(s -> GPACalculator.calculateGPA(
                        gradeMap.getOrDefault(s.getId(), List.of())))
                .average()
                .orElse(0.0);

        statTotal.setText("Students: " + count);
        statAvgGPA.setText(String.format("Avg GPA: %.2f", avgGpa));
        statHonours.setText("Honour roll (≥3.5): " + honours);
    }

    // ── Filtering ──────────────────────────────────────────────────────────
    private void applyFilters() {
        String query    = searchField.getText().trim().toLowerCase();
        String standing = (String) standingFilter.getSelectedItem();
        String major    = (String) majorFilter.getSelectedItem();

        // Guard — combo fires during rebuild with null selection
        if (standing == null || major == null) return;

        List<Student> filtered = new ArrayList<>();
        for (Student s : allStudents) {
            // Name / ID search
            if (!query.isEmpty()) {
                boolean matches = s.getFullName().toLowerCase().contains(query)
                        || s.getStudentId().toLowerCase().contains(query);
                if (!matches) continue;
            }
            // Standing filter
            if (!"All standings".equals(standing)) {
                List<Grade> grades = gradeMap.getOrDefault(s.getId(), List.of());
                double gpa = GPACalculator.calculateGPA(grades);
                if (!GPACalculator.standing(gpa).equals(standing)) continue;
            }
            // Major filter
            if (!"All majors".equals(major)) {
                String studentMajor = s.getMajor() != null ? s.getMajor() : "";
                if (!major.equalsIgnoreCase(studentMajor)) continue;
            }
            filtered.add(s);
        }

        populateTable(filtered);
        updateStats(filtered);
    }

    // ── Export ─────────────────────────────────────────────────────────────
    private void exportAllToCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("grade_report_all.csv"));
        chooser.setDialogTitle("Save summary report");

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        String path = chooser.getSelectedFile().getAbsolutePath();
        if (!path.endsWith(".csv")) path += ".csv";

        try {
            CSVExporter.exportSummaryReport(allStudents, gradeMap, path);
            JOptionPane.showMessageDialog(this,
                    "Report saved to:\n" + path,
                    "Export successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Export failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportSelectedToCSV() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Select a student row first.",
                    "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int dbId     = (int) tableModel.getValueAt(modelRow, COL_DBID);

        Student student = allStudents.stream()
                .filter(s -> s.getId() == dbId)
                .findFirst().orElse(null);
        if (student == null) return;

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(student.getStudentId() + "_grades.csv"));
        chooser.setDialogTitle("Save student grade report");

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        String path = chooser.getSelectedFile().getAbsolutePath();
        if (!path.endsWith(".csv")) path += ".csv";

        try {
            List<Grade> grades = gradeMap.getOrDefault(student.getId(), List.of());
            CSVExporter.exportStudentGrades(student, grades, path);
            JOptionPane.showMessageDialog(this,
                    "Report saved to:\n" + path,
                    "Export successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Export failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── GPA cell renderer (colour coded) ──────────────────────────────────
    private static class GPACellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int col) {

            super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, col);
            setHorizontalAlignment(SwingConstants.CENTER);

            if (value instanceof Double gpa && !isSelected) {
                if      (gpa >= 3.5) setForeground(new Color(0, 140, 70));
                else if (gpa >= 3.0) setForeground(new Color(60, 130, 200));
                else if (gpa >= 2.0) setForeground(new Color(180, 120, 0));
                else                 setForeground(new Color(190, 40, 40));
            } else {
                setForeground(table.getForeground());
            }

            if (value instanceof Double d) {
                setText(String.format("%.2f", d));
            }
            return this;
        }
    }
}