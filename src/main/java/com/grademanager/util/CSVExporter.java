package com.grademanager.util;

import com.grademanager.model.Grade;
import com.grademanager.model.Student;
import com.grademanager.service.GPACalculator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class CSVExporter {

    /**
     * Exports a full report: one row per student with their GPA summary.
     * Returns the file path written, or throws IOException on failure.
     */
    public static String exportSummaryReport(
            List<Student> students,
            Map<Integer, List<Grade>> gradeMap,
            String filePath) throws IOException {

        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {

            // Header
            pw.println("Student ID,First Name,Last Name,Email,Major," +
                    "Total Courses,Total Credits,GPA,Letter Standing,Exported At");

            String exportedAt = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            for (Student s : students) {
                List<Grade> grades = gradeMap.getOrDefault(s.getId(), List.of());
                double gpa         = GPACalculator.calculateGPA(grades);
                int totalCredits   = grades.stream().mapToInt(Grade::getCreditHours).sum();

                pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d,%d,%.2f,\"%s\",\"%s\"%n",
                        escape(s.getStudentId()),
                        escape(s.getFirstName()),
                        escape(s.getLastName()),
                        escape(s.getEmail()),
                        escape(s.getMajor()),
                        grades.size(),
                        totalCredits,
                        gpa,
                        GPACalculator.standing(gpa),
                        exportedAt);
            }
        }
        return filePath;
    }

    /**
     * Exports all grades for a single student — one row per course.
     */
    public static String exportStudentGrades(
            Student student,
            List<Grade> grades,
            String filePath) throws IOException {

        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {

            pw.println("Student: " + student.getFullName() +
                    " (" + student.getStudentId() + ")");
            pw.println("Exported: " + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            pw.println(); // blank line before data

            pw.println("Course Name,Course Code,Grade (%),Letter,Grade Points,Credits,Semester");

            for (Grade g : grades) {
                pw.printf("\"%s\",\"%s\",%.1f,\"%s\",%.1f,%d,\"%s\"%n",
                        escape(g.getCourseName()),
                        escape(g.getCourseCode()),
                        g.getGrade(),
                        GPACalculator.toLetter(g.getGrade()),
                        GPACalculator.toGradePoints(g.getGrade()),
                        g.getCreditHours(),
                        escape(g.getSemester()));
            }

            // Footer summary
            pw.println();
            double gpa = GPACalculator.calculateGPA(grades);
            int totalCredits = grades.stream().mapToInt(Grade::getCreditHours).sum();
            pw.printf(",,,,,,,%n");
            pw.printf("Cumulative GPA,%.2f / 4.00%n", gpa);
            pw.printf("Total Credits,%d%n", totalCredits);
            pw.printf("Standing,%s%n", GPACalculator.standing(gpa));
        }
        return filePath;
    }

    // Escape double-quotes inside field values
    private static String escape(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}