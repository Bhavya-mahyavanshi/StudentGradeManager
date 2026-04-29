# Student Grade Manager

> A Java Swing desktop application for managing student records, tracking grades across multiple courses, and generating academic reports — backed by a persistent SQLite database with no external server required.

---

## What it does

Student Grade Manager is a fully functional desktop application built with pure Java and Swing. It lets you manage a roster of students, record their course grades, and instantly see calculated GPA, letter grades, and academic standing — all persisted locally in an SQLite database that requires zero setup or configuration.

The project was built to demonstrate layered Java architecture, JDBC database management, and real Swing UI design — without relying on any frameworks.

---

## Features

**Student management**
- Add, edit, and delete student records (first name, last name, student ID, email, major)
- Live search across the student table by name or student ID
- Duplicate student ID detection with a clear validation error before any DB write
- Double-click any row to open the edit dialog instantly

**Grade tracking**
- Record grades per student per course — course name, course code, percentage score (0–100), credit hours, and semester
- Full CRUD with inline validation: rejects blank fields, out-of-range scores, and non-numeric input before touching the database
- Grades are cascade-deleted when a student is removed (SQLite `ON DELETE CASCADE`)

**GPA calculation engine** (`GPACalculator.java`)
- Converts percentage scores to 4.0-scale grade points using a 12-step scale (A → F)
- Calculates credit-hour-weighted cumulative GPA: `Σ(gradePoints × credits) / Σ(credits)`
- Maps GPA to a standing label: Highest Distinction / High Distinction / Distinction / Good Standing / Academic Probation
- Displayed live in the Grades tab whenever a student is selected

**Reports & overview tab**
- Summary table of every student: courses taken, total credits, cumulative GPA, top letter grade, academic standing
- GPA column is colour-coded — green for ≥ 3.5, blue for ≥ 3.0, amber for ≥ 2.0, red below
- Three simultaneous filters: text search (name or ID), standing filter, and major filter — all applied live without a button press
- Aggregate stats bar: total students, cohort average GPA, honour roll count (GPA ≥ 3.5)

**CSV export**
- Export full cohort summary report (one row per student, all GPA fields)
- Export individual student transcript (one row per course, with a GPA summary footer)
- Both use a `JFileChooser` with pre-filled filenames and proper CSV quoting (inner double-quotes are escaped)

---

## Architecture

The project follows a strict four-layer architecture — no layer reaches past its immediate neighbour.

```
com.grademanager/
├── Main.java                        Entry point — launches Swing on EDT via SwingUtilities.invokeLater
│
├── model/
│   ├── Student.java                 Plain Java object: id, firstName, lastName, studentId, email, major, createdAt
│   └── Grade.java                   Plain Java object: id, studentDbId (FK), courseName, courseCode, grade, creditHours, semester
│
├── db/
│   ├── DatabaseManager.java         Singleton JDBC connection to students.db — creates both tables on first run via PRAGMA + CREATE IF NOT EXISTS
│   ├── StudentDAO.java              insert / update / delete / getAll / search / findById — all via PreparedStatement
│   └── GradeDAO.java                insert / update / delete / getByStudentId / getAll — all via PreparedStatement
│
├── service/
│   ├── StudentService.java          Validates input before calling DAO — returns String error or null on success
│   ├── GradeService.java            Validates grade range (0–100) and credit hours (1–6) before calling DAO
│   └── GPACalculator.java           Pure static utility: toGradePoints(), toLetter(), calculateGPA(), standing()
│
├── ui/
│   ├── MainFrame.java               JFrame with JTabbedPane — refreshes the active panel's data on tab switch
│   ├── StudentPanel.java            Students tab: search bar, sortable JTable, Add / Edit / Delete buttons
│   ├── GradePanel.java              Grades tab: student dropdown, grade table, live GPA display
│   ├── ReportPanel.java             Reports tab: filtered summary table, colour-coded GPA, stats bar, CSV export
│   ├── StudentFormDialog.java       Modal dialog (add/edit student) — reused for both flows via null check on Student param
│   └── GradeFormDialog.java         Modal dialog (add/edit grade) — pre-fills fields when editing an existing grade
│
└── util/
    └── CSVExporter.java             exportSummaryReport() and exportStudentGrades() — writes properly quoted CSV with timestamp
```

**Key design decisions:**

- `DatabaseManager` uses the Singleton pattern so the entire application shares one JDBC connection, avoiding repeated open/close overhead
- Services return `String` (the error message) or `null` (success) — this keeps validation logic completely out of the UI layer
- `GPACalculator` is a pure static class with no dependencies — it can be unit tested in isolation with no mocks
- `JTable` row selection is mapped back through `convertRowIndexToModel()` so sorting never breaks edit/delete operations
- The DB ID column is hidden in every table (`setMinWidth(0)`, `setMaxWidth(0)`) but retained in the model for safe row-to-entity mapping without extra DB queries

---

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| UI | Java Swing (`JFrame`, `JTabbedPane`, `JTable`, `JDialog`) |
| Database | SQLite 3 via `sqlite-jdbc 3.45.1.0` (Xerial) |
| Build | Maven 3 — packaged as a runnable JAR |
| IDE | IntelliJ IDEA |

Zero runtime dependencies beyond the SQLite JDBC driver. No Spring, no frameworks, no external database server.

---

## Getting started

**Prerequisites:** Java 17+, Maven 3+

```bash
# Clone
git clone https://github.com/Bhavya-mahyavanshi/StudentGradeManager.git
cd StudentGradeManager

# Build
mvn clean package

# Run
java -jar target/StudentGradeManager-1.0-SNAPSHOT.jar
```

The SQLite database file (`students.db`) is created automatically in the working directory on first launch. No SQL scripts to run, no configuration needed.

To open in an IDE: import as a Maven project. Run `com.grademanager.Main`.

---

## Database schema

Created automatically by `DatabaseManager.initializeTables()` on startup.

```sql
CREATE TABLE IF NOT EXISTS students (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    first_name  TEXT    NOT NULL,
    last_name   TEXT    NOT NULL,
    student_id  TEXT    NOT NULL UNIQUE,
    email       TEXT,
    major       TEXT,
    created_at  TEXT    DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS grades (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id   INTEGER NOT NULL,
    course_name  TEXT    NOT NULL,
    course_code  TEXT,
    grade        REAL    NOT NULL,       -- 0.0 to 100.0
    credit_hours INTEGER DEFAULT 3,
    semester     TEXT,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);
```

---

## GPA scale reference

| Percentage | Letter | Grade points |
|---|---|---|
| ≥ 93 | A  | 4.0 |
| ≥ 90 | A− | 3.7 |
| ≥ 87 | B+ | 3.3 |
| ≥ 83 | B  | 3.0 |
| ≥ 80 | B− | 2.7 |
| ≥ 77 | C+ | 2.3 |
| ≥ 73 | C  | 2.0 |
| ≥ 70 | C− | 1.7 |
| ≥ 67 | D+ | 1.3 |
| ≥ 63 | D  | 1.0 |
| ≥ 60 | D− | 0.7 |
| < 60 | F  | 0.0 |

Cumulative GPA is credit-hour weighted: `Σ(gradePoints × creditHours) / Σ(creditHours)`

---

## Author

**Bhavya Mahyavanshi** · Java Full-Stack Developer

[LinkedIn](https://linkedin.com/in/bhavya-mahyavanshi) · [GitHub](https://github.com/Bhavya-mahyavanshi) · [Portfolio](https://bhavya-mahyavanshi.vercel.app)
