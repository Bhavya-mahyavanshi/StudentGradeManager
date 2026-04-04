package com.grademanager.service;

import com.grademanager.db.StudentDAO;
import com.grademanager.model.Student;
import java.util.List;

public class StudentService {

    private final StudentDAO dao;

    public StudentService() {
        this.dao = new StudentDAO();
    }

    public String addStudent(String firstName, String lastName, String studentId,
                             String email, String major) {
        String error = validate(firstName, lastName, studentId);
        if (error != null) return error;

        // Check for duplicate student ID
        List<Student> existing = dao.search(studentId);
        for (Student s : existing) {
            if (s.getStudentId().equalsIgnoreCase(studentId)) {
                return "Student ID \"" + studentId + "\" already exists.";
            }
        }

        Student s = new Student(firstName.trim(), lastName.trim(),
                studentId.trim(), email.trim(), major.trim());
        return dao.insert(s) ? null : "Database error — could not add student.";
    }

    public String updateStudent(Student s, String firstName, String lastName,
                                String studentId, String email, String major) {
        String error = validate(firstName, lastName, studentId);
        if (error != null) return error;

        // Allow same ID if it belongs to this student, block if taken by another
        List<Student> existing = dao.search(studentId);
        for (Student other : existing) {
            if (other.getStudentId().equalsIgnoreCase(studentId)
                    && other.getId() != s.getId()) {
                return "Student ID \"" + studentId + "\" is already used by another student.";
            }
        }

        s.setFirstName(firstName.trim());
        s.setLastName(lastName.trim());
        s.setStudentId(studentId.trim());
        s.setEmail(email.trim());
        s.setMajor(major.trim());
        return dao.update(s) ? null : "Database error — could not update student.";
    }

    public boolean deleteStudent(int id) {
        return dao.delete(id);
    }

    public List<Student> getAllStudents() {
        return dao.getAll();
    }

    public List<Student> searchStudents(String query) {
        if (query == null || query.isBlank()) return dao.getAll();
        return dao.search(query.trim());
    }

    private String validate(String firstName, String lastName, String studentId) {
        if (firstName == null || firstName.isBlank())  return "First name is required.";
        if (lastName  == null || lastName.isBlank())   return "Last name is required.";
        if (studentId == null || studentId.isBlank())  return "Student ID is required.";
        if (firstName.length() > 50)  return "First name must be under 50 characters.";
        if (lastName.length()  > 50)  return "Last name must be under 50 characters.";
        if (studentId.length() > 20)  return "Student ID must be under 20 characters.";
        return null;
    }
}