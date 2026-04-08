package com.grademanager.ui;

import com.grademanager.db.DatabaseManager;
import java.awt.Component;
import javax.swing.*;

public class MainFrame extends JFrame {

    private final StudentPanel studentPanel;
    private final GradePanel   gradePanel;
    private final ReportPanel  reportPanel;

    public MainFrame() {
        setTitle("Student Grade Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(960, 620);
        setMinimumSize(new java.awt.Dimension(700, 450));
        setLocationRelativeTo(null);

        studentPanel = new StudentPanel();
        gradePanel   = new GradePanel();
        reportPanel  = new ReportPanel();

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Students", studentPanel);
        tabs.addTab("Grades",   gradePanel);
        tabs.addTab("Reports",  reportPanel);

        // Refresh each panel when its tab is opened
        tabs.addChangeListener(e -> {
            Component selected = tabs.getSelectedComponent();
            if (selected == gradePanel)  gradePanel.populateStudentCombo();
            if (selected == reportPanel) reportPanel.loadData();
        });

        add(tabs);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                DatabaseManager.getInstance().closeConnection();
            }
        });
    }
}