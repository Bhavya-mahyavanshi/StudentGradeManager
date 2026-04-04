package com.grademanager.ui;

import com.grademanager.db.DatabaseManager;

import javax.swing.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Student Grade Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setMinimumSize(new java.awt.Dimension(700, 450));
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Students", new StudentPanel());
        // Grades, Reports tabs come in later modules
        add(tabs);

        // Close DB cleanly on exit
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                DatabaseManager.getInstance().closeConnection();
            }
        });
    }
}