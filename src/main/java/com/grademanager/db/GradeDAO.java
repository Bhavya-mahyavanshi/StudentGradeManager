package com.grademanager.db;

import com.grademanager.model.Grade;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GradeDAO {

    private final Connection conn;

    public GradeDAO() {
        this.conn = DatabaseManager.getInstance().getConnection();
    }

    public boolean insert(Grade g) {
        String sql = "INSERT INTO grades (student_id, course_name, course_code, grade, credit_hours, semester) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, g.getStudentDbId());
            ps.setString(2, g.getCourseName());
            ps.setString(3, g.getCourseCode());
            ps.setDouble(4, g.getGrade());
            ps.setInt(5, g.getCreditHours());
            ps.setString(6, g.getSemester());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) g.setId(keys.getInt(1));
            return true;
        } catch (SQLException e) {
            System.err.println("Grade insert failed: " + e.getMessage());
            return false;
        }
    }

    public boolean update(Grade g) {
        String sql = "UPDATE grades SET course_name=?, course_code=?, grade=?, credit_hours=?, semester=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, g.getCourseName());
            ps.setString(2, g.getCourseCode());
            ps.setDouble(3, g.getGrade());
            ps.setInt(4, g.getCreditHours());
            ps.setString(5, g.getSemester());
            ps.setInt(6, g.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Grade update failed: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(int id) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM grades WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Grade delete failed: " + e.getMessage());
            return false;
        }
    }

    public List<Grade> getByStudentId(int studentDbId) {
        List<Grade> list = new ArrayList<>();
        String sql = "SELECT * FROM grades WHERE student_id=? ORDER BY semester, course_name";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentDbId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("GetByStudentId failed: " + e.getMessage());
        }
        return list;
    }

    public List<Grade> getAll() {
        List<Grade> list = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery("SELECT * FROM grades")) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("GetAll grades failed: " + e.getMessage());
        }
        return list;
    }

    private Grade map(ResultSet rs) throws SQLException {
        return new Grade(
                rs.getInt("id"),
                rs.getInt("student_id"),
                rs.getString("course_name"),
                rs.getString("course_code"),
                rs.getDouble("grade"),
                rs.getInt("credit_hours"),
                rs.getString("semester")
        );
    }
}