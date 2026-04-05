package com.grademanager.ui;

import com.grademanager.model.Grade;

import javax.swing.*;
import java.awt.*;

public class GradeFormDialog extends JDialog {

    private JTextField courseNameField, courseCodeField,
            gradeField, creditField, semesterField;
    private boolean confirmed = false;

    public GradeFormDialog(Frame parent, Grade existing) {
        super(parent, existing == null ? "Add grade" : "Edit grade", true);
        setLayout(new BorderLayout(8, 8));
        setResizable(false);
        add(buildForm(existing), BorderLayout.CENTER);
        add(buildButtons(),      BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(parent);
    }

    private JPanel buildForm(Grade g) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(4, 0, 4, 12);

        GridBagConstraints fc = new GridBagConstraints();
        fc.fill    = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1.0;
        fc.insets  = new Insets(4, 0, 4, 0);

        courseNameField = new JTextField(g != null ? g.getCourseName()          : "", 22);
        courseCodeField = new JTextField(g != null ? g.getCourseCode()          : "", 22);
        gradeField      = new JTextField(g != null ? String.valueOf(g.getGrade()): "", 22);
        creditField     = new JTextField(g != null ? String.valueOf(g.getCreditHours()) : "3", 22);
        semesterField   = new JTextField(g != null ? g.getSemester()            : "", 22);

        String[] labels = {"Course name *", "Course code", "Grade (0–100) *",
                "Credit hours *", "Semester"};
        JTextField[] fields = {courseNameField, courseCodeField, gradeField,
                creditField, semesterField};

        for (int i = 0; i < fields.length; i++) {
            lc.gridy = fc.gridy = i;
            lc.gridx = 0; fc.gridx = 1;
            panel.add(new JLabel(labels[i]), lc);
            panel.add(fields[i], fc);
        }

        // Hint label under the grade field
        GridBagConstraints hc = new GridBagConstraints();
        hc.gridx = 1; hc.gridy = fields.length;
        hc.anchor = GridBagConstraints.WEST;
        hc.insets = new Insets(0, 0, 4, 0);
        JLabel hint = new JLabel("e.g. 85.5  →  B  (3.0 pts)");
        hint.setFont(hint.getFont().deriveFont(10f));
        hint.setForeground(Color.GRAY);
        panel.add(hint, hc);

        return panel;
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton cancel = new JButton("Cancel");
        JButton save   = new JButton("Save");
        getRootPane().setDefaultButton(save);

        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> { confirmed = true; dispose(); });

        panel.add(cancel);
        panel.add(save);
        return panel;
    }

    public boolean isConfirmed()    { return confirmed; }
    public String getCourseName()   { return courseNameField.getText(); }
    public String getCourseCode()   { return courseCodeField.getText(); }
    public String getGradeValue()   { return gradeField.getText(); }
    public String getCreditHours()  { return creditField.getText(); }
    public String getSemester()     { return semesterField.getText(); }
}