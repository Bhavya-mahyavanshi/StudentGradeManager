package com.grademanager.ui;

import com.grademanager.model.Student;

import javax.swing.*;
import java.awt.*;

public class StudentFormDialog extends JDialog {

    private JTextField firstNameField, lastNameField, studentIdField,
            emailField, majorField;
    private boolean confirmed = false;

    public StudentFormDialog(Frame parent, Student existing) {
        super(parent, existing == null ? "Add student" : "Edit student", true);
        setLayout(new BorderLayout(8, 8));
        setResizable(false);

        add(buildForm(existing), BorderLayout.CENTER);
        add(buildButtons(),      BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }

    private JPanel buildForm(Student s) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(4, 0, 4, 12);
        GridBagConstraints fc = new GridBagConstraints();
        fc.fill   = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1.0;
        fc.insets  = new Insets(4, 0, 4, 0);

        firstNameField = new JTextField(s != null ? s.getFirstName() : "", 20);
        lastNameField  = new JTextField(s != null ? s.getLastName()  : "", 20);
        studentIdField = new JTextField(s != null ? s.getStudentId() : "", 20);
        emailField     = new JTextField(s != null ? s.getEmail()     : "", 20);
        majorField     = new JTextField(s != null ? s.getMajor()     : "", 20);

        String[][] rows = {
                {"First name *", null},
                {"Last name *",  null},
                {"Student ID *", null},
                {"Email",        null},
                {"Major",        null}
        };
        JTextField[] fields = {firstNameField, lastNameField, studentIdField,
                emailField, majorField};

        for (int i = 0; i < fields.length; i++) {
            lc.gridy = fc.gridy = i;
            lc.gridx = 0; fc.gridx = 1;
            panel.add(new JLabel(rows[i][0]), lc);
            panel.add(fields[i], fc);
        }
        return panel;
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton cancel = new JButton("Cancel");
        JButton save   = new JButton("Save");
        save.setDefaultCapable(true);
        getRootPane().setDefaultButton(save);

        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        panel.add(cancel);
        panel.add(save);
        return panel;
    }

    // Accessors
    public boolean isConfirmed()   { return confirmed; }
    public String getFirstName()   { return firstNameField.getText(); }
    public String getLastName()    { return lastNameField.getText(); }
    public String getStudentId()   { return studentIdField.getText(); }
    public String getEmail()       { return emailField.getText(); }
    public String getMajor()       { return majorField.getText(); }
}