package com.grademanager.service;

import com.grademanager.model.Grade;
import java.util.List;

public class GPACalculator {

    // Weighted GPA on a 4.0 scale using credit hours
    public static double calculateGPA(List<Grade> grades) {
        if (grades == null || grades.isEmpty()) return 0.0;

        double totalPoints  = 0.0;
        int    totalCredits = 0;

        for (Grade g : grades) {
            totalPoints  += toGradePoints(g.getGrade()) * g.getCreditHours();
            totalCredits += g.getCreditHours();
        }

        if (totalCredits == 0) return 0.0;
        return Math.round((totalPoints / totalCredits) * 100.0) / 100.0;
    }

    // Percentage score → 4.0 grade points
    public static double toGradePoints(double score) {
        if (score >= 93) return 4.0;
        if (score >= 90) return 3.7;
        if (score >= 87) return 3.3;
        if (score >= 83) return 3.0;
        if (score >= 80) return 2.7;
        if (score >= 77) return 2.3;
        if (score >= 73) return 2.0;
        if (score >= 70) return 1.7;
        if (score >= 67) return 1.3;
        if (score >= 63) return 1.0;
        if (score >= 60) return 0.7;
        return 0.0;
    }

    // Percentage score → letter grade
    public static String toLetter(double score) {
        if (score >= 93) return "A";
        if (score >= 90) return "A-";
        if (score >= 87) return "B+";
        if (score >= 83) return "B";
        if (score >= 80) return "B-";
        if (score >= 77) return "C+";
        if (score >= 73) return "C";
        if (score >= 70) return "C-";
        if (score >= 67) return "D+";
        if (score >= 63) return "D";
        if (score >= 60) return "D-";
        return "F";
    }

    // GPA number → standing label
    public static String standing(double gpa) {
        if (gpa >= 3.7) return "Summa Cum Laude";
        if (gpa >= 3.5) return "Magna Cum Laude";
        if (gpa >= 3.0) return "Cum Laude";
        if (gpa >= 2.0) return "Good Standing";
        if (gpa >  0.0) return "Academic Probation";
        return "No grades";
    }
}