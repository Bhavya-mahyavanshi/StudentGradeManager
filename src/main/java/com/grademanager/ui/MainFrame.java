package com.grademanager.ui;

import com.grademanager.db.DatabaseManager;

import javax.swing.*;

public class MainFrame extends JFrame {

    private final StudentPanel studentPanel;
    private final GradePanel   gradePanel;

    public MainFrame() {
        setTitle("Student Grade Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setMinimumSize(new java.awt.Dimension(700, 450));
        setLocationRelativeTo(null);

        studentPanel = new StudentPanel();
        gradePanel   = new GradePanel();

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Students", studentPanel);
        tabs.addTab("Grades",   gradePanel);

        // When switching to the Grades tab, refresh the student combo
        // so any newly added students appear immediately
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedComponent() == gradePanel) {
                gradePanel.populateStudentCombo();
            }
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