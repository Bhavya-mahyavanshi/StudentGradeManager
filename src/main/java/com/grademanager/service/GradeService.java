package com.grademanager.service;

import com.grademanager.db.GradeDAO;
import com.grademanager.model.Grade;
import java.util.List;

public class GradeService {

    private final GradeDAO dao;

    public GradeService() {
        this.dao = new GradeDAO();
    }

    public String addGrade(int studentDbId, String courseName, String courseCode,
                           String gradeStr, String creditStr, String semester) {
        String error = validate(courseName, gradeStr, creditStr);
        if (error != null) return error;

        double grade   = Double.parseDouble(gradeStr.trim());
        int creditHours = Integer.parseInt(creditStr.trim());

        Grade g = new Grade(studentDbId, courseName.trim(),
                courseCode.trim(), grade, creditHours, semester.trim());
        return dao.insert(g) ? null : "Database error — could not add grade.";
    }

    public String updateGrade(Grade g, String courseName, String courseCode,
                              String gradeStr, String creditStr, String semester) {
        String error = validate(courseName, gradeStr, creditStr);
        if (error != null) return error;

        double grade    = Double.parseDouble(gradeStr.trim());
        int creditHours = Integer.parseInt(creditStr.trim());

        g.setCourseName(courseName.trim());
        g.setCourseCode(courseCode.trim());
        g.setGrade(grade);
        g.setCreditHours(creditHours);
        g.setSemester(semester.trim());
        return dao.update(g) ? null : "Database error — could not update grade.";
    }

    public boolean deleteGrade(int id) {
        return dao.delete(id);
    }

    public List<Grade> getGradesForStudent(int studentDbId) {
        return dao.getByStudentId(studentDbId);
    }

    private String validate(String courseName, String gradeStr, String creditStr) {
        if (courseName == null || courseName.isBlank())
            return "Course name is required.";
        if (courseName.length() > 100)
            return "Course name must be under 100 characters.";

        if (gradeStr == null || gradeStr.isBlank())
            return "Grade is required.";
        try {
            double grade = Double.parseDouble(gradeStr.trim());
            if (grade < 0 || grade > 100)
                return "Grade must be between 0 and 100.";
        } catch (NumberFormatException e) {
            return "Grade must be a valid number (e.g. 85.5).";
        }

        if (creditStr == null || creditStr.isBlank())
            return "Credit hours are required.";
        try {
            int credits = Integer.parseInt(creditStr.trim());
            if (credits < 1 || credits > 6)
                return "Credit hours must be between 1 and 6.";
        } catch (NumberFormatException e) {
            return "Credit hours must be a whole number.";
        }

        return null;
    }
}