package com.grademanager.model;

public class Student {
    private int id;
    private String firstName;
    private String lastName;
    private String studentId;   // e.g. "STU-001"
    private String email;
    private String major;
    private String createdAt;

    // Constructor for new students (no DB id yet)
    public Student(String firstName, String lastName, String studentId,
                   String email, String major) {
        this.firstName = firstName;
        this.lastName  = lastName;
        this.studentId = studentId;
        this.email     = email;
        this.major     = major;
    }

    // Full constructor (loaded from DB)
    public Student(int id, String firstName, String lastName, String studentId,
                   String email, String major, String createdAt) {
        this(firstName, lastName, studentId, email, major);
        this.id        = id;
        this.createdAt = createdAt;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    // Getters and setters
    public int getId()                       { return id; }
    public void setId(int id)                { this.id = id; }
    public String getFirstName()             { return firstName; }
    public void setFirstName(String v)       { this.firstName = v; }
    public String getLastName()              { return lastName; }
    public void setLastName(String v)        { this.lastName = v; }
    public String getStudentId()             { return studentId; }
    public void setStudentId(String v)       { this.studentId = v; }
    public String getEmail()                 { return email; }
    public void setEmail(String v)           { this.email = v; }
    public String getMajor()                 { return major; }
    public void setMajor(String v)           { this.major = v; }
    public String getCreatedAt()             { return createdAt; }

    @Override
    public String toString() {
        return studentId + " — " + getFullName();
    }
}