package com.grademanager.model;

public class Grade {
    private int id;
    private int studentDbId;   // FK → students.id
    private String courseName;
    private String courseCode;
    private double grade;       // 0.0 – 100.0
    private int creditHours;
    private String semester;

    public Grade(int studentDbId, String courseName, String courseCode,
                 double grade, int creditHours, String semester) {
        this.studentDbId = studentDbId;
        this.courseName  = courseName;
        this.courseCode  = courseCode;
        this.grade       = grade;
        this.creditHours = creditHours;
        this.semester    = semester;
    }

    public Grade(int id, int studentDbId, String courseName, String courseCode,
                 double grade, int creditHours, String semester) {
        this(studentDbId, courseName, courseCode, grade, creditHours, semester);
        this.id = id;
    }

    // Getters and setters
    public int getId()                        { return id; }
    public void setId(int id)                 { this.id = id; }
    public int getStudentDbId()               { return studentDbId; }
    public void setStudentDbId(int v)         { this.studentDbId = v; }
    public String getCourseName()             { return courseName; }
    public void setCourseName(String v)       { this.courseName = v; }
    public String getCourseCode()             { return courseCode; }
    public void setCourseCode(String v)       { this.courseCode = v; }
    public double getGrade()                  { return grade; }
    public void setGrade(double v)            { this.grade = v; }
    public int getCreditHours()               { return creditHours; }
    public void setCreditHours(int v)         { this.creditHours = v; }
    public String getSemester()               { return semester; }
    public void setSemester(String v)         { this.semester = v; }
}