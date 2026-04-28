# Student Grade Manager

A Java desktop application for managing student records and academic grades, 
backed by a persistent SQLite database.

## Features
- Add, view, update, and delete student records
- Track and manage grades per student
- Persistent local storage via SQLite (no external DB server required)
- Clean OOP architecture with modular class design

## Tech Stack
- Java (Core)
- SQLite via JDBC
- Maven (build & dependency management)
- IntelliJ IDEA

## How to Run
1. Clone the repo
2. Open in IntelliJ IDEA (or any Maven-compatible IDE)
3. Run `mvn clean install`
4. Execute the main class

## Project Structure
src/main/java/com/grademanager/
├── Main.java
├── model/        # Student, Grade entities
├── dao/          # Database access layer
└── ui/           # User interface layer
