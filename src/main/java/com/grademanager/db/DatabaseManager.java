package com.grademanager.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:students.db";
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        connect();
        initializeTables();
    }

    // Singleton — one connection for the app lifetime
    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void connect() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("Connected to SQLite database.");
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        return connection;
    }

    private void initializeTables() {
        String createStudents = """
            CREATE TABLE IF NOT EXISTS students (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                first_name  TEXT    NOT NULL,
                last_name   TEXT    NOT NULL,
                student_id  TEXT    NOT NULL UNIQUE,
                email       TEXT,
                major       TEXT,
                created_at  TEXT    DEFAULT (datetime('now'))
            );
        """;

        String createGrades = """
            CREATE TABLE IF NOT EXISTS grades (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id  INTEGER NOT NULL,
                course_name TEXT    NOT NULL,
                course_code TEXT,
                grade       REAL    NOT NULL,
                credit_hours INTEGER DEFAULT 3,
                semester    TEXT,
                FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
            );
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute(createStudents);
            stmt.execute(createGrades);
            System.out.println("Tables initialized.");
        } catch (SQLException e) {
            System.err.println("Table creation failed: " + e.getMessage());
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}